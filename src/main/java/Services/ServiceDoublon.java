package Services;

import Models.Document;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de détection de documents en doublon
 * Utilise plusieurs algorithmes de similarité
 */
public class ServiceDoublon {

    private final ServiceDocument documentService;

    public ServiceDoublon() {
        this.documentService = new ServiceDocument();
    }

    /**
     * Détecte tous les doublons potentiels dans la base
     */
    public List<DoublonGroup> detectAllDoublons(double seuil) throws SQLException {
        List<Document> allDocuments = documentService.findAll();
        return detectDoublons(allDocuments, seuil);
    }

    /**
     * Détecte les doublons dans une liste de documents
     */
    public List<DoublonGroup> detectDoublons(List<Document> documents, double seuil) {
        List<DoublonGroup> doublonGroups = new ArrayList<>();
        Set<Integer> processed = new HashSet<>();

        for (int i = 0; i < documents.size(); i++) {
            if (processed.contains(i))
                continue;

            Document doc1 = documents.get(i);
            List<DoublonPair> similars = new ArrayList<>();

            for (int j = i + 1; j < documents.size(); j++) {
                if (processed.contains(j))
                    continue;

                Document doc2 = documents.get(j);
                double similarity = calculateSimilarity(doc1, doc2);

                if (similarity >= seuil) {
                    similars.add(new DoublonPair(doc1, doc2, similarity));
                    processed.add(j);
                }
            }

            if (!similars.isEmpty()) {
                processed.add(i);
                doublonGroups.add(new DoublonGroup(doc1, similars));
            }
        }

        return doublonGroups;
    }

    /**
     * Trouve les doublons potentiels d'un document spécifique
     */
    public List<DoublonPair> findDoublonsOf(Document document, double seuil) throws SQLException {
        List<Document> allDocuments = documentService.findAll();
        List<DoublonPair> doublons = new ArrayList<>();

        for (Document other : allDocuments) {
            if (other.getId() == document.getId())
                continue;

            double similarity = calculateSimilarity(document, other);
            if (similarity >= seuil) {
                doublons.add(new DoublonPair(document, other, similarity));
            }
        }

        return doublons.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                .collect(Collectors.toList());
    }

    /**
     * Calcule la similarité entre deux documents (score de 0 à 1)
     * Combine plusieurs métriques:
     * - Similarité du titre (40%)
     * - Similarité du contenu texte (40%)
     * - Même dossier (10%)
     * - Même catégorie (10%)
     */
    public double calculateSimilarity(Document doc1, Document doc2) {
        double titleSim = calculateStringSimilarity(doc1.getTitre(), doc2.getTitre());
        double contentSim = calculateContentSimilarity(doc1.getContenuTexte(), doc2.getContenuTexte());
        double folderSim = sameFolderBonus(doc1, doc2);
        double categorySim = sameCategoryBonus(doc1, doc2);

        double totalScore = (titleSim * 0.4) + (contentSim * 0.4) + (folderSim * 0.1) + (categorySim * 0.1);

        // Règles métier pour booster la similarité en cas de correspondance forte :

        // 1. Si le titre est quasi-identique, on garantit que ça dépasse le seuil de
        // base (0.70)
        if (titleSim >= 0.95) {
            totalScore = Math.max(totalScore, 0.75);
        }

        // 2. Si le titre ET le dossier sont identiques, c'est très probablement un
        // doublon
        if (titleSim >= 0.95 && folderSim == 1.0) {
            totalScore = Math.max(totalScore, 0.85);
        }

        // 3. Si le texte complet est identique (et non vide), gros boost
        if (contentSim == 1.0 && doc1.getContenuTexte() != null && !doc1.getContenuTexte().trim().isEmpty()) {
            totalScore = Math.max(totalScore, 0.80);
        }

        // 4. Si le montant est identique (et supérieur à 0), c'est un indice très fort
        // de doublon (surtout si le nom se ressemble un peu)
        if (doc1.getMontant() > 0 && doc1.getMontant() == doc2.getMontant()) {
            totalScore = Math.max(totalScore + 0.30, 0.85);
        }

        return Math.min(totalScore, 1.0);
    }

