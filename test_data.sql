-- =============================================
-- Script de Données de Test - FinTrack Dashboard
-- =============================================
-- Ce script crée des données de test pour tester le Dashboard

USE fintrack;

-- =============================================
-- 1. NETTOYER LES DONNÉES EXISTANTES (OPTIONNEL)
-- =============================================
-- ATTENTION: Décommenter ces lignes uniquement si vous voulez SUPPRIMER toutes les données

-- DELETE FROM document;
-- DELETE FROM categorie;
-- DELETE FROM dossier;
-- ALTER TABLE document AUTO_INCREMENT = 1;
-- ALTER TABLE categorie AUTO_INCREMENT = 1;
-- ALTER TABLE dossier AUTO_INCREMENT = 1;

-- =============================================
-- 2. CRÉER DES DOSSIERS
-- =============================================
INSERT INTO dossier (nom, description, created_at) VALUES
('Finances 2026', 'Documents financiers de l''année 2026', NOW()),
('Ressources Humaines', 'Documents RH et contrats employés', NOW()),
('Juridique', 'Contrats et documents juridiques', NOW()),
('Marketing', 'Campagnes et supports marketing', NOW()),
('Projets', 'Documentation des projets en cours', NOW());

-- =============================================
-- 3. CRÉER DES CATÉGORIES
-- =============================================
INSERT INTO categorie (nom, description) VALUES
('Factures', 'Factures fournisseurs et clients'),
('Contrats', 'Contrats de travail et commerciaux'),
('Rapports', 'Rapports mensuels et annuels'),
('Devis', 'Devis et propositions commerciales'),
('Certifications', 'Certificats et attestations'),
('Présentations', 'Supports de présentation'),
('Procédures', 'Procédures et guides internes');

-- =============================================
-- 4. CRÉER DES DOCUMENTS DE TEST
-- =============================================

-- Documents du mois en cours (pour tester "Ce Mois")
INSERT INTO document (titre, description, file_path, dossier_id, categorie_id, uploaded_at, budget) VALUES
('Facture Janvier 2026', 'Facture fournisseur XYZ pour le mois de janvier', 'C:/documents/finances/facture_jan_2026.pdf', 1, 1, NOW(), 1500.00),
('Rapport Mensuel Janvier', 'Rapport financier du mois de janvier 2026', 'C:/documents/finances/rapport_jan_2026.xlsx', 1, 3, NOW(), NULL),
('Contrat Employé - Jean Dupont', 'CDI de Jean Dupont, poste développeur', 'C:/documents/rh/contrat_jean_dupont.pdf', 2, 2, NOW(), NULL),
('Devis Client ABC', 'Proposition commerciale pour le client ABC', 'C:/documents/marketing/devis_abc.pdf', 4, 4, NOW(), 5000.00),
('Certification ISO 9001', 'Certificat ISO 9001 valide jusqu''en 2027', 'C:/documents/juridique/iso_9001.pdf', 3, 5, NOW(), NULL);

-- Documents des mois précédents
INSERT INTO document (titre, description, file_path, dossier_id, categorie_id, uploaded_at, budget) VALUES
('Facture Décembre 2025', 'Facture fournisseur ABC pour décembre', 'C:/documents/finances/facture_dec_2025.pdf', 1, 1, '2025-12-15 10:30:00', 2500.00),
('Rapport Annuel 2025', 'Rapport financier annuel 2025', 'C:/documents/finances/rapport_annuel_2025.pdf', 1, 3, '2025-12-31 16:00:00', NULL),
('Contrat Commercial - Société XYZ', 'Contrat de prestation avec XYZ', 'C:/documents/juridique/contrat_xyz.pdf', 3, 2, '2025-11-20 14:20:00', 10000.00),
('Présentation Produit V2', 'Support de présentation du nouveau produit', 'C:/documents/marketing/presentation_produit_v2.pptx', 4, 6, '2025-10-05 09:15:00', NULL),
('Procédure Onboarding', 'Guide d''intégration nouveaux employés', 'C:/documents/rh/procedure_onboarding.docx', 2, 7, '2025-09-12 11:45:00', NULL);

