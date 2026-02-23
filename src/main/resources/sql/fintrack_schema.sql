-- FinTrack - Schema de base (MySQL / XAMPP)
-- A exécuter dans phpMyAdmin (onglet SQL) ou via le client MySQL.

CREATE DATABASE IF NOT EXISTS fintrack
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE fintrack;

-- Table parent
CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email VARCHAR(190) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NULL,
    profile_photo VARCHAR(512) NULL,
    fingerprint_template MEDIUMBLOB NULL,
    face_template MEDIUMBLOB NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

-- Table fille ADMIN (1-1 avec users)
CREATE TABLE IF NOT EXISTS admins (
    user_id BIGINT UNSIGNED NOT NULL,
    admin_code VARCHAR(40) NULL,

    PRIMARY KEY (user_id),
    CONSTRAINT fk_admins_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Table fille CLIENT (1-1 avec users)
CREATE TABLE IF NOT EXISTS clients (
    user_id BIGINT UNSIGNED NOT NULL,
    cin VARCHAR(20) NULL,
    phone VARCHAR(30) NULL,

    PRIMARY KEY (user_id),
    CONSTRAINT fk_clients_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;
