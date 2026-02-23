# Script pour réinitialiser toutes les empreintes
# À exécuter dans phpMyAdmin ou MySQL

USE fintrack;

-- Réinitialiser toutes les empreintes digitales
UPDATE users
SET fingerprint_template = NULL;

-- Vérifier
SELECT id, email, full_name,
       CASE
           WHEN fingerprint_template IS NULL THEN 'Pas d''empreinte'
           ELSE 'A une empreinte'
       END as statut_empreinte
FROM users;
