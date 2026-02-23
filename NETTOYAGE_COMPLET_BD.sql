-- ==========================================
-- NETTOYAGE COMPLET DES EMPREINTES CORROMPUES
-- ==========================================
-- À exécuter dans phpMyAdmin AVANT de tester
-- ==========================================

USE fintrack;

-- 1. Supprimer TOUTES les empreintes existantes (corrompues)
UPDATE users
SET fingerprint_template = NULL
WHERE fingerprint_template IS NOT NULL;

-- 2. Vérification : TOUS les utilisateurs doivent avoir NULL
SELECT
    id,
    email,
    full_name,
    CASE
        WHEN fingerprint_template IS NULL THEN '✅ Empreinte supprimée (OK)'
        ELSE '❌ ERREUR - Empreinte encore présente'
    END as statut,
    LENGTH(fingerprint_template) as taille_bytes
FROM users
ORDER BY id;

-- 3. Si certaines empreintes persistent, forcer la suppression :
-- UPDATE users SET fingerprint_template = NULL;

-- 4. Résultat attendu : TOUTES les lignes doivent afficher "✅ Empreinte supprimée (OK)"
