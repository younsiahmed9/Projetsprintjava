package Controllers;

import Controllers.Dialogs.CrudDialogManager;
import Models.Document;
import Services.ServiceDocument;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class DocumentDetailController {

    @FXML
    private Label lblTitle;
    @FXML
    private Label lblPath;
    @FXML
    private Label lblDescription;
    @FXML
    private Label lblCategory;
    @FXML
    private Label lblFolder;
    @FXML
    private Label lblCreatedDate;
    @FXML
    private Label lblFileType;
    @FXML
    private Label lblFileSize;
    @FXML
    private Label lblFilePath;
    @FXML
    private Label lblBudget;
    @FXML
    private Label lblBudgetStatus;
    @FXML
    private Label lblStatus;
    @FXML
    private Button btnOpenFile;
    @FXML
    private Button btnOpenFolder;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnClose;

    // Nouveaux boutons (présents dans document_detail_simple.fxml; peuvent être
    // absents dans d’autres variantes)
    @FXML
    private Button btnTranslate;
    @FXML
    private Button btnShare;

    private Document currentDocument;
    private ServiceDocument docService = new ServiceDocument();
    private CrudDialogManager dialogManager = new CrudDialogManager();
    private Stage currentStage;
    private Runnable onDocumentUpdated;

    @FXML
    public void initialize() {
        btnClose.setOnAction(e -> closeWindow());
        btnOpenFile.setOnAction(e -> openFile());
        btnOpenFolder.setOnAction(e -> openFileFolder());
        btnEdit.setOnAction(e -> editDocument());

        // Les FXML ne contiennent pas forcément ces boutons: on protège avec
        // null-check.
        if (btnTranslate != null) {
            btnTranslate.setOnAction(e -> translateDocument());
        }
        if (btnShare != null) {
            btnShare.setOnAction(e -> shareDocument());
        }
    }

    public void showDocument(Document doc) {
        this.currentDocument = doc;
        if (doc == null) {
            AlertUtils.showError("Erreur", "Document null");
            return;
        }
        loadDocumentDetails();
    }

    private void loadDocumentDetails() {
        try {
            if (currentDocument == null)
                return;

            lblTitle.setText("📄 " + currentDocument.getTitre());
            lblPath.setText("Chemin: " + currentDocument.getFilePath());

            lblDescription.setText(
                    currentDocument.getDescription() != null ? currentDocument.getDescription() : "Pas de description");

            String categoryName = currentDocument.getCategorie() != null ? currentDocument.getCategorie().getNom()
                    : "Non assignée";
            lblCategory.setText(categoryName);

            String folderName = currentDocument.getDossier() != null ? currentDocument.getDossier().getNom()
                    : "Inconnu";
            lblFolder.setText(folderName);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String dateStr = currentDocument.getUploadedAt().format(dtf);
            lblCreatedDate.setText(dateStr);

            File file = new File(currentDocument.getFilePath());
            lblFileType.setText(getFileExtension(currentDocument.getFilePath()).toUpperCase());
            lblFileSize.setText(formatFileSize(file.length()));
            lblFilePath.setText(currentDocument.getFilePath());

            displayFinancialData();

            updateStatus("Document chargé: " + currentDocument.getTitre());
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Erreur lors du chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayFinancialData() {
        // TND use 3 decimal places
        DecimalFormat df = new DecimalFormat("#,##0.000 TND");

        double montant = currentDocument.getMontant();
        lblBudget.setText(df.format(montant));

        if (montant > 0) {
            lblBudgetStatus.setText("✓ Alloué");
            lblBudgetStatus.setStyle("-fx-padding: 5 10; -fx-background-color: #dcfce7; " +
                    "-fx-text-fill: #166534; -fx-border-radius: 5; -fx-font-weight: bold;");
        } else if (montant == 0) {
            lblBudgetStatus.setText("○ Zéro");
            lblBudgetStatus.setStyle("-fx-padding: 5 10; -fx-background-color: #f3f4f6; " +
                    "-fx-text-fill: #6b7280; -fx-border-radius: 5;");
        } else {
            lblBudgetStatus.setText("✗ Négatif");
            lblBudgetStatus.setStyle("-fx-padding: 5 10; -fx-background-color: #fee2e2; " +
                    "-fx-text-fill: #991b1b; -fx-border-radius: 5; -fx-font-weight: bold;");
        }
    }

    private void editDocument() {
        try {
            dialogManager.showDocumentDialog(currentDocument, true).ifPresent(updatedDoc -> {
                try {
                    docService.update(updatedDoc);
                    currentDocument = updatedDoc;
                    loadDocumentDetails();
                    AlertUtils.showSuccess("Succès", "Document modifié avec succès !");
                    updateStatus("Document mis à jour");

                    if (onDocumentUpdated != null) {
                        onDocumentUpdated.run();
                    }
                } catch (Exception e) {
                    AlertUtils.showError("Erreur", "Impossible de modifier le document: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Erreur lors de l'édition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void translateDocument() {
        if (currentDocument == null)
            return;
        Controllers.Dialogs.TranslateActionDialog.showTranslateDialog(currentDocument);
    }

    private void shareDocument() {
        if (currentDocument == null)
            return;
        Controllers.Dialogs.ShareActionDialog.showShareDialog(currentDocument);
    }

    private void openFile() {
        try {
            File file = new File(currentDocument.getFilePath());
            if (file.exists()) {
                Desktop.getDesktop().open(file);
                updateStatus("Ouverture du fichier...");
            } else {
                AlertUtils.showError("Erreur", "Le fichier n'existe pas");
            }
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Impossible d'ouvrir: " + e.getMessage());
        }
    }

    private void openFileFolder() {
        try {
            File file = new File(currentDocument.getFilePath());
            if (file.exists()) {
                Desktop.getDesktop().open(file.getParentFile());
                updateStatus("Dossier ouvert...");
            } else {
                AlertUtils.showError("Erreur", "Le fichier n'existe pas");
            }
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Impossible d'ouvrir: " + e.getMessage());
        }
    }

    private void closeWindow() {
        if (currentStage != null) {
            currentStage.close();
        }
    }

    private void updateStatus(String message) {
        lblStatus.setText(message);
    }

    private String getFileExtension(String filePath) {
        int lastIndex = filePath.lastIndexOf('.');
        return lastIndex > 0 ? filePath.substring(lastIndex + 1) : "UNKNOWN";
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0)
            return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    public void setOnDocumentUpdated(Runnable callback) {
        this.onDocumentUpdated = callback;
    }
}
