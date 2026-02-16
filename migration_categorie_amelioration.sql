-- ============================================
-- Migration : Amélioration de la table categorie
-- Ajout de "code" et "couleur"
-- ============================================

USE fintrack;

-- Ajouter les nouvelles colonnes
ALTER TABLE categorie
ADD COLUMN code VARCHAR(10) NOT NULL DEFAULT 'CAT',
ADD COLUMN couleur VARCHAR(10) NOT NULL DEFAULT '#1E88E5';

-- Mettre à jour les codes par défaut pour les catégories existantes
-- (chaque catégorie aura un code unique)
UPDATE categorie SET code = CONCAT('CAT', LPAD(id, 3, '0'));

-- Ajouter des couleurs distinctes aux catégories existantes
UPDATE categorie SET couleur =
    CASE
        WHEN id % 10 = 0 THEN '#1E88E5'  -- Bleu
        WHEN id % 10 = 1 THEN '#43A047'  -- Vert
        WHEN id % 10 = 2 THEN '#FB8C00'  -- Orange
        WHEN id % 10 = 3 THEN '#E53935'  -- Rouge
        WHEN id % 10 = 4 THEN '#8E24AA'  -- Violet
        WHEN id % 10 = 5 THEN '#00ACC1'  -- Cyan
        WHEN id % 10 = 6 THEN '#FDD835'  -- Jaune
        WHEN id % 10 = 7 THEN '#F4511E'  -- Orange foncé
        WHEN id % 10 = 8 THEN '#7CB342'  -- Vert clair
        ELSE '#546E7A'                   -- Gris bleuté
    END;

-- Vérification
SELECT id, nom, code, couleur, description FROM categorie;

-- ✅ Migration terminée avec succès

