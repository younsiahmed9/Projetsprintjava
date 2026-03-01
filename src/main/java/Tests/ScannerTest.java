package Tests;

import Services.ServiceScanner;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Tests et exemples d'utilisation du ServiceScanner
 */
public class ScannerTest {

    public static void main(String[] args) {
        ServiceScanner scanner = new ServiceScanner();

        try {
            // Test 1: Scanner une image
            System.out.println("=== TEST 1: Scanner une image ===");
            testScanImage(scanner);

            // Test 2: Scanner un PDF
            System.out.println("\n=== TEST 2: Scanner un PDF ===");
            testScanPDF(scanner);

            // Test 3: Scan asynchrone
            System.out.println("\n=== TEST 3: Scan asynchrone ===");
            testAsyncScan(scanner);

            // Test 4: Scan avec différentes langues
            System.out.println("\n=== TEST 4: Langues multiples ===");
            testMultiLanguage(scanner);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.shutdown();
        }
    }

    private static void testScanImage(ServiceScanner scanner) {
        try {
            File imageFile = new File("test-image.png");
            if (imageFile.exists()) {
                String text = scanner.scanImage(imageFile);
                System.out.println("Texte extrait: " + text.substring(0, Math.min(200, text.length())));
            } else {
                System.out.println("⚠️ Fichier test-image.png non trouvé");
            }
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    private static void testScanPDF(ServiceScanner scanner) {
        try {
            File pdfFile = new File("Medium.pdf");
            if (pdfFile.exists()) {
                String text = scanner.scanPDF(pdfFile);
                System.out.println("Texte extrait du PDF (" + text.length() + " caractères)");
                System.out.println("Extrait: " + text.substring(0, Math.min(300, text.length())));
            } else {
                System.out.println("⚠️ Fichier Medium.pdf non trouvé");
            }
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    private static void testAsyncScan(ServiceScanner scanner) {
        try {
            File file = new File("Medium.pdf");
            if (file.exists()) {
                System.out.println("Démarrage du scan asynchrone...");
                CompletableFuture<String> future = scanner.scanFileAsync(file);

                future.thenAccept(text -> {
                    System.out.println("✅ Scan terminé: " + text.length() + " caractères extraits");
                }).exceptionally(e -> {
                    System.err.println("❌ Erreur: " + e.getMessage());
                    return null;
                });

                // Attendre la fin du traitement
                future.join();
            } else {
                System.out.println("⚠️ Fichier de test non trouvé");
            }
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    private static void testMultiLanguage(ServiceScanner scanner) {
        System.out.println("Test avec français:");
        scanner.setLanguage(ServiceScanner.Language.FRENCH);
        System.out.println("✅ Langue configurée: Français");

        System.out.println("\nTest avec anglais:");
        scanner.setLanguage(ServiceScanner.Language.ENGLISH);
        System.out.println("✅ Langue configurée: Anglais");

        System.out.println("\nTest avec arabe:");
        scanner.setLanguage(ServiceScanner.Language.ARABIC);
        System.out.println("✅ Langue configurée: Arabe");
    }

    /**
     * Exemple d'utilisation complète
     */
    public static void exempleComplet() {
        ServiceScanner scanner = new ServiceScanner();

        try {
            // 1. Scanner un fichier
            File file = new File("document.pdf");
            String texte = scanner.scanFile(file);
            System.out.println("Texte extrait: " + texte);

            // 2. Scanner en mode asynchrone
            CompletableFuture<String> futureScan = scanner.scanFileAsync(file);
            futureScan.thenAccept(text -> {
                System.out.println("Scan terminé: " + text.length() + " caractères");
            });

            // 3. Scanner un dossier entier
            List<ServiceScanner.ScanResult> results = scanner.scanDirectory("documents/");
            for (ServiceScanner.ScanResult result : results) {
                System.out.println(result);
            }

            // 4. Changer de langue
            scanner.setLanguage(ServiceScanner.Language.ENGLISH);
            String textEnglish = scanner.scanFile(new File("english-doc.pdf"));

        } catch (IOException | TesseractException e) {
            e.printStackTrace();
        } finally {
            scanner.shutdown();
        }
    }
}

