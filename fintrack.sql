-- fintrack.sql
-- Import this file in phpMyAdmin (SQL tab) to create the schema + sample data.

CREATE DATABASE IF NOT EXISTS fintrack CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE fintrack;

DROP TABLE IF EXISTS document;
DROP TABLE IF EXISTS categorie;
DROP TABLE IF EXISTS dossier;

CREATE TABLE dossier (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(100) NOT NULL,
  description TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categorie (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(100) NOT NULL UNIQUE,
  description TEXT
);

CREATE TABLE document (
  id INT PRIMARY KEY AUTO_INCREMENT,
  titre VARCHAR(150) NOT NULL,
  description TEXT,
  file_path VARCHAR(255) NOT NULL,
  uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  dossier_id INT NOT NULL,
  categorie_id INT,
  CONSTRAINT fk_document_dossier
    FOREIGN KEY (dossier_id)
    REFERENCES dossier(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_document_categorie
    FOREIGN KEY (categorie_id)
    REFERENCES categorie(id)
    ON DELETE SET NULL
);

-- Sample data (optional)
INSERT INTO dossier(nom, description) VALUES
('Contrats', 'Dossier contrats clients'),
('Factures', 'Dossier factures & paiements');

INSERT INTO categorie(nom, description) VALUES
('Important', 'Documents importants'),
('À vérifier', 'Documents à vérifier'),
('Archivé', 'Documents archivés');

INSERT INTO document(titre, description, file_path, dossier_id, categorie_id) VALUES
('Contrat Ahmed', 'Contrat 2026', 'C:/docs/contrat_ahmed.pdf', 1, 1),
('Facture Internet', 'Facture Janvier', 'C:/docs/facture_internet.pdf', 2, 2);
