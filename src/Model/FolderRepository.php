<?php
namespace App\Model;
use Medoo\Medoo;

class FolderRepository
{
    private Medoo $db;

    public function __construct(Medoo $db)
    {
        $this->db = $db;
    }

    public function listByUser(int $userId, bool $includeDeleted = false): array
    {
        $where = ['user_id' => $userId];
        if (!$includeDeleted) {
            $where['is_deleted'] = 0;
        }
        return $this->db->select('folders', '*', $where);
    }

    public function find(int $id): ?array
    {
        return $this->db->get('folders', '*', ['id' => $id]) ?: null;
    }

    public function create(array $data): int
    {
        $this->db->insert('folders', $data);
        return (int)$this->db->id();
    }

    public function listTrash(int $userId): array
    {
        return $this->db->select('folders', '*', [
            'user_id' => $userId,
            'is_deleted' => 1
        ]);
    }

    public function softDelete(int $id): void
    {
        $this->db->update('folders', ['is_deleted' => 1], ['id' => $id]);
    }

    public function restore(int $id): void
    {
        $this->db->update('folders', ['is_deleted' => 0], ['id' => $id]);
    }

    public function permanentDelete(int $id): void
    {
        $this->db->delete('folders', ['id' => $id]);
    }

    public function delete(int $id): void
    {
        $this->softDelete($id);
    }

    public function update(int $id, array $data): void
    {
        $this->db->update('folders', $data, ['id' => $id]);
    }
}