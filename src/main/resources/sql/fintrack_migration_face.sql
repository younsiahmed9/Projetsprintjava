-- Migration pour ajouter la colonne face_template aux utilisateurs existants
-- À exécuter dans phpMyAdmin ou via le client MySQL après avoir créé la base avec fintrack_schema.sql

USE fintrack;

-- Ajouter la colonne face_template si elle n'existe pas
ALTER TABLE users
ADD COLUMN IF NOT EXISTS face_template MEDIUMBLOB NULL
AFTER fingerprint_template;
