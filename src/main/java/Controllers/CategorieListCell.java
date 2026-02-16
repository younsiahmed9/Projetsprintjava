package Controllers;

import Models.Categorie;
import Services.ServiceCategorie;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

public class CategorieListCell extends ListCell<Categorie> {

    private HBox container;
    private Label nomLabel;
    private Label descLabel;
    private Button deleteBtn;
    private final ServiceCategorie categorieService = new ServiceCategorie();

    public CategorieListCell() {
        setPrefHeight(80);
        initializeUI();
    }

    private void initializeUI() {
        nomLabel = new Label();
        nomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #182d88;");

        descLabel = new Label();
        descLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666666;");
        descLabel.setWrapText(true);

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

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(nomLabel, descLabel);
        VBox.setVgrow(infoBox, Priority.ALWAYS);

        container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(8));
        container.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        container.getChildren().addAll(infoBox, deleteBtn);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(Categorie categorie, boolean empty) {
        super.updateItem(categorie, empty);

        if (empty || categorie == null) {
            setGraphic(null);
            setText(null);
        } else {
            nomLabel.setText("📁 " + categorie.getNom());
            String desc = categorie.getDescription() != null && !categorie.getDescription().isEmpty()
                    ? categorie.getDescription()
                    : "(Aucune description)";
            descLabel.setText(desc);

            deleteBtn.setOnAction(e -> deleteCategory(categorie));
            setGraphic(container);
        }
    }

    private void deleteCategory(Categorie categorie) {
    }
}

