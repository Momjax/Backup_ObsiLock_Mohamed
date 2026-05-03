<?php
namespace App\Controller;

use App\Model\FolderRepository;
use App\Model\UserRepository;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;

class FolderController
{
    private FolderRepository $folders;
    private UserRepository $users;

    public function __construct(FolderRepository $folders, UserRepository $users)
    {
        $this->folders = $folders;
        $this->users = $users;
    }

    // GET /folders
    public function list(Request $request, Response $response): Response
    {
        $user = $request->getAttribute('user');
        $folders = $this->folders->listByUser($user['user_id']);

        $response->getBody()->write(json_encode($folders), JSON_UNESCAPED_UNICODE);
        return $response->withHeader('Content-Type', 'application/json');
    }

    // POST /folders
    public function create(Request $request, Response $response): Response
    {
        $user = $request->getAttribute('user');
        $data = $request->getParsedBody();

        if (empty($data['name'])) {
            $response->getBody()->write(json_encode(['error' => 'Nom requis']), JSON_UNESCAPED_UNICODE);
            return $response->withHeader('Content-Type', 'application/json')->withStatus(400);
        }

        $folderId = $this->folders->create([
            'user_id' => $user['user_id'],
            'parent_id' => $data['parent_id'] ?? null,
            'name' => $data['name']
        ]);

        $response->getBody()->write(json_encode([
            'message' => 'Dossier créé',
            'id' => $folderId
        ]));
        return $response->withHeader('Content-Type', 'application/json')->withStatus(201);
    }

    // DELETE /folders/{id} (Soft Delete)
    public function delete(Request $request, Response $response, array $args): Response
    {
        $user = $request->getAttribute('user');
        $folderId = (int)$args['id'];

        $folder = $this->folders->find($folderId);

        if (!$folder || $folder['user_id'] !== $user['user_id']) {
            $response->getBody()->write(json_encode(['error' => 'Dossier introuvable']), JSON_UNESCAPED_UNICODE);
            return $response->withHeader('Content-Type', 'application/json')->withStatus(404);
        }

        // Marquer comme supprimé
        $this->folders->softDelete($folderId);

        $response->getBody()->write(json_encode(['message' => 'Dossier mis à la corbeille']), JSON_UNESCAPED_UNICODE);
        return $response->withHeader('Content-Type', 'application/json');
    }

    // GET /trash/folders
    public function listTrash(Request $request, Response $response): Response
    {
        $user = $request->getAttribute('user');
        $folders = $this->folders->listTrash($user['user_id']);
        
        $response->getBody()->write(json_encode($folders), JSON_UNESCAPED_UNICODE);
        return $response->withHeader('Content-Type', 'application/json');
    }

    // POST /folders/{id}/restore
    public function restore(Request $request, Response $response, array $args): Response
    {
        $user = $request->getAttribute('user');
        $folderId = (int)$args['id'];
        $folder = $this->folders->find($folderId);

        if (!$folder || $folder['user_id'] !== $user['user_id']) {
            $response->getBody()->write(json_encode(['error' => 'Dossier introuvable']), JSON_UNESCAPED_UNICODE);
            return $response->withHeader('Content-Type', 'application/json')->withStatus(404);
        }

        $this->folders->restore($folderId);

        $response->getBody()->write(json_encode(['message' => 'Dossier restauré']), JSON_UNESCAPED_UNICODE);
        return $response->withHeader('Content-Type', 'application/json');
    }

    // DELETE /folders/{id}/permanent
    public function permanentDelete(Request $request, Response $response, array $args): Response
    {
        $user = $request->getAttribute('user');
        $folderId = (int)$args['id'];
        $folder = $this->folders->find($folderId);

        if (!$folder || $folder['user_id'] !== $user['user_id']) {
            $response->getBody()->write(json_encode(['error' => 'Dossier introuvable']), JSON_UNESCAPED_UNICODE);
            return $response->withHeader('Content-Type', 'application/json')->withStatus(404);
        }

        $this->folders->permanentDelete($folderId);

        // Recalcul de quota après suppression en cascade
        $this->users->recalculateQuotaUsed($user['user_id']);

        $response->getBody()->write(json_encode(['message' => 'Dossier supprimé définitivement']), JSON_UNESCAPED_UNICODE);
        return $response->withHeader('Content-Type', 'application/json');
    }