    /**
     * Calcule la similarité entre deux chaînes en utilisant la distance de
     * Levenshtein normalisée
     */
    private double calculateStringSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null)
            return 0.0;
        if (s1.isEmpty() && s2.isEmpty())
            return 1.0;

        String str1 = s1.toLowerCase().trim();
        String str2 = s2.toLowerCase().trim();

        int distance = levenshteinDistance(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());

        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Calcule la similarité du contenu texte (Jaccard similarity sur les mots)
     */
    private double calculateContentSimilarity(String content1, String content2) {
        if (content1 == null || content2 == null)
            return 0.0;
        if (content1.isEmpty() && content2.isEmpty())
            return 1.0;

        Set<String> words1 = tokenize(content1);
        Set<String> words2 = tokenize(content2);

        if (words1.isEmpty() && words2.isEmpty())
            return 1.0;

        // Jaccard similarity
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        return (double) intersection.size() / union.size();
    }

    /**
     * Tokenize un texte en ensemble de mots
     */
    private Set<String> tokenize(String text) {
        if (text == null)
            return new HashSet<>();

        return Arrays.stream(text.toLowerCase()
                .replaceAll("[^a-zàâäéèêëïîôöùûüÿçæœ0-9\\s]", " ")
                .split("\\s+"))
                .filter(word -> word.length() > 2)
                .collect(Collectors.toSet());
    }

    /**
     * Bonus si les documents sont dans le même dossier
     */
    private double sameFolderBonus(Document doc1, Document doc2) {
        if (doc1.getDossier() == null || doc2.getDossier() == null)
            return 0.0;
        return doc1.getDossier().getId() == doc2.getDossier().getId() ? 1.0 : 0.0;
    }

    /**
     * Bonus si les documents ont la même catégorie
     */
    private double sameCategoryBonus(Document doc1, Document doc2) {
        if (doc1.getCategorie() == null || doc2.getCategorie() == null)
            return 0.0;
        return doc1.getCategorie().getId() == doc2.getCategorie().getId() ? 1.0 : 0.0;
    }

    /**
     * Calcule la distance de Levenshtein entre deux chaînes
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1, // deletion
                        dp[i][j - 1] + 1), // insertion
                        dp[i - 1][j - 1] + cost // substitution
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Génère un rapport de doublons
     */
    public DoublonReport generateReport(double seuil) throws SQLException {
        List<DoublonGroup> groups = detectAllDoublons(seuil);

        int totalDoublons = groups.stream()
                .mapToInt(g -> g.getSimilars().size() + 1)
                .sum();

        int highConfidence = groups.stream()
                .mapToInt(g -> (int) g.getSimilars().stream()
                        .filter(p -> p.getSimilarity() >= 0.9)
                        .count())
                .sum();

        return new DoublonReport(groups, totalDoublons, highConfidence, seuil);
    }

    /**
     * Classe représentant une paire de doublons
     */
    public static class DoublonPair {
        private final Document doc1;
        private final Document doc2;
        private final double similarity;

        public DoublonPair(Document doc1, Document doc2, double similarity) {
            this.doc1 = doc1;
            this.doc2 = doc2;
            this.similarity = similarity;
        }

        public Document getDoc1() {
            return doc1;
        }

        public Document getDoc2() {
            return doc2;
        }

        public double getSimilarity() {
            return similarity;
        }

        public String getSimilarityPercent() {
            return String.format("%.1f%%", similarity * 100);
        }

        @Override
        public String toString() {
            return String.format("'%s' ↔ '%s' (%.1f%%)",
                    doc1.getTitre(), doc2.getTitre(), similarity * 100);
        }
    }

    /**
     * Classe représentant un groupe de doublons
     */
    public static class DoublonGroup {
        private final Document original;
        private final List<DoublonPair> similars;

        public DoublonGroup(Document original, List<DoublonPair> similars) {
            this.original = original;
            this.similars = similars;
        }

        public Document getOriginal() {
            return original;
        }

        public List<DoublonPair> getSimilars() {
            return similars;
        }

        public int getTotalDocuments() {
            return similars.size() + 1;
        }

        @Override
        public String toString() {
            return String.format("Groupe: '%s' avec %d doublon(s)",
                    original.getTitre(), similars.size());
        }
    }

    /**
     * Classe représentant un rapport de doublons
     */
    public static class DoublonReport {
        private final List<DoublonGroup> groups;
        private final int totalDoublons;
        private final int highConfidence;
        private final double seuil;

        public DoublonReport(List<DoublonGroup> groups, int totalDoublons, int highConfidence, double seuil) {
            this.groups = groups;
            this.totalDoublons = totalDoublons;
            this.highConfidence = highConfidence;
            this.seuil = seuil;
        }

        public List<DoublonGroup> getGroups() {
            return groups;
        }

        public int getTotalDoublons() {
            return totalDoublons;
        }

        public int getHighConfidence() {
            return highConfidence;
        }

        public double getSeuil() {
            return seuil;
        }

        public int getGroupCount() {
            return groups.size();
        }

        @Override
        public String toString() {
            return String.format("Rapport: %d groupe(s), %d doublon(s) total, %d haute confiance (seuil: %.1f%%)",
                    groups.size(), totalDoublons, highConfidence, seuil * 100);
        }
    }
}
