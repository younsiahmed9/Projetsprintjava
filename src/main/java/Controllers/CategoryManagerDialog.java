package Controllers;

import Controllers.Dialogs.CrudDialogManager;
import Models.Categorie;
import Services.ServiceCategorie;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
        dialog.setHeaderText("Créer, modifier ou supprimer des catégories");
        dialog.setWidth(800);
        dialog.setHeight(600);

        ListView<Categorie> categoryList = new ListView<>();
        categoryList.getStyleClass().add("documents-list");
        categoryList.setPrefHeight(300);
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
                        AlertUtils.showSuccess("Succès", "Catégorie modifiée avec succès !");
                        refreshCategoryList(categoryList);
                    } catch (SQLException ex) {
                        AlertUtils.showError("Erreur", "Impossible de modifier la catégorie: " + ex.getMessage());
                    }
                });
            } else {
                AlertUtils.showError("Erreur", "Veuillez sélectionner une catégorie !");
            }
        });

        Button btnDelete = new Button("🗑️ Supprimer");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setPrefWidth(120);
        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-padding: 10 20; -fx-font-size: 12; -fx-background-color: #b91c1c; -fx-text-fill: white; -fx-border-radius: 6;"));
        btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-padding: 10 20; -fx-font-size: 12; -fx-background-color: #dc2626; -fx-text-fill: white; -fx-border-radius: 6;"));
        btnDelete.setOnAction(e -> {
            Categorie selected = categoryList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (dialogManager.showDeleteCategoryConfirmation(selected)) {
                    try {
                        categorieService.delete(selected.getId());
                        AlertUtils.showSuccess("Succès", "Catégorie supprimée avec succès !");
                        refreshCategoryList(categoryList);
                    } catch (SQLException ex) {
                        AlertUtils.showError("Erreur", "Impossible de supprimer la catégorie: " + ex.getMessage());
                    }
                }
            } else {
                AlertUtils.showError("Erreur", "Veuillez sélectionner une catégorie !");
            }
        });

        Button btnAddCategory = new Button("+ Ajouter une catégorie");
        btnAddCategory.getStyleClass().add("btn-primary");
        btnAddCategory.setPrefWidth(200);
        btnAddCategory.setOnAction(e -> {
            dialogManager.showCategoryDialog(null, false).ifPresent(category -> {
                try {
                    categorieService.add(category);
                    AlertUtils.showSuccess("Succès", "Catégorie créée avec succès !");
                    refreshCategoryList(categoryList);
                } catch (SQLException ex) {
                    AlertUtils.showError("Erreur", "Impossible de créer la catégorie: " + ex.getMessage());
                }
            });
        });

        javafx.scene.layout.HBox btnBox = new javafx.scene.layout.HBox(10);
        btnBox.getStyleClass().add("card");
        btnBox.setStyle("-fx-padding: 10;");
        btnBox.getChildren().addAll(btnEdit, btnDelete);

        VBox content = new VBox(15);
        content.getStyleClass().add("card");
        content.setStyle("-fx-padding: 20;");
        content.getChildren().addAll(
            new Label("Liste des catégories :"),
            new Separator(),
            categoryList,
            new Separator(),
            btnBox,
            new Separator(),
            btnAddCategory
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void refreshCategoryList(ListView<Categorie> listView) {
        try {
            List<Categorie> categories = categorieService.findAll();
            listView.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les catégories: " + e.getMessage());
        }
    }

    public static class CategoryListCell extends ListCell<Categorie> {
        @Override
        protected void updateItem(Categorie category, boolean empty) {
            super.updateItem(category, empty);
            if (empty || category == null) {
                setText(null);
                setStyle(null);
            } else {
                setText("📁 " + category.getNom());
                if (category.getDescription() != null && !category.getDescription().isEmpty()) {
                    setText(getText() + "\n   " + category.getDescription());
                }
                setPrefHeight(60);
                setStyle("-fx-text-fill: #333; -fx-font-size: 12;");
            }
        }
    }
}

