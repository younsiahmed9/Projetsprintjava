-- ============================================
-- SCRIPT : SUPPRIMER les colonnes CODE et COULEUR
-- Table : categorie
-- Action : DROP COLUMN (suppression définitive)
-- ============================================

USE fintrack;

-- Sauvegarder la structure actuelle (optionnel)
-- SHOW CREATE TABLE categorie;

-- ============================================
-- SUPPRESSION DES COLONNES
-- ============================================

-- Supprimer la colonne CODE
ALTER TABLE categorie DROP COLUMN code;

-- Supprimer la colonne COULEUR
ALTER TABLE categorie DROP COLUMN couleur;

-- ============================================
-- VÉRIFICATION
-- ============================================

-- Afficher la structure de la table après suppression
DESCRIBE categorie;

-- Afficher les données (sans code ni couleur)
SELECT * FROM categorie;

-- ============================================
-- ROLLBACK (Si besoin de restaurer)
-- ============================================

-- ⚠️ ATTENTION : Après DROP COLUMN, les données sont PERDUES !
-- Pour restaurer, il faudrait recréer les colonnes :
--
-- ALTER TABLE categorie ADD COLUMN code VARCHAR(10) DEFAULT NULL;
-- ALTER TABLE categorie ADD COLUMN couleur VARCHAR(10) DEFAULT NULL;

-- ============================================
-- FIN DU SCRIPT
-- ============================================

