-- ==========================================
-- NETTOYAGE TOTAL ET VÉRIFICATION
-- ==========================================
-- Exécutez ce script dans phpMyAdmin
-- ==========================================

USE fintrack;

-- 1. SUPPRIMER toutes les empreintes
UPDATE users
SET fingerprint_template = NULL
WHERE fingerprint_template IS NOT NULL;

-- 2. VÉRIFICATION : Toutes les empreintes doivent être NULL
SELECT
    id,
    email,
    full_name,
    CASE
        WHEN fingerprint_template IS NULL THEN '✅ Empreinte supprimée'
        ELSE '❌ ERREUR - Empreinte encore présente !'
    END as statut,
    LENGTH(fingerprint_template) as taille_bytes,
    HEX(SUBSTRING(fingerprint_template, 1, 32)) as premiers_bytes,
    MD5(fingerprint_template) as hash_md5
FROM users
ORDER BY id;

-- 3. Résultat attendu : TOUS les statuts doivent afficher "✅ Empreinte supprimée"
--    ET taille_bytes, premiers_bytes, hash_md5 doivent être NULL

-- 4. Vérifier qu'il n'y a AUCUN doublon
SELECT
    MD5(fingerprint_template) as hash_empreinte,
    GROUP_CONCAT(email SEPARATOR ', ') as comptes_utilisant_cette_empreinte,
    COUNT(*) as nombre_comptes
FROM users
WHERE fingerprint_template IS NOT NULL
GROUP BY MD5(fingerprint_template);

-- 5. Résultat attendu : 0 lignes (aucun résultat car toutes les empreintes sont NULL)
