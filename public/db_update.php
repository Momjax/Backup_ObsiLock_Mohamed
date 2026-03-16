<?php
// Script de mise à jour de la base de données
// À exécuter une seule fois pour ajouter la colonne description

error_reporting(E_ALL);
ini_set('display_errors', 1);

require_once __DIR__ . '/../vendor/autoload.php';

// Charger l'env si nécessaire (Slim le fait normalement)
if (file_exists(__DIR__ . '/../.env')) {
    $lines = file(__DIR__ . '/../.env', FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
    foreach ($lines as $line) {
        if (strpos(trim($line), '#') === 0) continue;
        list($name, $value) = explode('=', $line, 2);
        $_ENV[$name] = $value;
        putenv("$name=$value");
    }
}

try {
    $host = getenv('DB_HOST') ?: 'obsilock_db';
    $dbname = getenv('DB_NAME') ?: 'coffre_fort';
    $user = getenv('DB_USER') ?: 'obsilock_user';
    $pass = getenv('DB_PASS') ?: 'SDNENJI2329nfzehzenideza';

    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    echo "Tentative d'ajout de la colonne 'description'...\n";
    $pdo->exec("ALTER TABLE shares ADD COLUMN description TEXT NULL AFTER label");
    echo "Succès : La colonne 'description' a été ajoutée à la table 'shares'.\n";

} catch (PDOException $e) {
    if (strpos($e->getMessage(), 'Duplicate column name') !== false) {
        echo "Info : La colonne 'description' existe déjà.\n";
    } else {
        echo "Erreur : " . $e->getMessage() . "\n";
    }
}
