package Controllers;

import Services.ServiceScanner;
import Models.Document;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Guide d'intégration pour utiliser le ServiceScanner dans l'interface
 */
public class IntegrationGuide {

    private ServiceScanner scanner;
    private Label lblStatus;
    private ProgressBar progressBar;
    private Button btnScan;

    public IntegrationGuide() {
        this.scanner = new ServiceScanner();
    }

    /**
     * EXEMPLE 1: Scanner un fichier sélectionné par l'utilisateur
     */
    public void scannerFichierSelectionne() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier à scanner");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images et PDFs", "*.pdf", "*.png", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("PDFs", "*.pdf")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            scannerFichierAsync(file);
        }
    }

    /**
     * EXEMPLE 2: Scanner un fichier de manière asynchrone avec indicateur de progression
     */
    public void scannerFichierAsync(File file) {
        Task<String> scanTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Scan en cours de " + file.getName() + "...");
                updateProgress(-1, 1); // Indéterminé

                return scanner.scanFile(file);
            }
        };

        scanTask.setOnSucceeded(event -> {
            String texte = scanTask.getValue();
            Platform.runLater(() -> {
                lblStatus.setText("✅ Scan terminé: " + texte.length() + " caractères extraits");
                progressBar.setVisible(false);

                // Afficher le résultat
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Résultat du Scan");
                alert.setHeaderText("Texte extrait avec succès");
                alert.setContentText(texte.substring(0, Math.min(500, texte.length())) + "...");
                alert.showAndWait();
            });
        });

        scanTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                lblStatus.setText("❌ Erreur lors du scan");
                progressBar.setVisible(false);

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Impossible de scanner le fichier");
                alert.setContentText(scanTask.getException().getMessage());
                alert.showAndWait();
            });
        });

        // Lier les contrôles à la tâche
        lblStatus.textProperty().bind(scanTask.messageProperty());
        progressBar.progressProperty().bind(scanTask.progressProperty());
        progressBar.setVisible(true);

        // Lancer la tâche dans un thread séparé
        Thread scanThread = new Thread(scanTask);
        scanThread.setDaemon(true);
        scanThread.start();
    }

    /**
     * EXEMPLE 3: Scanner un document et mettre à jour la base de données
     */
    public void scannerEtMettreAJourDocument(Document document) {
        Task<String> scanTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Extraction du texte du document...");
                return scanner.scanAndUpdateDocument(document);
            }
        };

        scanTask.setOnSucceeded(event -> {
            String texte = scanTask.getValue();
            Platform.runLater(() -> {
                AlertUtils.showSuccess("Succès",
                    "Document mis à jour avec " + texte.length() + " caractères extraits");
            });
        });

        scanTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                AlertUtils.showError("Erreur",
                    "Impossible de scanner le document: " + scanTask.getException().getMessage());
            });
        });

        new Thread(scanTask).start();
    }

    /**
     * EXEMPLE 4: Changer la langue de reconnaissance OCR
     */
    public void changerLangueOCR(ServiceScanner.Language langue) {
        scanner.setLanguage(langue);
        AlertUtils.showInfo("Langue OCR",
            "Langue de reconnaissance changée: " + langue.name());
    }

    /**
     * EXEMPLE 5: Intégration complète dans un bouton
     */
    public void setupScanButton(Button btnScan, Document document) {
        btnScan.setOnAction(e -> {
            if (document.getCheminFichier() == null || document.getCheminFichier().isEmpty()) {
                AlertUtils.showWarning("Attention", "Ce document n'a pas de fichier associé");
                return;
            }

            File file = new File(document.getCheminFichier());
            if (!file.exists()) {
                AlertUtils.showError("Erreur", "Le fichier n'existe pas: " + file.getPath());
                return;
            }

            // Désactiver le bouton pendant le scan
            btnScan.setDisable(true);
            btnScan.setText("⏳ Scan en cours...");

            Task<String> scanTask = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return scanner.scanFile(file);
                }
            };

            scanTask.setOnSucceeded(event -> {
                String texte = scanTask.getValue();
                document.setContenuTexte(texte);

                Platform.runLater(() -> {
                    btnScan.setDisable(false);
                    btnScan.setText("🔍 Scanner");
                    AlertUtils.showSuccess("Succès",
                        texte.length() + " caractères extraits");
                });
            });

            scanTask.setOnFailed(event -> {
                Platform.runLater(() -> {
                    btnScan.setDisable(false);
                    btnScan.setText("🔍 Scanner");
                    AlertUtils.showError("Erreur",
                        scanTask.getException().getMessage());
                });
            });

            new Thread(scanTask).start();
        });
    }

    /**
     * Fermer le scanner à la fin
     */
    public void shutdown() {
        if (scanner != null) {
            scanner.shutdown();
        }
    }
}

