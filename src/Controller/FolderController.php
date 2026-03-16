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
        $folders = $this->folders->listByUser($user['user_id'], false);

        $response->getBody()->write(json_encode($folders));
        return $response->withHeader('Content-Type', 'application/json');
    }

    // POST /folders
    public function create(Request $request, Response $response): Response
    {
        $user = $request->getAttribute('user');
        $data = $request->getParsedBody();

        if (empty($data['name'])) {
            $response->getBody()->write(json_encode(['error' => 'Nom requis']));
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
            $response->getBody()->write(json_encode(['error' => 'Dossier introuvable']));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(404);
        }

        // Marquer comme supprimé
        $this->folders->softDelete($folderId);

        $response->getBody()->write(json_encode(['message' => 'Dossier mis à la corbeille']));
        return $response->withHeader('Content-Type', 'application/json');
    }

    // GET /trash/folders
    public function listTrash(Request $request, Response $response): Response
    {
        $user = $request->getAttribute('user');
        $folders = $this->folders->listTrash($user['user_id']);
        
        $response->getBody()->write(json_encode($folders));
        return $response->withHeader('Content-Type', 'application/json');
    }

    // POST /folders/{id}/restore
    public function restore(Request $request, Response $response, array $args): Response
    {
        $user = $request->getAttribute('user');
        $folderId = (int)$args['id'];
        $folder = $this->folders->find($folderId);

        if (!$folder || $folder['user_id'] !== $user['user_id']) {
            $response->getBody()->write(json_encode(['error' => 'Dossier introuvable']));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(404);
        }

        $this->folders->restore($folderId);

        $response->getBody()->write(json_encode(['message' => 'Dossier restauré']));
        return $response->withHeader('Content-Type', 'application/json');
    }

    // DELETE /folders/{id}/permanent
    public function permanentDelete(Request $request, Response $response, array $args): Response
    {
        $user = $request->getAttribute('user');
        $folderId = (int)$args['id'];
        $folder = $this->folders->find($folderId);

        if (!$folder || $folder['user_id'] !== $user['user_id']) {
            $response->getBody()->write(json_encode(['error' => 'Dossier introuvable']));
            return $response->withHeader('Content-Type', 'application/json')->withStatus(404);
        }

        $this->folders->permanentDelete($folderId);

        // Recalcul de quota après suppression en cascade
        $this->users->recalculateQuotaUsed($user['user_id']);

        $response->getBody()->write(json_encode(['message' => 'Dossier supprimé définitivement']));
        return $response->withHeader('Content-Type', 'application/json');
    }

    // PUT /folders/{id} (rename)
    public function rename(Request $request, Response $response, array $args): Response
    {
        $user = $request->getAttribute('user');
        $folderId = (int)$args['id'];
        $data = $request->getParsedBody();
        $newName = $data['name'] ?? null;

        if (!$newName) {
            $response->getBody()->write(json_encode(['error' => 'Nom manquant']));
            return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
        }

        $folder = $this->folders->find($folderId);
        if (!$folder || $folder['user_id'] !== $user['user_id']) {
            $response->getBody()->write(json_encode(['error' => 'Dossier introuvable']));
            return $response->withStatus(404)->withHeader('Content-Type', 'application/json');
        }

        $this->folders->update($folderId, ['name' => $newName]);

        $response->getBody()->write(json_encode(['message' => 'Dossier renommé']));
        return $response->withHeader('Content-Type', 'application/json');
    }
}