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

    public function listByUser(int $userId, ?int $folderId = null, bool $includeDeleted = false): array
    {
        $where = ['user_id' => $userId];
        if (!$includeDeleted) {
            $where['is_deleted'] = 0;
        }
        if ($folderId !== null) {
            $where['folder_id'] = $folderId;
        }
        return $this->db->select('files', '*', $where);
    }

    public function find(int $id): ?array
    {
        return $this->db->get('files', '*', ['id' => $id]) ?: null;
    }

    public function create(array $data): int
    {
        $this->db->insert('files', $data);
        return (int)$this->db->id();
    }

    public function listTrash(int $userId): array
    {
        return $this->db->select('files', '*', [
            'user_id' => $userId,
            'is_deleted' => 1
        ]);
    }

    public function softDelete(int $id): void
    {
        $this->db->update('files', ['is_deleted' => 1], ['id' => $id]);
    }

    public function restore(int $id): void
    {
        $this->db->update('files', ['is_deleted' => 0], ['id' => $id]);
    }

    public function permanentDelete(int $id): void
    {
        $this->db->delete('files', ['id' => $id]);
    }

    public function delete(int $id): void
    {
        $this->softDelete($id);
    }

    public function update(int $id, array $data): void
    {
        $this->db->update('files', $data, ['id' => $id]);
    }

    public function totalSize(int $userId): int
    {
        return (int)$this->db->sum('files', 'size', ['user_id' => $userId]) ?: 0;
    }
}