-- ==========================================
-- VÉRIFICATION DES EMPREINTES DANS LA BASE
-- ==========================================
-- Ce script vérifie si les empreintes sont VRAIMENT différentes
-- ==========================================

USE fintrack;

-- 1. Voir les empreintes enregistrées
SELECT
    id,
    email,
    full_name,
    CASE
        WHEN fingerprint_template IS NULL THEN '❌ Aucune empreinte'
        ELSE '✅ Empreinte présente'
    END as statut,
    LENGTH(fingerprint_template) as taille_bytes,
    HEX(SUBSTRING(fingerprint_template, 1, 32)) as premiers_32_bytes,
    MD5(fingerprint_template) as hash_md5
FROM users
ORDER BY id;

-- 2. Détecter les DOUBLONS (même empreinte utilisée plusieurs fois)
SELECT
    MD5(fingerprint_template) as hash_empreinte,
    GROUP_CONCAT(email SEPARATOR ', ') as comptes_utilisant_cette_empreinte,
    COUNT(*) as nombre_comptes
FROM users
WHERE fingerprint_template IS NOT NULL
GROUP BY MD5(fingerprint_template)
HAVING COUNT(*) > 1;

-- 3. Résultat attendu de la requête 2 :
-- Si VIDE (aucun résultat) → Toutes les empreintes sont différentes ✅
-- Si des lignes apparaissent → PROBLÈME : Plusieurs comptes utilisent la même empreinte ❌
