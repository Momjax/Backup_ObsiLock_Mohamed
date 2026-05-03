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

    public function getDb(): Medoo
    {
        return $this->db;
    }

    public function listByUser(?int $userId, ?int $parentId = null): array
    {
        $where = [];
        if ($userId !== null) $where['user_id'] = $userId;
        if ($parentId !== null) $where['parent_id'] = $parentId;
        
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
        return []; // Corbeille désactivée
    }

    public function softDelete(int $id): void
    {
        $this->permanentDelete($id);
    }

    public function restore(int $id): void
    {
        // Plus de corbeille
    }

    public function permanentDelete(int $id): void
    {
        $this->db->delete('folders', ['id' => $id]);
    }

    public function delete(int $id): void
    {
        $this->permanentDelete($id);
    }

    public function update(int $id, array $data): void
    {
        $this->db->update('folders', $data, ['id' => $id]);
    }
}