<?php

namespace App\Controller;

use App\Model\Share;
use App\Model\DownloadLog;
use App\Model\FileRepository;
use App\Model\FolderRepository;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;

/**
 * ShareController
 * Gestion des partages sécurisés et des téléchargements publics
 * 
 * @package App\Controller
 * @author ObsiLock Team
 * @version 1.0
 */
class ShareController
{
    private Share $shareModel;
    private DownloadLog $logModel;
    private FileRepository $fileRepo;
    private FolderRepository $folderRepo;

    private $db;

    public function __construct($database)
    {
        $this->db = $database;
        $this->shareModel = new Share($database);
        $this->logModel = new DownloadLog($database);
        $this->fileRepo = new FileRepository($database);
        $this->folderRepo = new FolderRepository($database);
    }

    /**
     * POST /shares - Créer un nouveau partage
     */
    public function create(Request $request, Response $response): Response
    {
        $data = $request->getParsedBody();
        $user = $request->getAttribute('user');
        $userId = $user['user_id'];

        // Validation des champs requis
        if (!isset($data['kind']) || !isset($data['target_id'])) {
            $response->getBody()->write(json_encode([
                'error' => 'Missing required fields: kind, target_id'
            ]));
            return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
        }

        $kind = $data['kind'];
        $targetId = (int)$data['target_id'];
        $label = $data['label'] ?? null;
        $description = $data['description'] ?? null;
        $recipientNote = $data['recipient_note'] ?? null;
        $expiresAt = $data['expires_at'] ?? null;
        $maxUses = isset($data['max_uses']) ? (int)$data['max_uses'] : null;

        // Validation du kind
        if (!in_array($kind, ['file', 'folder'])) {
            $response->getBody()->write(json_encode([
                'error' => 'Invalid kind. Must be "file" or "folder"'
            ]));
            return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
        }

        // Vérifier que l'utilisateur est propriétaire de la ressource
        if ($kind === 'file') {
            $resource = $this->fileRepo->find($targetId);
            if (!$resource || $resource['user_id'] !== $userId) {
                $response->getBody()->write(json_encode([
                    'error' => 'File not found or access denied'
                ]));
                return $response->withStatus(404)->withHeader('Content-Type', 'application/json');
            }
        } else {
            $resource = $this->folderRepo->find($targetId);
            if (!$resource || $resource['user_id'] !== $userId) {
                $response->getBody()->write(json_encode([
                    'error' => 'Folder not found or access denied'
                ]));
                return $response->withStatus(404)->withHeader('Content-Type', 'application/json');
            }
        }

        // Validation de la date d'expiration
        if ($expiresAt && strtotime($expiresAt) <= time()) {
            $response->getBody()->write(json_encode([
                'error' => 'Expiration date must be in the future'
            ]));
            return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
        }

        // Validation de max_uses
        if ($maxUses !== null && $maxUses < 1) {
            $response->getBody()->write(json_encode([
                'error' => 'max_uses must be at least 1'
            ]));
            return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
        }

        // Créer le partage
        $share = $this->shareModel->create($userId, $kind, $targetId, $label, $description, $recipientNote, $expiresAt, $maxUses);

        if (!$share) {
            $response->getBody()->write(json_encode([
                'error' => 'Failed to create share'
            ]));
            return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
        }

        // Générer l'URL publique
        $baseUrl = getenv('APP_URL') ?: 'https://api.obsilock.iris.a3n.fr:4433';
        $publicUrl = $baseUrl . '/share?token=' . $share['token'];

        $response->getBody()->write(json_encode([
            'id' => $share['id'],
            'token' => $share['token'],
            'url' => $publicUrl,
            'kind' => $share['kind'],
            'target_id' => $share['target_id'],
            'label' => $share['label'],
            'description' => $share['description'],
            'recipient_note' => $share['recipient_note'],
            'expires_at' => $share['expires_at'],
            'max_uses' => $share['max_uses'],
            'remaining_uses' => $share['remaining_uses'],
            'created_at' => $share['created_at']
        ]));

        return $response->withStatus(201)->withHeader('Content-Type', 'application/json');
    }