    public function rename(Request $request, Response $response, array $args): Response
    {
        $user = $request->getAttribute('user');
        $folderId = (int)$args['id'];
        $data = $request->getParsedBody();
        $newName = $data['name'] ?? null;

        if (!$newName) {
            $response->getBody()->write(json_encode(['error' => 'Nom manquant']), JSON_UNESCAPED_UNICODE);
            return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
        }

        $folder = $this->folders->find($folderId);
        if (!$folder || $folder['user_id'] !== $user['user_id']) {
            $response->getBody()->write(json_encode(['error' => 'Dossier introuvable']), JSON_UNESCAPED_UNICODE);
            return $response->withStatus(404)->withHeader('Content-Type', 'application/json');
        }

        $this->folders->update($folderId, ['name' => $newName]);

        $response->getBody()->write(json_encode(['message' => 'Dossier renommé']), JSON_UNESCAPED_UNICODE);
        return $response->withHeader('Content-Type', 'application/json');
    }

    public function download(Request $request, Response $response, array $args): Response
    {
        $user = $request->getAttribute('user');
        $folderId = (int)$args['id'];

        $folder = $this->folders->find($folderId);
        if (!$folder || $folder['user_id'] !== $user['user_id']) return $response->withStatus(404);

        $zipFile = sys_get_temp_dir() . '/' . uniqid('folder_') . '.zip';
        $zip = new \ZipArchive();
        if ($zip->open($zipFile, \ZipArchive::CREATE) !== TRUE) {
            $response->getBody()->write(json_encode(['error' => 'Impossible de créer le ZIP']));
            return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
        }

        $this->addFolderToZip($folderId, '', $zip, (int)$user['user_id']);
        
        // Si le ZIP est vide, on ajoute un fichier bidon pour éviter qu'il soit invalide
        if ($zip->numFiles == 0) {
            $zip->addFromString('info.txt', 'Dossier vide');
        }
        
        $zip->close();

        if (!file_exists($zipFile)) {
            $response->getBody()->write(json_encode(['error' => 'Erreur génération archive']));
            return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
        }

        $content = file_get_contents($zipFile);
        @unlink($zipFile);

        $response->getBody()->write($content);
        return $response
            ->withHeader('Content-Type', 'application/zip')
            ->withHeader('Content-Disposition', 'attachment; filename="' . addslashes($folder['name']) . '.zip"');
    }

    private function addFolderToZip($folderId, $path, $zip, $userId)
    {
        $db = $this->folders->getDb();
        $files = $db->select('files', '*', ['folder_id' => $folderId]);
        
        foreach ($files as $file) {
            $version = $db->get('file_versions', '*', ['file_id' => $file['id'], 'version' => $file['current_version']]);
            if ($version) {
                // Déchiffrement temporaire
                $enc = new \App\Service\EncryptionService();
                $storageDir = __DIR__ . '/../../storage/user_' . $userId;
                $encPath = $storageDir . '/' . $version['stored_name'];
                
                if (file_exists($encPath)) {
                    $tmpPath = sys_get_temp_dir() . '/' . uniqid('zip_');
                    try {
                        $enc->decryptFile($encPath, $tmpPath, $version['key_envelope'], $version['key_nonce'], $version['nonce']);
                        if (file_exists($tmpPath)) {
                            $zip->addFile($tmpPath, $path . $file['filename']);
                        }
                    } catch (\Exception $e) {}
                }
            }
        }

        $subfolders = $this->folders->listByUser($userId, (int)$folderId);
        foreach ($subfolders as $sub) {
            $this->addFolderToZip($sub['id'], $path . $sub['name'] . '/', $zip, $userId);
        }
    }
}