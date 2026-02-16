package Controllers;

import Models.Document;
import Services.ServiceDocument;
// import Controllers.Dialogs.DeleteConfirmationDialog;
import Controllers.Dialogs.CrudDialogManager;
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
    private Label amountLabel;
    private Button editBtn;
    private Button deleteBtn;
    private final ServiceDocument docService = new ServiceDocument();

    public DocumentCardListCell() {
        setPrefHeight(110);
        initializeUI();
    }

    private void initializeUI() {
        titleLabel = new Label();
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #1d4ed8;");

        descLabel = new Label();
        descLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666; -fx-wrap-text: true;");
        descLabel.setMaxWidth(420);

        metaLabel = new Label();
        metaLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #999;");

        amountLabel = new Label();
        amountLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #111827; -fx-font-weight: bold;");

        editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("btn-modify");
        editBtn.setStyle("-fx-font-size: 12; -fx-min-width: 110; -fx-pref-width: 110; -fx-max-width: 110; -fx-text-overrun: clip;");

        deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setStyle("-fx-font-size: 12; -fx-min-width: 110; -fx-pref-width: 110; -fx-max-width: 110; -fx-text-overrun: clip;");

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(titleLabel, descLabel, metaLabel);
        VBox.setVgrow(infoBox, Priority.ALWAYS);

        HBox actionsRow = new HBox(10, editBtn, deleteBtn);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);

        VBox rightBox = new VBox(6, amountLabel, actionsRow);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        HBox contentBox = new HBox(12, infoBox, rightBox);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        container = new VBox(6);
        container.setPadding(new Insets(12));
        container.getStyleClass().add("account-card");
        container.getChildren().add(contentBox);
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

            amountLabel.setText(String.format("Montant: %.2f €", document.getMontant()));

            editBtn.setOnAction(e -> editDocument(document));
            deleteBtn.setOnAction(e -> deleteDocument(document));

            setGraphic(container);
        }
    }

    private void editDocument(Document document) {
        CrudDialogManager dialogManager = new CrudDialogManager();
        dialogManager.showDocumentDialog(document, true).ifPresent(updated -> {
            try {
                docService.update(updated);
                getListView().getItems().set(getIndex(), updated);
                getListView().refresh();
                AlertUtils.showSuccess("Succès", "Document modifié avec succès !");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de modifier le document : " + e.getMessage());
            }
        });
    }

    private void deleteDocument(Document document) {
    }
}
