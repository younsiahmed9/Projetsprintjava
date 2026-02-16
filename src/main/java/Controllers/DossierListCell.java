package Controllers;

import Models.Dossier;
import Services.ServiceDossier;
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

public class DossierListCell extends ListCell<Dossier> {
    private final Label lblNom;
    private final Label lblDesc;
    private final Label lblCreated;
    private final Button deleteBtn;
    private HBox container;
    private final ServiceDossier dossierService = new ServiceDossier();

    public DossierListCell() {
        lblNom = new Label();
        lblNom.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        lblDesc = new Label();
        lblDesc.setStyle("-fx-font-size: 12; -fx-text-fill: #6b7280;");
        lblDesc.setWrapText(true);

        lblCreated = new Label();
        lblCreated.setStyle("-fx-font-size: 11; -fx-text-fill: #9ca3af;");

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
        infoBox.getChildren().addAll(lblNom, lblDesc, lblCreated);
        VBox.setVgrow(infoBox, Priority.ALWAYS);

        container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        container.getChildren().addAll(infoBox, deleteBtn);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(Dossier item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            lblNom.setText(item.getNom());
            lblDesc.setText(item.getDescription() != null ? item.getDescription() : "Aucune description");

            if (item.getCreatedAt() != null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                lblCreated.setText("Créé le: " + item.getCreatedAt().format(fmt));
            } else {
                lblCreated.setText("");
            }

            deleteBtn.setOnAction(e -> deleteFolder(item));
            setGraphic(container);
        }
    }

    private void deleteFolder(Dossier dossier) {
        // Utilisé seulement par l'ancien système ListView - désactivé
        /*
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
        */
    }
}

