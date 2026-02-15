-- 📊 Script SQL: Ajouter colonne BUDGET au Document
-- Date: 15/02/2026
-- Rôle: Gérer les budgets financiers des documents

ALTER TABLE document ADD COLUMN budget DECIMAL(12,2) DEFAULT NULL;

-- Vérifier la modification
DESCRIBE document;

-- Exemple de mise à jour
-- UPDATE document SET budget = 5000.00 WHERE id = 1;

