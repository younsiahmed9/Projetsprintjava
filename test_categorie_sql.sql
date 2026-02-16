-- ============================================
-- TEST MANUEL - Catégories avec Code et Couleur
-- ============================================

USE fintrack;

-- 1. Vérifier que les colonnes existent
DESCRIBE categorie;

-- 2. Afficher toutes les catégories avec leurs codes et couleurs
SELECT id, nom, code, couleur, description
FROM categorie
ORDER BY nom;

-- 3. Créer des catégories de test avec codes personnalisés
INSERT INTO categorie (nom, description, code, couleur)
VALUES
    ('Factures', 'Documents de facturation', 'FAC', '#FF5722'),
    ('Juridique', 'Documents juridiques et contrats', 'JUR', '#2196F3'),
    ('Devis', 'Devis et propositions commerciales', 'DEV', '#4CAF50'),
    ('Administratif', 'Documents administratifs', 'ADM', '#9C27B0'),
    ('Comptabilité', 'Documents comptables', 'CPT', '#FFC107');

-- 4. Mettre à jour une catégorie existante
UPDATE categorie
SET code = 'RH', couleur = '#00BCD4'
WHERE nom = 'Ressources Humaines';

-- 5. Vérifier qu'il n'y a pas de NULL
SELECT * FROM categorie
WHERE code IS NULL OR couleur IS NULL;

-- 6. Statistiques par code
SELECT code, COUNT(*) as nb_categories
FROM categorie
GROUP BY code;

-- 7. Afficher les documents avec leur badge catégorie
SELECT
    d.id,
    d.titre,
    c.nom as categorie,
    c.code as badge_code,
    c.couleur as badge_couleur
FROM document d
LEFT JOIN categorie c ON d.categorie_id = c.id
ORDER BY d.uploaded_at DESC
LIMIT 10;

-- 8. Compter les documents par code de catégorie
SELECT
    c.code,
    c.nom,
    c.couleur,
    COUNT(d.id) as nb_documents
FROM categorie c
LEFT JOIN document d ON d.categorie_id = c.id
GROUP BY c.id, c.code, c.nom, c.couleur
ORDER BY nb_documents DESC;

-- 9. Trouver les catégories sans documents
SELECT c.id, c.nom, c.code, c.couleur
FROM categorie c
LEFT JOIN document d ON d.categorie_id = c.id
WHERE d.id IS NULL;

-- 10. Afficher un aperçu visuel (simulation)
SELECT
    CONCAT('📄 ', d.titre, ' [', c.code, ']') as affichage,
    c.couleur as 'Couleur Badge'
FROM document d
LEFT JOIN categorie c ON d.categorie_id = c.id
LIMIT 20;

-- ============================================
-- VÉRIFICATIONS DE SÉCURITÉ
-- ============================================

-- Vérifier que tous les codes sont uniques (recommandé)
SELECT code, COUNT(*) as nb
FROM categorie
GROUP BY code
HAVING COUNT(*) > 1;

-- Vérifier que les couleurs sont au format HEX valide
SELECT id, nom, code, couleur
FROM categorie
WHERE couleur NOT REGEXP '^#[0-9A-F]{6}$';

-- ============================================
-- ROLLBACK SI BESOIN (À UTILISER AVEC PRÉCAUTION)
-- ============================================

-- Pour supprimer les colonnes ajoutées (NE PAS EXÉCUTER EN PRODUCTION)
-- ALTER TABLE categorie DROP COLUMN code, DROP COLUMN couleur;

-- ============================================
-- FIN DU SCRIPT DE TEST
-- ============================================

