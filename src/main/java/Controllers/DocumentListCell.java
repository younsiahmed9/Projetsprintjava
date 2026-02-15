package Controllers;

import Models.Document;
import Services.ServiceDocument;
import Controllers.Dialogs.DeleteConfirmationDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class DocumentListCell extends ListCell<Document> {
    private final Label lblTitre;
    private final Label lblDossier;
    private final Label lblPath;
    private final Label lblUploaded;
    private final Button deleteBtn;
    private HBox container;
    private final ServiceDocument docService = new ServiceDocument();

    public DocumentListCell() {
        lblTitre = new Label();
        lblTitre.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        lblDossier = new Label();
        lblDossier.setStyle("-fx-font-size: 12; -fx-text-fill: #1d4ed8;");

        lblPath = new Label();
        lblPath.setStyle("-fx-font-size: 11; -fx-text-fill: #6b7280;");
        lblPath.setWrapText(true);

        lblUploaded = new Label();
        lblUploaded.setStyle("-fx-font-size: 11; -fx-text-fill: #9ca3af;");

        deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-size: 11; " +
            "-fx-padding: 6 12; -fx-border-radius: 5; -fx-background-radius: 5;"
        );
        deleteBtn.setPrefWidth(110);
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
            "-fx-background-color: #b91c1c; -fx-text-fill: white; -fx-font-size: 11; " +
            "-fx-padding: 6 12; -fx-border-radius: 5; -fx-background-radius: 5;"
        ));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-size: 11; " +
            "-fx-padding: 6 12; -fx-border-radius: 5; -fx-background-radius: 5;"
        ));

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(lblTitre, lblDossier, lblPath, lblUploaded);
        VBox.setVgrow(infoBox, Priority.ALWAYS);

        container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        container.getChildren().addAll(infoBox, deleteBtn);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(Document item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            lblTitre.setText(item.getTitre());
            lblDossier.setText("📁 " + (item.getDossier() != null ? item.getDossier().getNom() : "N/A"));
            lblPath.setText("📄 " + item.getFilePath());

            if (item.getUploadedAt() != null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                lblUploaded.setText("Uploadé le: " + item.getUploadedAt().format(fmt));
            } else {
                lblUploaded.setText("");
            }

            deleteBtn.setOnAction(e -> deleteDocument(item));
            setGraphic(container);
        }
    }

    private void deleteDocument(Document document) {
        if (DeleteConfirmationDialog.showAndWait(
            "Document",
            document.getTitre(),
            "Le fichier \"" + document.getFilePath() + "\" sera supprimé.")) {

            try {
                docService.delete(document.getId());
                AlertUtils.showSuccess("Succès", "Document supprimé avec succès !");
                getListView().getItems().remove(getItem());
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer le document : " + e.getMessage());
            }
        }
    }
}

