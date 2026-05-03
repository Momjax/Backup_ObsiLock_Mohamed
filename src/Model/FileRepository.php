<?php
namespace App\Model;
use Medoo\Medoo;

class FileRepository
{
    private Medoo $db;

    public function __construct(Medoo $db)
    {
        $this->db = $db;
    }

    /**
     * Liste les fichiers d'un utilisateur, optionnellement dans un dossier spécifique.
     * Utilise une jointure car la table 'files' n'a plus de user_id direct.
     */
    public function listByUser(?int $userId, ?int $folderId = null): array
    {
        $where = [];
        if ($userId !== null) {
            $where["folders.user_id"] = $userId;
        }
        if ($folderId !== null) {
            $where["files.folder_id"] = $folderId;
        }

        return $this->db->select("files", [
            "[>]folders" => ["folder_id" => "id"]
        ], [
            "files.id",
            "files.folder_id",
            "files.filename",
            "files.stored_name",
            "files.size",
            "files.mime_type",
            "files.checksum",
            "files.current_version",
            "files.uploaded_at"
        ], $where);
    }

    public function find(int $id): ?array
    {
        return $this->db->get('files', [
            "id",
            "folder_id",
            "filename",
            "stored_name",
            "size",
            "mime_type",
            "checksum",
            "current_version",
            "uploaded_at"
        ], ['id' => $id]) ?: null;
    }

    /**
     * Vérifie si un fichier appartient à un utilisateur via son dossier parent.
     */
    public function getOwnerId(int $fileId): ?int
    {
        return (int)$this->db->get("files", [
            "[>]folders" => ["folder_id" => "id"]
        ], "folders.user_id", ["files.id" => $fileId]) ?: null;
    }

    public function create(array $data): int
    {
        $this->db->insert('files', $data);
        return (int)$this->db->id();
    }

    public function permanentDelete(int $id): void
    {
        $this->db->delete('files', ['id' => $id]);
    }

    public function delete(int $id): void
    {
        $this->permanentDelete($id);
    }

    public function update(int $id, array $data): void
    {
        $this->db->update('files', $data, ['id' => $id]);
    }

    /**
     * Calcule la taille totale occupée par un utilisateur.
     */
    public function totalSize(int $userId): int
    {
        return (int)$this->db->sum("files", [
            "[>]folders" => ["folder_id" => "id"]
        ], "files.size", ["folders.user_id" => $userId]) ?: 0;
    }
}