    /**
     * GET /shares - Lister mes partages avec pagination
     */
    public function list(Request $request, Response $response): Response
    {
        $user = $request->getAttribute('user');
        $userId = $user['user_id'];
        $params = $request->getQueryParams();
        
        // Pagination - Supporte per_page/page OU limit/offset
        $limit = isset($params['limit']) ? (int)$params['limit'] : (isset($params['per_page']) ? (int)$params['per_page'] : 20);
        $offset = isset($params['offset']) ? (int)$params['offset'] : ((isset($params['page']) ? (int)$params['page'] - 1 : 0) * $limit);
        $perPage = min(100, max(1, $limit));
        $offset = max(0, $offset);

        $shares = $this->shareModel->getByUser($userId, $perPage, $offset);
        $total = $this->shareModel->countByUser($userId);
        $totalPages = ceil($total / $perPage);

        // Enrichir avec les statistiques
        foreach ($shares as &$share) {
            $share['stats'] = $this->shareModel->getStats($share['id']);
            
            // Générer l'URL publique conviviale
            $baseUrl = getenv('APP_URL') ?: 'https://api.obsilock.iris.a3n.fr:4433';
            $share['url'] = $baseUrl . '/share?token=' . $share['token'];
        }

        $response->getBody()->write(json_encode([
            'shares' => $shares,
            'total' => (int)$total,
            'limit' => $perPage,
            'offset' => $offset,
            'pagination' => [
                'page' => floor($offset / $perPage) + 1,
                'per_page' => $perPage,
                'total_pages' => (int)$totalPages
            ]
        ]));

        return $response->withHeader('Content-Type', 'application/json');
    }

    /**
     * POST /shares/{id}/revoke - Révoquer un partage
     */
    public function revoke(Request $request, Response $response, array $args): Response
    {
        $shareId = (int)$args['id'];
        $user = $request->getAttribute('user');
        $userId = $user['user_id'];

        $success = $this->shareModel->revoke($shareId, $userId);

        if (!$success) {
            $response->getBody()->write(json_encode([
                'error' => 'Share not found or access denied'
            ]));
            return $response->withStatus(404)->withHeader('Content-Type', 'application/json');
        }

        return $response->withStatus(204);
    }

    /**
     * DELETE /shares/{id} - Supprimer définitivement un partage
     */
    public function delete(Request $request, Response $response, array $args): Response
    {
        $shareId = (int)$args['id'];
        $user = $request->getAttribute('user');
        $userId = $user['user_id'];

        $success = $this->shareModel->delete($shareId, $userId);

        if (!$success) {
            $response->getBody()->write(json_encode([
                'error' => 'Share not found or access denied'
            ]));
            return $response->withStatus(404)->withHeader('Content-Type', 'application/json');
        }

        return $response->withStatus(204);
    }

    /**
     * GET /s/{token} - Obtenir les métadonnées d'un partage (PUBLIC)
     */
    public function getPublicMetadata(Request $request, Response $response, array $args): Response
    {
        $token = $args['token'];

        $share = $this->shareModel->getByToken($token);

        if (!$share) {
            $response->getBody()->write(json_encode([
                'error' => 'Share not found'
            ]));
            return $response->withStatus(404)->withHeader('Content-Type', 'application/json');
        }

        // Vérifier la validité
        $validation = $this->shareModel->isValid($share);
        
        if (!$validation['valid']) {
            $statusCode = 410; // Gone
            $errorMessages = [
                'revoked' => 'This share has been revoked',
                'expired' => 'This share has expired',
                'no_uses_left' => 'This share has no remaining uses'
            ];
            $errorMessage = $errorMessages[$validation['reason']] ?? 'This share is no longer valid';

            $response->getBody()->write(json_encode([
                'error' => $errorMessage,
                'reason' => $validation['reason']
            ]));
            return $response->withStatus($statusCode)->withHeader('Content-Type', 'application/json');
        }

        // Récupérer les métadonnées de la ressource (sans infos sensibles)
        if ($share['kind'] === 'file') {
            $resource = $this->fileRepo->find($share['target_id']);
            $metadata = [
                'name' => $resource['filename'] ?? 'Unknown',
                'size' => $resource['size'] ?? 0,
                'type' => 'file'
            ];
        } else {
            $resource = $this->folderRepo->find($share['target_id']);
            $metadata = [
                'name' => $resource['name'] ?? 'Unknown',
                'type' => 'folder'
            ];
        }

        $response->getBody()->write(json_encode([
            'token' => $token,
            'label' => $share['label'],
            'description' => $share['description'],
            'recipient_note' => $share['recipient_note'],
            'kind' => $share['kind'],
            'metadata' => $metadata,
            'expires_at' => $share['expires_at'],
            'remaining_uses' => $share['remaining_uses'],
            'created_at' => $share['created_at']
        ]));

        return $response->withHeader('Content-Type', 'application/json');
    }

