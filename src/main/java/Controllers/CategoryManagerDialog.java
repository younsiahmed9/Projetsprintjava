package Controllers;

import Controllers.Dialogs.CrudDialogManager;
import Models.Categorie;
import Services.ServiceCategorie;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import utils.UiStyles;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrôleur pour la gestion des catégories
 * CRUD complet avec interface professionnelle
 */
public class CategoryManagerDialog {

    private final ServiceCategorie categorieService = new ServiceCategorie();
    private final CrudDialogManager dialogManager = new CrudDialogManager();

    /**
     * Affiche un dialog de gestion complète des catégories
     */
    public void showCategoryManager() {
        Dialog<Void> dialog = new Dialog<>();
        UiStyles.applyDialogStyles(dialog.getDialogPane());
        dialog.setTitle("Gestion des Catégories");
        dialog.setHeaderText("Personnalisez vos catégories de documents");
        dialog.getDialogPane().setPrefWidth(700);
        dialog.getDialogPane().setPrefHeight(600);

        ListView<Categorie> categoryList = new ListView<>();
        categoryList.getStyleClass().add("documents-list");
        categoryList.setPrefHeight(350);
        refreshCategoryList(categoryList);
        categoryList.setCellFactory(param -> new CategoryListCell());

        Button btnEdit = new Button("✎ Modifier");
        btnEdit.getStyleClass().add("btn-action-light-blue");
        btnEdit.setPrefWidth(120);
        btnEdit.setOnAction(e -> {
            Categorie selected = categoryList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                dialogManager.showCategoryDialog(selected, true).ifPresent(updated -> {
                    try {
                        categorieService.update(updated);
                        AlertUtils.showSuccess("Succès", "Catégorie modifiée !");
                        refreshCategoryList(categoryList);
                    } catch (SQLException ex) {
                        AlertUtils.showError("Erreur", "Modification impossible: " + ex.getMessage());
                    }
                });
            } else {
                AlertUtils.showError("Sélection requise", "Veuillez sélectionner une catégorie.");
            }
        });

        Button btnDelete = new Button("🗑️ Supprimer");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setPrefWidth(120);
        btnDelete.setOnAction(e -> {
            Categorie selected = categoryList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (dialogManager.showDeleteCategoryConfirmation(selected)) {
                    try {
                        categorieService.delete(selected.getId());
                        AlertUtils.showSuccess("Succès", "Catégorie supprimée.");
                        refreshCategoryList(categoryList);
                    } catch (SQLException ex) {
                        AlertUtils.showError("Erreur", "Suppression impossible: " + ex.getMessage());
                    }
                }
            } else {
                AlertUtils.showError("Sélection requise", "Veuillez sélectionner une catégorie.");
            }
        });

        Button btnAddCategory = new Button("+ Nouvelle Catégorie");
        btnAddCategory.getStyleClass().add("btn-primary");
        btnAddCategory.setPrefWidth(220);
        btnAddCategory.setStyle("-fx-font-size: 14; -fx-padding: 10 25; -fx-background-radius: 30;");
        btnAddCategory.setOnAction(e -> {
            dialogManager.showCategoryDialog(null, false).ifPresent(category -> {
                try {
                    categorieService.add(category);
                    AlertUtils.showSuccess("Succès", "Catégorie créée !");
                    refreshCategoryList(categoryList);
                } catch (SQLException ex) {
                    AlertUtils.showError("Erreur", "Création impossible: " + ex.getMessage());
                }
            });
        });

        HBox btnBox = new HBox(15, btnEdit, btnDelete);
        btnBox.setAlignment(Pos.CENTER_LEFT);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white;");

        Label lblListTitle = new Label("Catégories Actives");
        lblListTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        content.getChildren().addAll(
                lblListTitle,
                categoryList,
                btnBox,
                new Separator(),
                btnAddCategory);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void refreshCategoryList(ListView<Categorie> listView) {
        try {
            List<Categorie> categories = categorieService.findAll();
            listView.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Chargement impossible: " + e.getMessage());
        }
    }

    public static class CategoryListCell extends ListCell<Categorie> {
        @Override
        protected void updateItem(Categorie category, boolean empty) {
            super.updateItem(category, empty);
            if (empty || category == null) {
                setGraphic(null);
                setText(null);
            } else {
                VBox box = new VBox(5);
                box.setPadding(new Insets(10));

                Label lblName = new Label("📁 " + category.getNom());
                lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #182d88;");

                Label lblDesc = new Label(category.getDescription() != null && !category.getDescription().isEmpty()
                        ? category.getDescription()
                        : "Aucune description");
                lblDesc.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12;");

                box.getChildren().addAll(lblName, lblDesc);
                setGraphic(box);
            }
        }
    }
}
