-- Migration: ajouter profile_photo dans users si la table existe déjà
-- Exécuter une seule fois dans phpMyAdmin (base fintrack)

USE fintrack;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS profile_photo VARCHAR(512) NULL AFTER full_name;
