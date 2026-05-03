<?php
namespace App\Controller;

use App\Model\FileRepository;
use App\Model\UserRepository;
use App\Model\FileVersion;
use App\Model\FolderRepository;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;

class FileController
{
    private FileRepository $files;
    private UserRepository $users;
    private FolderRepository $folders;
    private FileVersion $versions;
    private string $uploadDir;
    private $db;

    public function __construct(FileRepository $files, UserRepository $users, FolderRepository $folders, string $uploadDir, $database = null)
    {
        $this->files   = $files;
        $this->users   = $users;
        $this->folders = $folders;
        $this->uploadDir = $uploadDir;
        $this->db = $database;
        if ($database) {
            $this->versions = new FileVersion($database);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private function findEncFile(int $userId, string $storedName): ?string
    {
        $base = sprintf('%s/%d', $this->uploadDir, $userId);
        if (!is_dir($base)) return null;
        $it = new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($base, \FilesystemIterator::SKIP_DOTS));
        foreach ($it as $f) {
            if ($f->getFilename() === $storedName) return $f->getPathname();
        }
        return null;
    }

    private function ok(Response $response, array $data, int $status = 200): Response
    {
        $response->getBody()->write(json_encode($data), JSON_UNESCAPED_UNICODE);
        return $response->withHeader('Content-Type', 'application/json')->withStatus($status);
    }

    private function err(Response $response, string $msg, int $status = 400): Response
    {
        $response->getBody()->write(json_encode(['error' => $msg]), JSON_UNESCAPED_UNICODE);
        return $response->withHeader('Content-Type', 'application/json')->withStatus($status);
    }

    // ─── LIST ────────────────────────────────────────────────────────────────

    public function list(Request $request, Response $response): Response
    {
        $user = $request->getAttribute('user');
        $params = $request->getQueryParams();
        $folderId = isset($params['folder_id']) ? (int)$params['folder_id'] : null;
        $files = $this->files->listByUser($user['user_id'], $folderId);
        return $this->ok($response, ['data' => $files]);
    }

    // ─── UPLOAD ──────────────────────────────────────────────────────────────

    public function upload(Request $request, Response $response): Response
    {
        $user = $request->getAttribute('user');
        $uploadedFiles = $request->getUploadedFiles();
        $params = $request->getParsedBody();

        if (!isset($uploadedFiles['file'])) return $this->err($response, 'Aucun fichier');

        $file = $uploadedFiles['file'];
        $size = $file->getSize();

        $folderCount = $this->db->count('folders', ['user_id' => $user['user_id']]);
        if ($folderCount === 0) return $this->err($response, 'Créez un dossier avant d\'uploader.', 400);

        $folderId = isset($params['folder_id']) ? (int)$params['folder_id'] : null;
        if (!$folderId) return $this->err($response, 'Sélectionnez un dossier.', 400);

        $userInfo = $this->users->find($user['user_id']);
        if (($userInfo['quota_used'] + $size) > $userInfo['quota_total']) return $this->err($response, 'Quota dépassé', 413);

        $originalName = $file->getClientFilename();
        $storedName   = uniqid('f_', true) . '_' . time();
        $tempPath     = sys_get_temp_dir() . '/' . $storedName . '.tmp';
        $encTempPath  = sys_get_temp_dir() . '/' . $storedName . '.enc';

        try {
            $file->moveTo($tempPath);
            $checksum   = hash_file('sha256', $tempPath);
            $enc        = new \App\Service\EncryptionService();
            $encData    = $enc->encryptFile($tempPath, $encTempPath);

            $uploadPath = sprintf('%s/%d/%s/%s', $this->uploadDir, $user['user_id'], date('Y'), date('m'));
            if (!is_dir($uploadPath)) mkdir($uploadPath, 0777, true);
            rename($encTempPath, $uploadPath . '/' . $storedName . '.enc');
            if (file_exists($tempPath)) unlink($tempPath);

            $fileId = $this->files->create([
                'folder_id'       => $folderId,
                'filename'        => $originalName,
                'stored_name'     => $storedName . '.enc',
                'size'            => $size,
                'mime_type'       => $file->getClientMediaType(),
                'checksum'        => $checksum,
                'current_version' => 1,
            ]);

            $this->db->insert('file_versions', [
                'file_id'      => $fileId,
                'version'      => 1,
                'stored_name'  => $storedName . '.enc',
                'size'         => $size,
                'checksum'     => $checksum,
                'mime_type'    => $file->getClientMediaType(),
                'nonce'        => $encData['chunk_nonce_start'],
                'key_envelope' => $encData['key_envelope'],
                'key_nonce'    => $encData['nonce'],
                'is_current'   => 1,
            ]);

            $this->users->updateQuota($user['user_id'], $userInfo['quota_used'] + $size);
            return $this->ok($response, ['message' => 'Upload réussi', 'id' => $fileId], 201);
        } catch (\Exception $e) {
            if (file_exists($tempPath))    unlink($tempPath);
            if (file_exists($encTempPath)) unlink($encTempPath);
            return $this->err($response, $e->getMessage(), 500);
        }
    }

    // ─── SHOW ────────────────────────────────────────────────────────────────

    public function show(Request $request, Response $response, array $args): Response
    {
        $user   = $request->getAttribute('user');
        $fileId = (int)$args['id'];
        $file   = $this->files->find($fileId);
        if (!$file || $this->files->getOwnerId($fileId) !== $user['user_id']) return $response->withStatus(404);

        return $this->ok($response, [
            'id'              => (int)$file['id'],
            'original_name'   => $file['filename'],
            'size'            => (int)$file['size'],
            'current_version' => (int)($file['current_version'] ?? 1),
            'created_at'      => $file['created_at'] ?? '',
            'updated_at'      => $file['updated_at'] ?? '',
        ]);
    }

    // ─── DOWNLOAD ────────────────────────────────────────────────────────────

    public function download(Request $request, Response $response, array $args): Response
    {
        $user   = $request->getAttribute('user');
        $fileId = (int)$args['id'];
        $file   = $this->files->find($fileId);
        if (!$file || $this->files->getOwnerId($fileId) !== $user['user_id']) return $response->withStatus(404);

        $version = $this->db->get('file_versions', '*', ['file_id' => $fileId, 'version' => $file['current_version']]);
        if (!$version) return $this->err($response, 'Version introuvable', 404);

        $encPath = $this->findEncFile($user['user_id'], $version['stored_name']);
        if (!$encPath) return $this->err($response, 'Fichier physique introuvable', 404);

        $tmpPath = sys_get_temp_dir() . '/' . uniqid('dl_', true);
        try {
            $enc = new \App\Service\EncryptionService();
            $enc->decryptFile($encPath, $tmpPath, $version['key_envelope'], $version['key_nonce'], $version['nonce']);
        } catch (\Exception $e) {
            return $this->err($response, 'Erreur déchiffrement: ' . $e->getMessage(), 500);
        }

        $response->getBody()->write(file_get_contents($tmpPath));
        @unlink($tmpPath);
        return $response
            ->withHeader('Content-Type', $version['mime_type'] ?? 'application/octet-stream')
            ->withHeader('Content-Disposition', 'attachment; filename="' . addslashes($file['filename']) . '"');
    }

    // ─── RENAME ──────────────────────────────────────────────────────────────

    public function rename(Request $request, Response $response, array $args): Response
    {
        $user    = $request->getAttribute('user');
        $fileId  = (int)$args['id'];
        $data    = $request->getParsedBody();
        $newName = $data['name'] ?? null;
        if (!$newName) return $this->err($response, 'Nom manquant');
        $file = $this->files->find($fileId);
        if (!$file || $this->files->getOwnerId($fileId) !== $user['user_id']) return $response->withStatus(404);
        $this->db->update('files', ['filename' => $newName], ['id' => $fileId]);
        return $this->ok($response, ['message' => 'Fichier renommé']);
    }

    // ─── MOVE ────────────────────────────────────────────────────────────────

    public function move(Request $request, Response $response, array $args): Response
    {
        $user        = $request->getAttribute('user');
        $fileId      = (int)$args['id'];
        $params      = $request->getParsedBody();
        $newFolderId = isset($params['folder_id']) ? (int)$params['folder_id'] : null;
        if (!$newFolderId) return $this->err($response, 'Dossier destination manquant');
        $file = $this->files->find($fileId);
        if (!$file || $this->files->getOwnerId($fileId) !== $user['user_id']) return $response->withStatus(404);
        $this->db->update('files', ['folder_id' => $newFolderId], ['id' => $fileId]);
        return $this->ok($response, ['message' => 'Fichier déplacé']);
    }

    // ─── DUPLICATE ───────────────────────────────────────────────────────────

    public function duplicate(Request $request, Response $response, array $args): Response
    {
        $user   = $request->getAttribute('user');
        $fileId = (int)$args['id'];
        $file   = $this->files->find($fileId);
        if (!$file || $this->files->getOwnerId($fileId) !== $user['user_id']) return $response->withStatus(404);

        $userInfo = $this->users->find($user['user_id']);
        if (($userInfo['quota_used'] + $file['size']) > $userInfo['quota_total']) return $this->err($response, 'Quota dépassé', 413);

        $version = $this->db->get('file_versions', '*', ['file_id' => $fileId, 'version' => $file['current_version']]);
        if (!$version) return $response->withStatus(404);

        $newStoredName = uniqid('f_', true) . '_' . time() . '.enc';
        $encryptedData = $file['encrypted_data']; // Données BLOB si présentes

        // Si stockage physique (disque)
        if ($version['stored_name'] !== 'bdd_storage' && empty($encryptedData)) {
            $oldPath = $this->findEncFile($user['user_id'], $version['stored_name']);
            if ($oldPath && file_exists($oldPath)) {
                $newPath = dirname($oldPath) . '/' . $newStoredName;
                copy($oldPath, $newPath);
            }
        }

        $newFileId = $this->files->create([
            'folder_id'       => $file['folder_id'],
            'filename'        => 'Copie de ' . $file['filename'],
            'stored_name'     => $newStoredName,
            'size'            => $file['size'],
            'mime_type'       => $file['mime_type'],
            'checksum'        => $file['checksum'],
            'encrypted_data'  => $encryptedData, // On duplique les données BLOB
            'current_version' => 1,
        ]);

        $this->db->insert('file_versions', [
            'file_id'      => $newFileId, 'version' => 1,
            'stored_name'  => $newStoredName, 'size' => $version['size'],
            'checksum'     => $version['checksum'], 'mime_type' => $version['mime_type'],
            'nonce'        => $version['nonce'], 'key_envelope' => $version['key_envelope'],
            'key_nonce'    => $version['key_nonce'], 'is_current' => 1,
        ]);

        $this->users->updateQuota($user['user_id'], $userInfo['quota_used'] + $file['size']);
        return $this->ok($response, ['message' => 'Fichier dupliqué', 'id' => $newFileId], 201);
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    public function delete(Request $request, Response $response, array $args): Response
    {
        return $this->permanentDelete($request, $response, $args);
    }

    public function permanentDelete(Request $request, Response $response, array $args): Response
    {
        $user   = $request->getAttribute('user');
        $fileId = (int)$args['id'];
        $file   = $this->files->find($fileId);
        if (!$file || $this->files->getOwnerId($fileId) !== $user['user_id']) return $response->withStatus(404);

        $versions = $this->db->select('file_versions', '*', ['file_id' => $fileId]);
        foreach ($versions as $v) {
            $path = $this->findEncFile($user['user_id'], $v['stored_name']);
            if ($path && file_exists($path)) unlink($path);
        }

        $this->db->delete('file_versions', ['file_id' => $fileId]);
        $this->files->delete($fileId);
        $this->users->recalculateQuotaUsed($user['user_id']);
        return $this->ok($response, ['message' => 'Fichier supprimé définitivement']);
    }

    // ─── TRASH (stubs) ───────────────────────────────────────────────────────

    public function listTrash(Request $request, Response $response): Response
    {
        return $this->ok($response, []);
    }

    public function restore(Request $request, Response $response, array $args): Response
    {
        return $this->ok($response, ['message' => 'Restauré']);
    }

    // ─── QUOTA / STATS / ACTIVITY ────────────────────────────────────────────

    public function quota(Request $request, Response $response): Response
    {
        $user     = $request->getAttribute('user');
        $userInfo = $this->users->find($user['user_id']);
        return $this->ok($response, [
            'total'   => (int)$userInfo['quota_total'],
            'used'    => (int)$userInfo['quota_used'],
            'percent' => $userInfo['quota_total'] > 0
                ? round(($userInfo['quota_used'] / $userInfo['quota_total']) * 100, 2) : 0,
        ]);
    }

    public function stats(Request $request, Response $response): Response
    {
        $user        = $request->getAttribute('user');
        $filesCount  = $this->db->count('files', ["[>]folders" => ["folder_id" => "id"]], "files.id", ["folders.user_id" => $user['user_id']]);
        $foldersCount = $this->db->count('folders', ['user_id' => $user['user_id']]);
        return $this->ok($response, ['files_count' => $filesCount, 'folders_count' => $foldersCount]);
    }

    public function activity(Request $request, Response $response): Response
    {
        return $this->ok($response, []);
    }

    // ─── VERSIONS ────────────────────────────────────────────────────────────

    public function uploadVersion(Request $request, Response $response, array $args): Response
    {
        $user   = $request->getAttribute('user');
        $fileId = (int)$args['id'];
        $file   = $this->files->find($fileId);
        if (!$file || $this->files->getOwnerId($fileId) !== $user['user_id']) return $this->err($response, 'Fichier non trouvé', 404);

        $uploadedFiles = $request->getUploadedFiles();
        if (!isset($uploadedFiles['file'])) return $this->err($response, 'Aucun fichier envoyé');

        $uploaded = $uploadedFiles['file'];
        $size     = $uploaded->getSize();
        $userInfo = $this->users->find($user['user_id']);
        if (($userInfo['quota_used'] + $size) > $userInfo['quota_total']) return $this->err($response, 'Quota dépassé', 413);

        $storedName  = uniqid('f_', true) . '_' . time();
        $tempPath    = sys_get_temp_dir() . '/' . $storedName . '.tmp';
        $encTempPath = sys_get_temp_dir() . '/' . $storedName . '.enc';

        try {
            $uploaded->moveTo($tempPath);
            $checksum = hash_file('sha256', $tempPath);
            $enc      = new \App\Service\EncryptionService();
            $encData  = $enc->encryptFile($tempPath, $encTempPath);

            $uploadPath = sprintf('%s/%d/%s/%s', $this->uploadDir, $user['user_id'], date('Y'), date('m'));
            if (!is_dir($uploadPath)) mkdir($uploadPath, 0777, true);
            rename($encTempPath, $uploadPath . '/' . $storedName . '.enc');
            if (file_exists($tempPath)) unlink($tempPath);

            $lastVersion = (int)($this->db->max('file_versions', 'version', ['file_id' => $fileId]) ?? 0);
            $newVersion  = $lastVersion + 1;

            $this->db->update('file_versions', ['is_current' => 0], ['file_id' => $fileId]);

            $this->db->insert('file_versions', [
                'file_id'      => $fileId,
                'version'      => $newVersion,
                'stored_name'  => $storedName . '.enc',
                'size'         => $size,
                'checksum'     => $checksum,
                'mime_type'    => $uploaded->getClientMediaType(),
                'nonce'        => $encData['chunk_nonce_start'],
                'key_envelope' => $encData['key_envelope'],
                'key_nonce'    => $encData['nonce'],
                'is_current'   => 1,
            ]);

            $this->db->update('files', [
                'stored_name'     => $storedName . '.enc',
                'size'            => $size,
                'checksum'        => $checksum,
                'current_version' => $newVersion,
            ], ['id' => $fileId]);

            $this->users->updateQuota($user['user_id'], $userInfo['quota_used'] + $size);
            return $this->ok($response, ['message' => 'Nouvelle version uploadée', 'version' => $newVersion], 201);

        } catch (\Exception $e) {
            if (file_exists($tempPath))    unlink($tempPath);
            if (file_exists($encTempPath)) unlink($encTempPath);
            return $this->err($response, $e->getMessage(), 500);
        }
    }

    public function listVersions(Request $request, Response $response, array $args): Response
    {
        $fileId = (int)$args['id'];
        $user   = $request->getAttribute('user');
        $file   = $this->files->find($fileId);
        if (!$file || $this->files->getOwnerId($fileId) !== $user['user_id']) return $this->err($response, 'Fichier non trouvé', 404);

        $p      = $request->getQueryParams();
        $limit  = isset($p['limit'])  ? (int)$p['limit']  : 10;
        $offset = isset($p['offset']) ? (int)$p['offset'] : 0;

        $rows  = $this->db->select('file_versions', '*', ['file_id' => $fileId, 'ORDER' => ['version' => 'DESC'], 'LIMIT' => [$offset, $limit]]);
        $total = $this->db->count('file_versions', ['file_id' => $fileId]);

        $formatted = [];
        foreach (($rows ?: []) as $v) {
            $formatted[] = [
                'id'         => (int)$v['id'],
                'file_id'    => (int)$v['file_id'],
                'version'    => (int)$v['version'],
                'size'       => (int)$v['size'],
                'checksum'   => $v['checksum'],
                'created_at' => $v['created_at'],
                'is_current' => (bool)$v['is_current'],
            ];
        }

        return $this->ok($response, ['versions' => $formatted, 'total' => (int)$total, 'offset' => $offset, 'limit' => $limit]);
    }

    public function downloadVersion(Request $request, Response $response, array $args): Response
    {
        $user      = $request->getAttribute('user');
        $fileId    = (int)$args['id'];
        $versionNb = (int)$args['version'];
        $file      = $this->files->find($fileId);
        if (!$file || $this->files->getOwnerId($fileId) !== $user['user_id']) return $response->withStatus(404);

        $version = $this->db->get('file_versions', '*', ['file_id' => $fileId, 'version' => $versionNb]);
        if (!$version) return $this->err($response, 'Version introuvable', 404);

        $encPath = $this->findEncFile($user['user_id'], $version['stored_name']);
        if (!$encPath) return $this->err($response, 'Fichier physique introuvable', 404);

        $tmpPath = sys_get_temp_dir() . '/' . uniqid('dlv_', true);
        try {
            $enc = new \App\Service\EncryptionService();
            $enc->decryptFile($encPath, $tmpPath, $version['key_envelope'], $version['key_nonce'], $version['nonce']);
        } catch (\Exception $e) {
            return $this->err($response, 'Erreur déchiffrement: ' . $e->getMessage(), 500);
        }

        $ext      = pathinfo($file['filename'], PATHINFO_EXTENSION);
        $base     = pathinfo($file['filename'], PATHINFO_FILENAME);
        $filename = $base . '_v' . $versionNb . '.' . $ext;

        $response->getBody()->write(file_get_contents($tmpPath));
        @unlink($tmpPath);
        return $response
            ->withHeader('Content-Type', $version['mime_type'] ?? 'application/octet-stream')
            ->withHeader('Content-Disposition', 'attachment; filename="' . addslashes($filename) . '"');
    }
}
