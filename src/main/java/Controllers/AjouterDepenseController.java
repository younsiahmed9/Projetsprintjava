package Controllers;

import Models.Depense;
import Models.Budget;
import Services.ServiceDepense;
import Services.ServiceBudget;
import Services.SmsService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import java.io.IOException;
import java.sql.SQLDataException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class AjouterDepenseController {

    @FXML private TextField txtUtilisateur;
    @FXML private TextField txtBudget;
    @FXML private TextField txtCategorie;
    @FXML private TextField txtMontant;
    @FXML private DatePicker dateDepense;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbModePaiement;
    @FXML private VBox depensesContainer;
    @FXML private VBox sectionListe;
    @FXML private Button btnAfficherListe;

    private final ServiceDepense service = new ServiceDepense();
    private final ServiceBudget serviceBudget = new ServiceBudget();
    private final SmsService smsService = new SmsService();
    private MainPageController mainPageController;
    private Budget budget;
    private boolean listeAffichee = false;

    @FXML
    public void initialize() {
        // Initialisation simple
    }

    @FXML
    private void afficherListeDependances() {
        if (budget == null) {
            showError("Erreur: Budget non défini");
            return;
        }

        try {
            if (!listeAffichee) {
                // Charger les dépenses
                List<Depense> depenses = service.getDepensesByBudget(budget.getIdBudget());

                // Vider le container
                depensesContainer.getChildren().clear();

                // Créer une carte pour chaque dépense
                for (Depense depense : depenses) {
                    VBox carte = creerCarteDepense(depense);
                    depensesContainer.getChildren().add(carte);
                }

                // Afficher la section
                sectionListe.setVisible(true);
                sectionListe.setManaged(true);
                btnAfficherListe.setText("🔍 Masquer les dépenses");
                listeAffichee = true;

                if (depenses.isEmpty()) {
                    Label lblVide = new Label("Aucune dépense trouvée pour ce budget");
                    lblVide.setStyle("-fx-text-fill: #999; -fx-font-size: 14; -fx-padding: 20;");
                    depensesContainer.getChildren().add(lblVide);
                }
            } else {
                // Masquer la section
                sectionListe.setVisible(false);
                sectionListe.setManaged(false);
                btnAfficherListe.setText("📋 Voir les dépenses du budget");
                listeAffichee = false;
            }
        } catch (Exception e) {
            showError("Erreur lors du chargement de la liste: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox creerCarteDepense(Depense depense) {
        VBox carte = new VBox(10);
        carte.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #E0E0E0; " +
                "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Header: ID et Catégorie
        HBox header = new HBox(15);
        header.setStyle("-fx-alignment: center-left;");

        Label lblId = new Label("ID: " + depense.getIdDepense());
        lblId.setStyle("-fx-font-weight: bold; -fx-text-fill: #666; -fx-font-size: 12;");

        Label lblCategorie = new Label("📁 " + depense.getCategorie());
        lblCategorie.setStyle("-fx-font-weight: bold; -fx-text-fill: #182d88; -fx-font-size: 14;");

        header.getChildren().addAll(lblId, lblCategorie);

        // Contenu principal
        HBox contenu = new HBox(20);
        contenu.setStyle("-fx-alignment: center-left;");

        // Colonne gauche : Montant et Date
        VBox colonneGauche = new VBox(5);
        Label lblMontant = new Label(String.format("💰 %.2f DT", depense.getMontant()));
        lblMontant.setStyle("-fx-font-weight: bold; -fx-text-fill: #f78f34; -fx-font-size: 16;");

        Label lblDate = new Label("📅 " + depense.getDateDepense());
        lblDate.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        colonneGauche.getChildren().addAll(lblMontant, lblDate);

        // Colonne droite : Mode de paiement
        VBox colonneDroite = new VBox(5);
        Label lblMode = new Label("💳 " + depense.getModePaiement());
        lblMode.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        colonneDroite.getChildren().add(lblMode);

        contenu.getChildren().addAll(colonneGauche, colonneDroite);

        // Description (si existe)
        if (depense.getDescription() != null && !depense.getDescription().isEmpty()) {
            Label lblDesc = new Label("📝 " + depense.getDescription());
            lblDesc.setStyle("-fx-text-fill: #666; -fx-font-size: 12; -fx-wrap-text: true;");
            lblDesc.setWrapText(true);
            lblDesc.setMaxWidth(600);
            carte.getChildren().add(lblDesc);
        }

        // Footer: Boutons d'actions
        HBox footer = new HBox(10);
        footer.setStyle("-fx-alignment: center-right; -fx-padding: 10 0 0 0;");

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #175905; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        btnModifier.setOnAction(e -> modifierDepense(depense));

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; " +
                "-fx-font-weight: bold; -fx-padding: 8 15; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        btnSupprimer.setOnAction(e -> supprimerDepense(depense));

        footer.getChildren().addAll(btnModifier, btnSupprimer);

        // Assembler la carte
        carte.getChildren().addAll(header, contenu, footer);

        return carte;
    }

    private void modifierDepense(Depense depense) {
        // Créer une boîte de dialogue pour modifier
        Dialog<Depense> dialog = new Dialog<>();
        dialog.setTitle("Modifier la Dépense");
        dialog.setHeaderText("ID: " + depense.getIdDepense());

        // Créer les champs
        TextField tfCategorie = new TextField(depense.getCategorie());
        TextField tfMontant = new TextField(String.valueOf(depense.getMontant()));
        TextArea taDescription = new TextArea(depense.getDescription());
        ComboBox<String> cbModePaiement = new ComboBox<>();
        cbModePaiement.setItems(FXCollections.observableArrayList("Carte", "Virement", "Cash"));
        cbModePaiement.setValue(depense.getModePaiement());

        taDescription.setWrapText(true);
        taDescription.setPrefRowCount(3);

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Catégorie:"), 0, 0);
        grid.add(tfCategorie, 1, 0);
        grid.add(new Label("Montant:"), 0, 1);
        grid.add(tfMontant, 1, 1);
        grid.add(new Label("Mode Paiement:"), 0, 2);
        grid.add(cbModePaiement, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(taDescription, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Boutons
        ButtonType okButton = new ButtonType("✓ Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("✕ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                try {
                    double nouveauMontant = Double.parseDouble(tfMontant.getText().replace(",", "."));

                    // Restaurer le montant au budget
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
                    rafraichirListe();
                } catch (NumberFormatException ex) {
                    showError("Montant invalide!");
                } catch (SQLDataException ex) {
                    showError("Erreur base de données: " + ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void supprimerDepense(Depense depense) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer la dépense?");
        alert.setContentText("Catégorie: " + depense.getCategorie() + "\nMontant: " + depense.getMontant() + "€\n\nCette action est irréversible.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                service.supprimer(depense.getIdDepense());

                // Restaurer le montant au budget
                double budgetMaj = budget.getMontantTotal() + depense.getMontant();
                budget.setMontantTotal(budgetMaj);
                serviceBudget.modifier(budget);

                showInfo("✓ Dépense supprimée avec succès!");
                rafraichirListe();
            } catch (Exception e) {
                showError("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void rafraichirListe() {
        try {
            List<Depense> depenses = service.getDepensesByBudget(budget.getIdBudget());

            // Vider le container
            depensesContainer.getChildren().clear();

            // Créer une carte pour chaque dépense
            for (Depense depense : depenses) {
                VBox carte = creerCarteDepense(depense);
                depensesContainer.getChildren().add(carte);
            }

            if (depenses.isEmpty()) {
                Label lblVide = new Label("Aucune dépense trouvée pour ce budget");
                lblVide.setStyle("-fx-text-fill: #999; -fx-font-size: 14; -fx-padding: 20;");
                depensesContainer.getChildren().add(lblVide);
            }
        } catch (Exception e) {
            showError("Erreur lors du rafraîchissement: " + e.getMessage());
        }
    }

    @FXML
    private void onAjouter() {
        try {
            int idUtilisateur = Integer.parseInt(txtUtilisateur.getText().trim());
            int idBudget = Integer.parseInt(txtBudget.getText().trim());
            String categorie = txtCategorie.getText().trim();
            double montant = Double.parseDouble(txtMontant.getText().trim().replace(",", "."));
            LocalDate ld = dateDepense.getValue();
            Date dateD = (ld != null) ? Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()) : new Date();
            String description = txtDescription.getText().trim();
            String modePaiement = cbModePaiement.getValue();

            if (categorie.isEmpty() || modePaiement == null) {
                showError("Veuillez remplir tous les champs obligatoires");
                return;
            }

            if (montant <= 0) {
                showError("Le montant doit être supérieur à 0");
                return;
            }

            if (budget == null) {
                showError("Erreur: Budget non défini");
                return;
            }

            if (montant > budget.getMontantTotal()) {
                SmsService.SmsResult smsResult = smsService.sendBudgetExceededSms(budget, montant);
                showError("❌ Budget insuffisant! " + smsResult.getUserMessage()
                        + " La depense sera enregistree.");
            }

            Depense d = new Depense(0, idUtilisateur, idBudget, categorie, montant, dateD, description, modePaiement);
            service.ajouter(d);

            double nouveauMontant = budget.getMontantTotal() - montant;
            budget.setMontantTotal(nouveauMontant);
            serviceBudget.modifier(budget);

            showInfo("✓ Dépense ajoutée avec succès!");
            clearForm();
            rafraichirListe();
        } catch (NumberFormatException e) {
            showError("Erreur: Format de nombre invalide");
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void onAnnuler() {
        retournerAMainPage();
    }

    @FXML
    private void onRetour() {
        retournerAMainPage();
    }

    public void retournerAMainPage() {
        if (mainPageController != null) {
            mainPageController.retournerAListe();
            mainPageController.refreshBudgets();
        }
    }

    private void clearForm() {
        txtUtilisateur.setText("1");
        txtBudget.clear();
        txtCategorie.clear();
        txtMontant.clear();
        dateDepense.setValue(null);
        txtDescription.clear();
        cbModePaiement.setValue(null);
    }

    public void setMainPageController(MainPageController controller) {
        this.mainPageController = controller;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
        if (budget != null) {
            txtBudget.setText(String.valueOf(budget.getIdBudget()));
            txtCategorie.setText(budget.getNomBudget());
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
