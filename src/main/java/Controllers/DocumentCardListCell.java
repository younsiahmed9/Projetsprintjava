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

public class DocumentCardListCell extends ListCell<Document> {

    private VBox container;
    private Label titleLabel;
    private Label descLabel;
    private Label metaLabel;
    private Button deleteBtn;
    private final ServiceDocument docService = new ServiceDocument();

    public DocumentCardListCell() {
        setPrefHeight(100);
        initializeUI();
    }

    private void initializeUI() {
        titleLabel = new Label();
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #1d4ed8;");

        descLabel = new Label();
        descLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666; -fx-wrap-text: true;");
        descLabel.setMaxWidth(300);

        metaLabel = new Label();
        metaLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #999;");

        deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle(
            "-fx-background-color: #dc2626; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 11; " +
            "-fx-padding: 6 12; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );
        deleteBtn.setPrefWidth(110);
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
            "-fx-background-color: #b91c1c; -fx-text-fill: white; -fx-font-size: 11; " +
            "-fx-padding: 6 12; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;"
        ));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-size: 11; " +
            "-fx-padding: 6 12; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;"
        ));

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(titleLabel, descLabel, metaLabel);
        VBox.setVgrow(infoBox, Priority.ALWAYS);

        HBox contentBox = new HBox(10);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        contentBox.getChildren().addAll(infoBox, deleteBtn);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        container = new VBox(5);
        container.setPadding(new Insets(12));
        container.setStyle(
            "-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-border-radius: 12; " +
            "-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 1);"
        );
        container.getChildren().add(contentBox);

        container.setOnMouseEntered(e -> container.setStyle(
            "-fx-border-color: #1d4ed8; -fx-border-width: 1; -fx-border-radius: 12; " +
            "-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(29, 78, 216, 0.15), 10, 0, 0, 3);"
        ));
        container.setOnMouseExited(e -> container.setStyle(
            "-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-border-radius: 12; " +
            "-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 1);"
        ));
    }

    @Override
    protected void updateItem(Document document, boolean empty) {
        super.updateItem(document, empty);

        if (empty || document == null) {
            setGraphic(null);
            setText(null);
        } else {
            titleLabel.setText("📄 " + document.getTitre());

            String desc = document.getDescription() != null && !document.getDescription().isEmpty()
                    ? document.getDescription()
                    : "(Aucune description)";
            descLabel.setText(desc);

            String meta = "Dossier: " + document.getDossier().getNom();
            if (document.getCategorie() != null) {
                meta += " • Catégorie: " + document.getCategorie().getNom();
            }
            metaLabel.setText(meta);

            deleteBtn.setOnAction(e -> deleteDocument(document));

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

