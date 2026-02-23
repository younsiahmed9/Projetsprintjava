-- Script pour réinitialiser toutes les empreintes digitales
-- À exécuter dans phpMyAdmin ou MySQL Workbench

USE fintrack;

-- Supprimer toutes les empreintes existantes
UPDATE users SET fingerprint_template = NULL WHERE fingerprint_template IS NOT NULL;

-- Vérification
SELECT
    id,
    email,
    full_name,
    CASE
        WHEN fingerprint_template IS NULL THEN '❌ Aucune empreinte'
        ELSE '✅ Empreinte présente'
    END as statut_empreinte
FROM users
ORDER BY id;