    /**
     * POST /s/{token}/download - Télécharger via partage public (PUBLIC)
     */
    /**
     * POST /s/{token}/download - Télécharger via partage public (PUBLIC)
     */
    public function downloadPublic(Request $request, Response $response, array $args): Response
    {
        $token = $args['token'];
        
        // Récupérer IP et User-Agent
        $ip = $this->getClientIp($request);
        $userAgent = $request->getHeaderLine('User-Agent');

        $share = $this->shareModel->getByToken($token);

        if (!$share) {
            $this->logModel->create(0, $ip, $userAgent, false, 'Share not found');
            $response->getBody()->write(json_encode(['error' => 'Share not found']));
            return $response->withStatus(404)->withHeader('Content-Type', 'application/json');
        }

        // Vérifier la validité
        $validation = $this->shareModel->isValid($share);
        
        if (!$validation['valid']) {
            $this->logModel->create($share['id'], $ip, $userAgent, false, $validation['reason']);
            $errorMessages = [
                'revoked' => 'This share has been revoked',
                'expired' => 'This share has expired',
                'no_uses_left' => 'This share has no remaining uses'
            ];
            $errorMessage = $errorMessages[$validation['reason']] ?? 'This share is no longer valid';
            $response->getBody()->write(json_encode(['error' => $errorMessage]));
            return $response->withStatus(410)->withHeader('Content-Type', 'application/json');
        }

        // Décrémenter le compteur
        if ($share['max_uses'] !== null) {
            if (!$this->shareModel->decrementUses($share['id'])) {
                $this->logModel->create($share['id'], $ip, $userAgent, false, 'No uses left');
                $response->getBody()->write(json_encode(['error' => 'This share has no remaining uses']));
                return $response->withStatus(410)->withHeader('Content-Type', 'application/json');
            }
        }

        $uploadDir = getenv('UPLOAD_DIR') ?: '/var/www/html/storage/uploads';

        // TÉLÉCHARGEMENT FICHIER UNIQUE
        if ($share['kind'] === 'file') {
            $file = $this->fileRepo->find($share['target_id']);
            
            if (!$file) {
                $this->logModel->create($share['id'], $ip, $userAgent, false, 'File not found');
                $response->getBody()->write(json_encode(['error' => 'File not found']));
                return $response->withStatus(404)->withHeader('Content-Type', 'application/json');
            }

            // Récupérer la version courante pour les clés de chiffrement
            // On utilise la connexion DB brute car on n'a pas FileVersion injecté
            $version = $this->db->get('file_versions', '*', [
                'file_id' => $file['id'],
                'version' => $file['current_version'] ?? 1
            ]);

            if (!$version) {
                // Fallback: essayer de trouver une v1
                 $version = $this->db->get('file_versions', '*', [
                    'file_id' => $file['id'],
                    'version' => 1
                ]);
            }

            // Si toujours rien, impossible de déchiffrer
            if (!$version) {
                 $this->logModel->create($share['id'], $ip, $userAgent, false, 'Encryption metadata missing');
                 $response->getBody()->write(json_encode(['error' => 'File metadata corrupted']));
                 return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
            }

            // Reconstruction chemin
            $storedName = $version['stored_name'];
            $storedNameWithoutExt = str_replace('.enc', '', $storedName);
            $parts = explode('_', $storedNameWithoutExt);
            $timestamp = end($parts);
            
            if (!is_numeric($timestamp)) {
                 $encryptedPath = $uploadDir . DIRECTORY_SEPARATOR . $storedName;
            } else {
                $date = date('Y/m', (int)$timestamp);
                $encryptedPath = sprintf('%s/%d/%s/%s', $uploadDir, $file['user_id'], $date, $storedName);
            }

            if (!file_exists($encryptedPath)) {
                $this->logModel->create($share['id'], $ip, $userAgent, false, 'File missing on disk');
                $response->getBody()->write(json_encode(['error' => 'File not found on server']));
                return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
            }

            try {
                // Déchiffrement temporaire
                $tempPath = sys_get_temp_dir() . '/' . uniqid('share_dec_', true);
                $encryption = new \App\Service\EncryptionService();
                $encryption->decryptFile(
                    $encryptedPath,
                    $tempPath,
                    $version['key_envelope'],
                    $version['key_nonce'],
                    $version['nonce']
                );

                $this->logModel->create($share['id'], $ip, $userAgent, true, null);

                // Stream
                $stream = fopen($tempPath, 'rb');
                $response->getBody()->write(stream_get_contents($stream));
                fclose($stream);
                
                $fileSize = filesize($tempPath);
                unlink($tempPath); // Nettoyage immédiat

                return $response
                    ->withHeader('Content-Type', $file['mime_type'] ?? 'application/octet-stream')
                    ->withHeader('Content-Disposition', 'attachment; filename="' . $file['filename'] . '"')
                    ->withHeader('Content-Length', (string)$fileSize);

            } catch (\Exception $e) {
                if (isset($tempPath) && file_exists($tempPath)) unlink($tempPath);
                $this->logModel->create($share['id'], $ip, $userAgent, false, 'Decryption error: ' . $e->getMessage());
                $response->getBody()->write(json_encode(['error' => 'Download failed']));
                return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
            }

        } else {
            // TÉLÉCHARGEMENT DOSSIER (ZIP)
            $folder = $this->folderRepo->find($share['target_id']);
            if (!$folder) {
                $response->getBody()->write(json_encode(['error' => 'Folder not found']));
                return $response->withStatus(404)->withHeader('Content-Type', 'application/json');
            }

            // Récupérer les fichiers du dossier
            $files = $this->fileRepo->listByUser($folder['user_id'], $folder['id']);
            
            if (empty($files)) {
                $response->getBody()->write(json_encode(['error' => 'Folder is empty']));
                return $response->withStatus(404)->withHeader('Content-Type', 'application/json');
            }

            $zipPath = sys_get_temp_dir() . '/' . uniqid('folder_', true) . '.zip';
            $zip = new \ZipArchive();
            
            if ($zip->open($zipPath, \ZipArchive::CREATE) !== TRUE) {
                $response->getBody()->write(json_encode(['error' => 'Could not create zip']));
                return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
            }

            $encryption = new \App\Service\EncryptionService();
            $tempFiles = []; // Pour supprimer après

            foreach ($files as $file) {
                 // Récupérer version courante
                 $version = $this->db->get('file_versions', '*', [
                    'file_id' => $file['id'],
                    'version' => $file['current_version'] ?? 1
                ]);

                if (!$version) continue; // Skip corrupted files

                // Chemin chiffré
                $storedName = $version['stored_name'];
                $parts = explode('_', str_replace('.enc', '', $storedName));
                $timestamp = end($parts);
                
                if (!is_numeric($timestamp)) {
                     $encryptedPath = $uploadDir . DIRECTORY_SEPARATOR . $storedName;
                } else {
                    $date = date('Y/m', (int)$timestamp);
                    $encryptedPath = sprintf('%s/%d/%s/%s', $uploadDir, $file['user_id'], $date, $storedName);
                }

                if (!file_exists($encryptedPath)) continue;

                try {
                    $tempDecrypted = sys_get_temp_dir() . '/' . uniqid('zip_entry_', true);
                    $encryption->decryptFile(
                        $encryptedPath,
                        $tempDecrypted,
                        $version['key_envelope'],
                        $version['key_nonce'],
                        $version['nonce']
                    );
                    
                    // Ajouter au ZIP
                    $zip->addFile($tempDecrypted, $file['filename']);
                    $tempFiles[] = $tempDecrypted;
                } catch (\Exception $e) {
                    // Skip failed file or log warning
                    continue;
                }
            }

            $zip->close();

            // Nettoyer fichiers temporaires déchiffrés
            foreach ($tempFiles as $tf) {
                if (file_exists($tf)) unlink($tf);
            }

            if (!file_exists($zipPath)) {
                $response->getBody()->write(json_encode(['error' => 'Zip creation failed']));
                return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
            }

            // Log succès
            $this->logModel->create($share['id'], $ip, $userAgent, true, 'ZIP download');

            // Stream ZIP
            $response = $response
                ->withHeader('Content-Type', 'application/zip')
                ->withHeader('Content-Disposition', 'attachment; filename="' . $folder['name'] . '.zip"')
                ->withHeader('Content-Length', (string)filesize($zipPath));
            
            $response->getBody()->write(file_get_contents($zipPath));
            unlink($zipPath);

            return $response;
        }
    }

    /**
     * Obtenir l'IP réelle du client (gérer les proxies)
     */
    private function getClientIp(Request $request): string
    {
        $serverParams = $request->getServerParams();
        
        // Vérifier les headers de proxy
        if (!empty($serverParams['HTTP_X_FORWARDED_FOR'])) {
            $ips = explode(',', $serverParams['HTTP_X_FORWARDED_FOR']);
            return trim($ips[0]);
        }
        
        if (!empty($serverParams['HTTP_X_REAL_IP'])) {
            return $serverParams['HTTP_X_REAL_IP'];
        }
        
        return $serverParams['REMOTE_ADDR'] ?? '0.0.0.0';
    }
}