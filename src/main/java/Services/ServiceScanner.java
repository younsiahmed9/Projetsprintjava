package Services;

import Models.Document;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service de scan OCR pour extraire du texte depuis des images et PDFs
 * Supporte: PNG, JPG, JPEG, PDF
 * Utilise Tesseract OCR et Apache PDFBox
 */
public class ServiceScanner {

    private final Tesseract tesseract;
    private final ServiceDocument documentService;
    private final ExecutorService executorService;
    private static final String TESSDATA_PATH = "tessdata";

    // Langues supportées
    public enum Language {
        FRENCH("fra"),
        ENGLISH("eng"),
        ARABIC("ara"),
        GERMAN("deu"),
        SPANISH("spa"),
        ITALIAN("ita");

        private final String code;

        Language(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public ServiceScanner() {
        this.tesseract = new Tesseract();
        this.documentService = new ServiceDocument();
        this.executorService = Executors.newFixedThreadPool(4);

        configureTesseract();
    }

    /**
     * Configure Tesseract avec les paramètres optimaux
     */
    private void configureTesseract() {
        File tessDataDir = new File(TESSDATA_PATH);
        if (tessDataDir.exists()) {
            tesseract.setDatapath(TESSDATA_PATH);
        } else {
            System.err.println("⚠️ ATTENTION: Dossier tessdata non trouvé. Utilisation du chemin par défaut.");
        }

        // Configuration par défaut: français
        tesseract.setLanguage("fra");

        // Paramètres d'optimisation
        tesseract.setPageSegMode(1); // Automatic page segmentation with OSD
        tesseract.setOcrEngineMode(1); // Neural nets LSTM engine only
    }

    /**
     * Change la langue de reconnaissance OCR
     */
    public void setLanguage(Language language) {
        tesseract.setLanguage(language.getCode());
    }

    /**
     * Change la langue de reconnaissance OCR (code personnalisé)
     */
    public void setLanguage(String languageCode) {
        tesseract.setLanguage(languageCode);
    }

    /**
     * Scan une image et extrait le texte
     */
    public String scanImage(File imageFile) throws TesseractException {
        if (!imageFile.exists()) {
            throw new IllegalArgumentException("Le fichier n'existe pas: " + imageFile.getPath());
        }

        String extension = getFileExtension(imageFile.getName()).toLowerCase();
        if (!extension.matches("png|jpg|jpeg|tiff|bmp|gif")) {
            throw new IllegalArgumentException("Format d'image non supporté: " + extension);
        }

        System.out.println("🔍 Scan de l'image: " + imageFile.getName());
        String text = tesseract.doOCR(imageFile);
        System.out.println("✅ Texte extrait (" + text.length() + " caractères)");

        return text;
    }

    /**
     * Scan un PDF et extrait le texte (méthode hybride)
     * 1. Essaie d'abord d'extraire le texte natif du PDF
     * 2. Si échec ou peu de texte, utilise l'OCR sur chaque page
     */
    public String scanPDF(File pdfFile) throws IOException, TesseractException {
        if (!pdfFile.exists()) {
            throw new IllegalArgumentException("Le fichier n'existe pas: " + pdfFile.getPath());
        }

        System.out.println("📄 Scan du PDF: " + pdfFile.getName());

        try (PDDocument document = PDDocument.load(pdfFile.getAbsoluteFile())) {
            // Tentative 1: Extraction de texte natif
            PDFTextStripper stripper = new PDFTextStripper();
            String nativeText = stripper.getText(document);

            // Si le texte natif est suffisant (> 100 caractères), on l'utilise
            if (nativeText != null && nativeText.trim().length() > 100) {
                System.out.println("✅ Texte natif extrait (" + nativeText.length() + " caractères)");
                return nativeText;
            }

            // Tentative 2: OCR sur chaque page
            System.out.println("📸 Le PDF semble être une image, utilisation de l'OCR...");
            StringBuilder fullText = new StringBuilder();
            PDFRenderer renderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                System.out.println("  📄 Traitement page " + (page + 1) + "/" + document.getNumberOfPages());

                // Rendre la page en image haute résolution
                BufferedImage image = renderer.renderImageWithDPI(page, 300);

                // OCR sur l'image
                String pageText = tesseract.doOCR(image);
                fullText.append(pageText).append("\n\n");
            }

            String result = fullText.toString();
            System.out.println("✅ OCR terminé (" + result.length() + " caractères)");
            return result;
        }
    }

