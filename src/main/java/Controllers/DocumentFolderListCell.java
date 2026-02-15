package Controllers;

import Models.Dossier;
import Services.ServiceDossier;
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

public class DocumentFolderListCell extends ListCell<Dossier> {

    private HBox container;
    private Label nameLabel;
    private Label descLabel;
    private Button deleteBtn;
    private final ServiceDossier dossierService = new ServiceDossier();

    public DocumentFolderListCell() {
        setPrefHeight(80);
        initializeUI();
    }

    private void initializeUI() {
        nameLabel = new Label();
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #333;");

        descLabel = new Label();
        descLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999;");
        descLabel.setMaxWidth(250);

        deleteBtn = new Button("🗑️");
        deleteBtn.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-size: 12; " +
            "-fx-padding: 5 10; -fx-border-radius: 5; -fx-background-radius: 5;"
        );
        deleteBtn.setPrefWidth(40);
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
            "-fx-background-color: #b91c1c; -fx-text-fill: white; -fx-font-size: 12; " +
            "-fx-padding: 5 10; -fx-border-radius: 5; -fx-background-radius: 5;"
        ));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-size: 12; " +
            "-fx-padding: 5 10; -fx-border-radius: 5; -fx-background-radius: 5;"
        ));

        VBox infoBox = new VBox(3);
        infoBox.getChildren().addAll(nameLabel, descLabel);
        VBox.setVgrow(infoBox, Priority.ALWAYS);

        container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(10, 15, 10, 15));
        container.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        container.getChildren().addAll(infoBox, deleteBtn);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(Dossier dossier, boolean empty) {
        super.updateItem(dossier, empty);

        if (empty || dossier == null) {
            setGraphic(null);
            setText(null);
        } else {
            nameLabel.setText("📁 " + dossier.getNom());

            String desc = dossier.getDescription() != null && !dossier.getDescription().isEmpty()
                    ? dossier.getDescription()
                    : "";
            descLabel.setText(desc);
            descLabel.setVisible(!desc.isEmpty());

            deleteBtn.setOnAction(e -> deleteFolder(dossier));

            setGraphic(container);
        }
    }

    private void deleteFolder(Dossier dossier) {
        if (DeleteConfirmationDialog.showAndWait(
            "Dossier",
            dossier.getNom(),
            "Tous les documents resteront accessibles.")) {

            try {
                dossierService.delete(dossier.getId());
                AlertUtils.showSuccess("Succès", "Dossier supprimé avec succès !");
                getListView().getItems().remove(getItem());
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer : " + e.getMessage());
            }
        }
    }
}

