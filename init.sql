-- Initialisation de la base de données pour Docker
USE coffre_fort;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    quota_total BIGINT DEFAULT 52428800,
    quota_used BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS folders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    parent_id INT NULL,
    name VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS files (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    folder_id INT NULL,
    filename VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    checksum VARCHAR(64) NULL,
    current_version INT DEFAULT 1,
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE SET NULL,
    INDEX idx_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    value VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

INSERT INTO settings (name, value) VALUES ('quota_bytes', '52428800')
ON DUPLICATE KEY UPDATE value = '52428800';

CREATE TABLE IF NOT EXISTS shares (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    kind ENUM('file', 'folder') NOT NULL,
    target_id INT NOT NULL,
    label VARCHAR(255) NULL,
    description TEXT NULL,
    recipient_note TEXT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    token_signature VARCHAR(255) NOT NULL,
    expires_at DATETIME NULL,
    max_uses INT NULL,
    remaining_uses INT NULL,
    is_revoked TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS downloads_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    share_id INT NOT NULL,
    version_id INT NULL,
    ip VARCHAR(64) NOT NULL,
    user_agent TEXT NULL,
    success TINYINT(1) DEFAULT 1,
    message TEXT NULL,
    downloaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (share_id) REFERENCES shares(id) ON DELETE CASCADE,
    INDEX idx_share (share_id),
    INDEX idx_ip (ip)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS file_versions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_id INT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    stored_name VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    checksum VARCHAR(64) NULL,
    iv VARCHAR(255) NULL,
    auth_tag VARCHAR(255) NULL,
    mime_type VARCHAR(100) NULL,
    nonce VARCHAR(255) NULL,
    key_envelope TEXT NULL,
    key_nonce VARCHAR(255) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_current TINYINT(1) DEFAULT 1,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    INDEX idx_file (file_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS upload_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    file_id INT NULL,
    filename VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    mime_type VARCHAR(100) NULL,
    checksum VARCHAR(64) NULL,
    ip_address VARCHAR(45) NULL,
    user_agent TEXT NULL,
    success TINYINT(1) DEFAULT 1,
    error_message TEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;