-- Plus de documents pour avoir des statistiques intéressantes
INSERT INTO document (titre, description, file_path, dossier_id, categorie_id, uploaded_at, budget) VALUES
('Devis Projet Alpha', 'Estimation coûts projet Alpha', 'C:/documents/projets/devis_alpha.xlsx', 5, 4, '2025-08-18 08:30:00', 15000.00),
('Contrat Fournisseur Beta', 'Contrat de fourniture matériel', 'C:/documents/juridique/contrat_beta.pdf', 3, 2, '2025-07-22 15:10:00', 8000.00),
('Facture Maintenance', 'Facture maintenance serveurs Q3', 'C:/documents/finances/facture_maintenance_q3.pdf', 1, 1, '2025-07-01 10:00:00', 3500.00),
('Rapport Projet Gamma', 'Avancement projet Gamma - T3 2025', 'C:/documents/projets/rapport_gamma.pdf', 5, 3, '2025-06-15 14:20:00', NULL),
('Certification Formation', 'Attestation formation sécurité', 'C:/documents/rh/cert_formation_securite.pdf', 2, 5, '2025-05-10 09:00:00', NULL);

-- Documents sans catégorie (pour tester "Non catégorisé")
INSERT INTO document (titre, description, file_path, dossier_id, categorie_id, uploaded_at, budget) VALUES
('Document Divers 1', 'Document non catégorisé', 'C:/documents/divers/doc1.pdf', 1, NULL, '2025-04-20 10:00:00', NULL),
('Document Divers 2', 'Autre document non catégorisé', 'C:/documents/divers/doc2.pdf', 2, NULL, '2025-03-15 11:30:00', NULL),
('Notes Réunion', 'Notes de réunion du 10/03/2025', 'C:/documents/divers/notes_reunion.txt', 5, NULL, '2025-03-10 16:45:00', NULL);

-- =============================================
-- 5. VÉRIFICATIONS
-- =============================================

-- Compter les documents
SELECT 'Total Documents' AS Statistique, COUNT(*) AS Valeur FROM document
UNION ALL
SELECT 'Total Dossiers', COUNT(*) FROM dossier
UNION ALL
SELECT 'Total Catégories', COUNT(*) FROM categorie
UNION ALL
SELECT 'Documents ce mois', COUNT(*) FROM document
WHERE MONTH(uploaded_at) = MONTH(CURRENT_DATE())
AND YEAR(uploaded_at) = YEAR(CURRENT_DATE());

-- Répartition par catégorie
SELECT
    COALESCE(c.nom, 'Non catégorisé') AS Categorie,
    COUNT(d.id) AS NombreDocuments
FROM document d
LEFT JOIN categorie c ON d.categorie_id = c.id
GROUP BY c.nom
ORDER BY NombreDocuments DESC;

-- Répartition par dossier
SELECT
    ds.nom AS Dossier,
    COUNT(d.id) AS NombreDocuments
FROM document d
JOIN dossier ds ON d.dossier_id = ds.id
GROUP BY ds.nom
ORDER BY NombreDocuments DESC;

-- Les 5 documents les plus récents
SELECT
    d.titre AS Titre,
    ds.nom AS Dossier,
    COALESCE(c.nom, 'Non catégorisé') AS Categorie,
    d.uploaded_at AS DateAjout
FROM document d
JOIN dossier ds ON d.dossier_id = ds.id
LEFT JOIN categorie c ON d.categorie_id = c.id
ORDER BY d.uploaded_at DESC
LIMIT 5;

-- =============================================
-- RÉSULTAT ATTENDU
-- =============================================
-- Total Documents : 18
-- Total Dossiers : 5
-- Total Catégories : 7
-- Documents ce mois : 5 (si exécuté en février 2026)
--
-- Répartition par catégorie :
--   - Non catégorisé : 3
--   - Factures : 3
--   - Contrats : 3
--   - Rapports : 3
--   - Devis : 2
--   - Certifications : 2
--   - Présentations : 1
--   - Procédures : 1
--
-- Répartition par dossier :
--   - Finances 2026 : 5
--   - Ressources Humaines : 3
--   - Juridique : 3
--   - Marketing : 2
--   - Projets : 3
-- =============================================

SELECT '✅ Données de test créées avec succès!' AS Statut;

