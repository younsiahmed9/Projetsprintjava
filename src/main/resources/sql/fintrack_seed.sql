-- FinTrack - Données de test (seed)
-- Objectif: ne pas laisser les tables vides.

USE fintrack;

-- Nettoyage (optionnel) : attention, ça supprime les données
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE admins;
TRUNCATE TABLE clients;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1) ADMIN
INSERT INTO users(email, password_hash, full_name, profile_photo, role, is_active)
VALUES ('admin@fintrack.tn', 'hash-admin-demo', 'Admin FinTrack', NULL, 'ADMIN', 1);
INSERT INTO admins(user_id, admin_code)
VALUES (LAST_INSERT_ID(), 'ADM-001');

-- 2) CLIENT (Ghaith)
INSERT INTO users(email, password_hash, full_name, profile_photo, role, is_active)
VALUES ('ghaith@fintrack.tn', 'hash-demo', 'ghaith', NULL, 'CLIENT', 1);
INSERT INTO clients(user_id, cin, phone)
VALUES (LAST_INSERT_ID(), 'CIN123456', '+216 20 000 000');

-- 3) CLIENT (Abdou)
INSERT INTO users(email, password_hash, full_name, profile_photo, role, is_active)
VALUES ('abdou@fintrack.tn', 'hash-demo', 'abdou', NULL, 'CLIENT', 1);
INSERT INTO clients(user_id, cin, phone)
VALUES (LAST_INSERT_ID(), 'CIN222222', '+216 22 222 222');
