-- Migration: Ajout du système de bannissement
USE fintrack;

ALTER TABLE users ADD COLUMN is_banned TINYINT(1) NOT NULL DEFAULT 0;
