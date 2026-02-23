-- FinTrack - Données de test (seed)
-- Objectif: ne pas laisser les tables vides.

USE fintrack;

-- Nettoyage (optionnel) : attention, ça supprime les données
-- ASTUCE: TRUNCATE peut échouer avec des clés étrangères (#1701).
-- On utilise donc DELETE puis on remet l'auto_increment à 1.
DELETE FROM admins;
DELETE FROM clients;
DELETE FROM users;
ALTER TABLE users AUTO_INCREMENT = 1;

-- 1) ADMIN
INSERT INTO users(email, password_hash, full_name, profile_photo, is_active)
VALUES ('admin@fintrack.tn', 'hash-admin-demo', 'Admin FinTrack', NULL, 1);
INSERT INTO admins(user_id, admin_code)
VALUES (LAST_INSERT_ID(), 'ADM-001');

-- 2) CLIENT (Ghaith)
INSERT INTO users(email, password_hash, full_name, profile_photo, is_active)
VALUES ('ghaith@fintrack.tn', 'hash-demo', 'ghaith', NULL, 1);
INSERT INTO clients(user_id, cin, phone)
VALUES (LAST_INSERT_ID(), 'CIN123456', '+216 20 000 000');

-- 3) CLIENT (Abdou)
INSERT INTO users(email, password_hash, full_name, profile_photo, is_active)
VALUES ('abdou@fintrack.tn', 'hash-demo', 'abdou', NULL, 1);
INSERT INTO clients(user_id, cin, phone)
VALUES (LAST_INSERT_ID(), 'CIN222222', '+216 22 222 222');
