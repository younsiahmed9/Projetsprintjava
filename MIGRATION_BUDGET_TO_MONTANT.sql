-- Script de migration : Remplacer BUDGET par MONTANT (obligatoire)
-- Date: 16/02/2026

-- Étape 1 : Supprimer la contrainte de clé étrangère (si elle existe)
-- Supprimer la colonne budget
ALTER TABLE document DROP COLUMN budget;

-- Étape 2 : Ajouter la colonne montant obligatoire
ALTER TABLE document ADD COLUMN montant DECIMAL(10,2) NOT NULL DEFAULT 0.00;

-- Étape 3 : Vérifier la modification
DESCRIBE document;

-- ✅ Résultat attendu : La colonne montant doit être presente et NOT NULL