    /**
     * Scan un fichier (image ou PDF) de manière automatique
     */
    public String scanFile(File file) throws IOException, TesseractException {
        String extension = getFileExtension(file.getName()).toLowerCase();

        if (extension.equals("pdf")) {
            return scanPDF(file);
        } else if (extension.matches("png|jpg|jpeg|tiff|bmp|gif")) {
            return scanImage(file);
        } else {
            throw new IllegalArgumentException("Format de fichier non supporté: " + extension);
        }
    }

    /**
     * Scan un fichier de manière asynchrone
     */
    public CompletableFuture<String> scanFileAsync(File file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return scanFile(file);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors du scan: " + e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * Scan plusieurs fichiers en parallèle
     */
    public CompletableFuture<List<ScanResult>> scanFilesAsync(List<File> files) {
        List<CompletableFuture<ScanResult>> futures = new ArrayList<>();

        for (File file : files) {
            CompletableFuture<ScanResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    String text = scanFile(file);
                    return new ScanResult(file, text, true, null);
                } catch (Exception e) {
                    return new ScanResult(file, null, false, e.getMessage());
                }
            }, executorService);

            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    /**
     * Scan un document et met à jour son contenu texte dans la base de données
     */
    public String scanAndUpdateDocument(Document document) throws IOException, TesseractException, SQLException {
        if (document.getCheminFichier() == null || document.getCheminFichier().isEmpty()) {
            throw new IllegalArgumentException("Le document n'a pas de chemin de fichier défini");
        }

        File file = new File(document.getCheminFichier());
        String extractedText = scanFile(file);

        // Mise à jour du document avec le texte extrait
        document.setContenuTexte(extractedText);
        documentService.update(document);

        System.out.println("✅ Document mis à jour avec le texte extrait");
        return extractedText;
    }

    /**
     * Scan un dossier entier et extrait le texte de tous les fichiers supportés
     */
    public List<ScanResult> scanDirectory(String directoryPath) throws IOException {
        Path dirPath = Paths.get(directoryPath);
        if (!Files.isDirectory(dirPath)) {
            throw new IllegalArgumentException("Le chemin n'est pas un dossier: " + directoryPath);
        }

        List<File> filesToScan = new ArrayList<>();
        Files.walk(dirPath)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String name = path.getFileName().toString().toLowerCase();
                    return name.matches(".*\\.(pdf|png|jpg|jpeg|tiff|bmp|gif)$");
                })
                .forEach(path -> filesToScan.add(path.toFile()));

        System.out.println("📁 " + filesToScan.size() + " fichiers trouvés dans le dossier");

        try {
            return scanFilesAsync(filesToScan).get();
        } catch (Exception e) {
            throw new IOException("Erreur lors du scan du dossier", e);
        }
    }

    /**
     * Obtient l'extension d'un fichier
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }

    /**
     * Ferme le service et libère les ressources
     */
    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * Classe représentant le résultat d'un scan
     */
    public static class ScanResult {
        private final File file;
        private final String text;
        private final boolean success;
        private final String errorMessage;

        public ScanResult(File file, String text, boolean success, String errorMessage) {
            this.file = file;
            this.text = text;
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public File getFile() {
            return file;
        }

        public String getText() {
            return text;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String toString() {
            if (success) {
                return "✅ " + file.getName() + " (" + (text != null ? text.length() : 0) + " caractères)";
            } else {
                return "❌ " + file.getName() + " - Erreur: " + errorMessage;
            }
        }
    }
}

