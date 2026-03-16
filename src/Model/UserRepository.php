<?php
namespace App\Model;
use Medoo\Medoo;

class UserRepository
{
    private Medoo $db;

    public function __construct(Medoo $db)
    {
        $this->db = $db;
    }

    public function find(int $id): ?array
    {
        return $this->db->get('users', '*', ['id' => $id]) ?: null;
    }

    public function findByEmail(string $email): ?array
    {
        return $this->db->get('users', '*', ['email' => $email]) ?: null;
    }

    public function create(array $data): int
    {
        $this->db->insert('users', $data);
        return (int)$this->db->id();
    }

    public function updateQuota(int $userId, int $newQuota): void
    {
        $this->db->update('users', ['quota_used' => $newQuota], ['id' => $userId]);
    }
    public function recalculateQuotaUsed(int $userId): void
    {
        $sql = "SELECT SUM(file_versions.size) FROM file_versions 
                JOIN files ON file_versions.file_id = files.id 
                WHERE files.user_id = :user_id";
        $total = $this->db->query($sql, [':user_id' => $userId])->fetchColumn();
        
        $this->db->update('users', ['quota_used' => (int)$total], ['id' => $userId]);
    }
}