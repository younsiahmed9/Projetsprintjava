package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import net.sourceforge.tess4j.Tesseract;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service professionnel d'OCR et d'extraction de données.
 */
public class OcrService {

    private final Tesseract tesseract;

    public OcrService() {
        this.tesseract = new Tesseract();
        // Optionnel : Configuration du chemin tessdata si présent
        File tessDataFolder = new File("tessdata");
        if (tessDataFolder.exists()) {
            this.tesseract.setDatapath(tessDataFolder.getAbsolutePath());
        }
    }

    /**
     * Extrait les données d'un document (PDF ou Image).
     */
    public Map<String, Object> extractData(File file) {
        String text = "";
        if (file.getName().toLowerCase().endsWith(".pdf")) {
            text = extractTextFromPdf(file);
        } else {
            text = extractTextFromImage(file);
        }
        return parseInvoiceText(text);
    }

    private String extractTextFromPdf(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            System.err.println("Erreur lecture PDF: " + e.getMessage());
            return "";
        }
    }

    private String extractTextFromImage(File file) {
        try {
            return tesseract.doOCR(file);
        } catch (Exception e) {
            System.err.println("Erreur OCR Image: " + e.getMessage());
            return "";
        }
    }

    /**
     * Parseur intelligent pour extraire les champs clés d'une facture.
     */
    private Map<String, Object> parseInvoiceText(String text) {
        Map<String, Object> results = new HashMap<>();
        if (text == null || text.isEmpty())
            return results;

        // 1. Recherche du Montant
        // Patterns: "Total TTC: 250.00", "Montant: 123.45", "Total: 100 TND"
        Pattern amountPattern = Pattern.compile("(?i)(?:total|montant|somme).*?(\\d+(?:[.,]\\d{2})?)");
        Matcher amountMatcher = amountPattern.matcher(text);
        if (amountMatcher.find()) {
            results.put("montant", amountMatcher.group(1).replace(",", "."));
        }

        // 2. Recherche des Dates (Format DD/MM/YYYY ou DD-MM-YYYY)

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // On cherche "Date de facture"
        Pattern dateFacturePattern = Pattern.compile("(?i)(?:date.*?facture).*?(\\d{2}[/\\-]\\d{2}[/\\-]\\d{4})");
        Matcher dfMatcher = dateFacturePattern.matcher(text);
        if (dfMatcher.find()) {
            try {
                results.put("date_facture", LocalDate.parse(dfMatcher.group(1).replace("-", "/"), formatter));
            } catch (Exception ignored) {
            }
        }

        // On cherche "Date limite"
        Pattern dateLimitePattern = Pattern
                .compile("(?i)(?:date.*?limite|echeance|paye.*?avant).*?(\\d{2}[/\\-]\\d{2}[/\\-]\\d{4})");
        Matcher dlMatcher = dateLimitePattern.matcher(text);
        if (dlMatcher.find()) {
            try {
                results.put("date_limite", LocalDate.parse(dlMatcher.group(1).replace("-", "/"), formatter));
            } catch (Exception ignored) {
            }
        }

        // 3. Recherche du Titre / Fournisseur
        Pattern supplierPattern = Pattern.compile("(?i)(?:fournisseur|societe|nom):?\\s*(.*)");
        Matcher sMatcher = supplierPattern.matcher(text);
        if (sMatcher.find()) {
            results.put("titre", "Facture " + sMatcher.group(1).trim());
        } else {
            // Fallback: première ligne non vide
            String[] lines = text.split("\\n");
            for (String line : lines) {
                if (!line.trim().isEmpty() && line.length() > 3) {
                    results.put("titre", line.trim());
                    break;
                }
            }
        }

        return results;
    }
}
