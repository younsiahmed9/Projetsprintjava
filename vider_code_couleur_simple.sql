-- ============================================
-- SCRIPT SIMPLE : Vider CODE et COULEUR
-- ============================================

USE fintrack;

-- Vider tous les codes
UPDATE categorie SET code = NULL;

-- Vider toutes les couleurs
UPDATE categorie SET couleur = NULL;

-- Vérifier le résultat
SELECT id, nom, code, couleur FROM categorie;

