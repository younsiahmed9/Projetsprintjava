-- Migration SQL pour ajouter la catégorie
-- Exécutez ces commandes si vous avez déjà une base de données fintrack

USE fintrack;

-- 1. Créer la table categorie
CREATE TABLE IF NOT EXISTS categorie (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(100) NOT NULL UNIQUE,
  description TEXT
);

-- 2. Ajouter la colonne categorie_id à la table document (si elle n'existe pas)
ALTER TABLE document ADD COLUMN categorie_id INT;

-- 3. Ajouter la contrainte FK
ALTER TABLE document ADD CONSTRAINT fk_document_categorie
  FOREIGN KEY (categorie_id) REFERENCES categorie(id) ON DELETE SET NULL;

-- 4. Insérer les catégories d'exemple
INSERT INTO categorie(nom, description) VALUES
('Important', 'Documents importants et prioritaires'),
('À vérifier', 'Documents en attente de vérification'),
('Archivé', 'Documents archivés'),
('Personnel', 'Documents personnels'),
('Professionnel', 'Documents professionnels');

-- Vérifier que tout est correct
SELECT 'Catégories créées' AS message;
SELECT COUNT(*) AS nombre_categories FROM categorie;
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'document' AND COLUMN_NAME = 'categorie_id';

