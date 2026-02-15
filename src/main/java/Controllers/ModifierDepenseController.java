package Controllers;

import Models.Depense;
import Models.Budget;
import Services.ServiceDepense;
import Services.ServiceBudget;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import java.sql.*;
import java.util.List;

public class ModifierDepenseController {

    @FXML private VBox vboxDepenses;
    @FXML private ScrollPane scrollDepenses;
    @FXML private Button btnToggleExpenses;
    @FXML private Label lblExpenseCount;

    private final ServiceDepense service = new ServiceDepense();
    private final ServiceBudget serviceBudget = new ServiceBudget();
    private int idBudget = 0;
    private AjouterDepenseController ajouterDepenseController;
    private boolean listVisible = false;
    private Budget budget;

    public void setBudget(int budgetId, AjouterDepenseController controller) {
        this.idBudget = budgetId;
        this.ajouterDepenseController = controller;
        try {
            this.budget = serviceBudget.recupererParId(budgetId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        chargerListeDependances();
    }

    @FXML
    private void onToggleExpenses() {
        listVisible = !listVisible;
        scrollDepenses.setVisible(listVisible);
        btnToggleExpenses.setText(listVisible ? "🔍 Masquer la liste" : "📋 Afficher la liste");
        if (listVisible) {
            chargerListeDependances();
        }
    }

    @FXML
    private void onRetour() {
        if (ajouterDepenseController != null) {
            ajouterDepenseController.retournerAMainPage();
        }
    }

    private void chargerListeDependances() {
        try {
            if (idBudget == 0) return;

            List<Depense> depenses = service.getDepensesByBudget(idBudget);

            vboxDepenses.getChildren().clear();

            if (depenses.isEmpty()) {
                Label emptyLabel = new Label("Aucune dépense trouvée pour ce budget");
                emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 14; -fx-padding: 20;");
                vboxDepenses.getChildren().add(emptyLabel);
            } else {
                for (Depense depense : depenses) {
                    VBox card = creerCarteDepense(depense);
                    vboxDepenses.getChildren().add(card);
                }
            }

            lblExpenseCount.setText(depenses.size() + " dépense(s)");

        } catch (Exception e) {
            showError("Erreur lors du chargement des dépenses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox creerCarteDepense(Depense depense) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: #EEE; -fx-border-radius: 15; -fx-background-color: white; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 20, 0, 0, 10);");
        card.setPrefWidth(400);

        // En-tête avec ID et catégorie
        HBox header = new HBox(10);
        header.setStyle("-fx-alignment: CENTER_LEFT;");
        Label lblId = new Label("ID: " + depense.getIdDepense());
        lblId.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #999;");
        Label lblCategorie = new Label(depense.getCategorie());
        lblCategorie.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #182d88;");
        header.getChildren().addAll(lblId, lblCategorie);

        // Ligne montant
        HBox montantBox = new HBox(10);
        montantBox.setStyle("-fx-alignment: CENTER_LEFT;");
        Label lblMontantLabel = new Label("Montant:");
        lblMontantLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        Label lblMontant = new Label(String.format("%.2f€", depense.getMontant()));
        lblMontant.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #f78f34;");
        montantBox.getChildren().addAll(lblMontantLabel, lblMontant);

        // Ligne date et mode paiement
        HBox dateMode = new HBox(20);
        dateMode.setStyle("-fx-alignment: CENTER_LEFT;");

        Label lblDateLabel = new Label("Date:");
        lblDateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        Label lblDate = new Label(depense.getDateDepense().toString());
        lblDate.setStyle("-fx-text-fill: #333; -fx-font-size: 12;");

        Label lblModeLabel = new Label("Mode:");
        lblModeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        Label lblMode = new Label(depense.getModePaiement());
        lblMode.setStyle("-fx-text-fill: #333; -fx-font-size: 12;");

        VBox dateBox = new VBox(2);
        dateBox.getChildren().addAll(lblDateLabel, lblDate);

        VBox modeBox = new VBox(2);
        modeBox.getChildren().addAll(lblModeLabel, lblMode);

        dateMode.getChildren().addAll(dateBox, modeBox);

        // Description (si présente)
        if (depense.getDescription() != null && !depense.getDescription().isEmpty()) {
            Label lblDescLabel = new Label("Description:");
            lblDescLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12; -fx-font-weight: bold;");
            Label lblDesc = new Label(depense.getDescription());
            lblDesc.setStyle("-fx-text-fill: #333; -fx-font-size: 12; -fx-wrap-text: true;");
            lblDesc.setWrapText(true);
            card.getChildren().addAll(lblDescLabel, lblDesc);
        }

        // Boutons d'action
        HBox actions = new HBox(10);
        actions.setStyle("-fx-alignment: CENTER; -fx-padding: 15 0 0 0;");

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.setStyle("-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #E8F5E9; -fx-text-fill: #175905; -fx-border-radius: 12; -fx-font-weight: bold; -fx-cursor: hand;");
        btnModifier.setOnAction(e -> modifyExpense(depense));

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle("-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-border-radius: 12; -fx-font-weight: bold; -fx-cursor: hand;");
        btnSupprimer.setOnAction(e -> deleteExpense(depense));

        actions.getChildren().addAll(btnModifier, btnSupprimer);

        card.getChildren().addAll(header, montantBox, dateMode, actions);

        return card;
    }

    private void modifyExpense(Depense depense) {
        Dialog<Depense> dialog = new Dialog<>();
        dialog.setTitle("Modifier la Dépense");
        dialog.setHeaderText("ID: " + depense.getIdDepense());

        TextField tfCategorie = new TextField(depense.getCategorie());
        TextField tfMontant = new TextField(String.valueOf(depense.getMontant()));
        TextArea taDescription = new TextArea(depense.getDescription());
        ComboBox<String> cbModePaiement = new ComboBox<>();
        cbModePaiement.setItems(FXCollections.observableArrayList("Carte", "Virement", "Cash"));
        cbModePaiement.setValue(depense.getModePaiement());

        taDescription.setWrapText(true);
        taDescription.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Catégorie:"), 0, 0);
        grid.add(tfCategorie, 1, 0);
        grid.add(new Label("Montant:"), 0, 1);
        grid.add(tfMontant, 1, 1);
        grid.add(new Label("Mode Paiement:"), 0, 2);
        grid.add(cbModePaiement, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(taDescription, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType okButton = new ButtonType("✓ Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("✕ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                try {
                    double nouveauMontant = Double.parseDouble(tfMontant.getText().replace(",", "."));
                    double difference = depense.getMontant() - nouveauMontant;
                    double budgetMaj = budget.getMontantTotal() + difference;

                    depense.setCategorie(tfCategorie.getText());
                    depense.setMontant(nouveauMontant);
                    depense.setDescription(taDescription.getText());
                    depense.setModePaiement(cbModePaiement.getValue());

                    service.modifier(depense);
                    budget.setMontantTotal(budgetMaj);
                    serviceBudget.modifier(budget);

                    showInfo("✓ Dépense modifiée avec succès!");
                    chargerListeDependances();
                } catch (NumberFormatException ex) {
                    showError("Montant invalide!");
                } catch (Exception ex) {
                    showError("Erreur: " + ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void deleteExpense(Depense depense) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer la dépense?");
        alert.setContentText("Catégorie: " + depense.getCategorie() + "\nMontant: " + depense.getMontant() + "€\n\nCette action est irréversible.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                service.supprimer(depense.getIdDepense());

                if (budget != null) {
                    double budgetMaj = budget.getMontantTotal() + depense.getMontant();
                    budget.setMontantTotal(budgetMaj);
                    serviceBudget.modifier(budget);
                }

                showInfo("✓ Dépense supprimée avec succès!");
                chargerListeDependances();
            } catch (Exception e) {
                showError("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
