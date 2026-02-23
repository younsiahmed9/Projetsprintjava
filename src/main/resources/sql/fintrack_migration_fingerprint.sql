-- Migration pour ajouter la colonne fingerprint_template aux utilisateurs existants
-- A exécuter dans phpMyAdmin ou via le client MySQL après avoir créé la base avec fintrack_schema.sql

USE fintrack;

-- Ajouter la colonne fingerprint_template si elle n'existe pas
ALTER TABLE users
ADD COLUMN IF NOT EXISTS fingerprint_template MEDIUMBLOB NULL
AFTER profile_photo;
