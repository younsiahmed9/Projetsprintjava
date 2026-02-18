package controllers;

import models.Service;
import models.Produit;
import services.ServiceProduit;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

public class AjoutServiceController {

    @FXML private TextField nomField;
    @FXML private ComboBox<Service.TypeService> typeCombo;
    @FXML private TextField tarifField;
    @FXML private ComboBox<Service.Frequence> frequenceCombo;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<Service.StatutService> statutCombo;
    @FXML private ComboBox<Produit> produitCombo;  // ComboBox de type Produit

    @FXML private Label nomError;
    @FXML private Label typeError;
    @FXML private Label tarifError;
    @FXML private Label frequenceError;
    @FXML private Label dateError;
    @FXML private Label statutError;
    @FXML private Label dateFinError;  // Ajouté pour éviter l'erreur

    private ServiceProduit serviceProduit;
    private Service service;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        serviceProduit = new ServiceProduit();

        // Initialiser les combobox
        typeCombo.setItems(FXCollections.observableArrayList(Service.TypeService.values()));
        frequenceCombo.setItems(FXCollections.observableArrayList(Service.Frequence.values()));
        statutCombo.setItems(FXCollections.observableArrayList(Service.StatutService.values()));
        statutCombo.setValue(Service.StatutService.actif);

        // Charger les produits disponibles
        chargerProduits();

        // Configuration de l'affichage des produits dans la ComboBox
        setupProduitCombo();

        // Désactiver la fréquence par défaut
        frequenceCombo.setDisable(true);
        dateDebut.setDisable(true);
        dateFin.setDisable(true);

        // Écouter le changement de type
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isAbonnement = newVal == Service.TypeService.abonnement;
            frequenceCombo.setDisable(!isAbonnement);
            dateDebut.setDisable(!isAbonnement);
            dateFin.setDisable(!isAbonnement);
            if (!isAbonnement) {
                frequenceCombo.setValue(null);
                dateDebut.setValue(null);
                dateFin.setValue(null);
            }
        });

        // Validation en temps réel
        setupRealTimeValidation();
    }

    private void chargerProduits() {
        try {
            // Charger tous les produits disponibles
            ObservableList<Produit> produits = FXCollections.observableArrayList(
                    serviceProduit.getProduitsDisponibles()
            );

            // Ajouter null en première position (option "Aucun produit")
            produits.add(0, null);

            produitCombo.setItems(produits);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les produits: " + e.getMessage());
        }
    }

    private void setupProduitCombo() {
        // Configuration de l'affichage dans la ComboBox (quand un élément est sélectionné)
        produitCombo.setConverter(new StringConverter<Produit>() {
            @Override
            public String toString(Produit produit) {
                if (produit == null) return "Aucun produit";
                // Afficher "Nom - Code" dans le champ
                return produit.getNomProduit() + " - " + produit.getCodeUnique();
            }

            @Override
            public Produit fromString(String string) {
                return null; // Non utilisé
            }
        });

        // Personnaliser l'affichage de chaque élément dans la liste déroulante
        produitCombo.setCellFactory(lv -> new ListCell<Produit>() {
            @Override
            protected void updateItem(Produit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Aucun produit");
                } else {
                    // Afficher avec plus de détails : "Nom - Code (Montant TND)"
                    setText(item.getNomProduit() + " - " + item.getCodeUnique() +
                            " (" + item.getMontant() + " TND)");
                }
            }
        });
    }

    private void setupRealTimeValidation() {
        // Validation du nom
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                nomError.setText("Le nom est obligatoire");
                nomError.setTextFill(Color.RED);
            } else if (newVal.length() < 3) {
                nomError.setText("Le nom doit contenir au moins 3 caractères");
                nomError.setTextFill(Color.RED);
            } else {
                nomError.setText("✓");
                nomError.setTextFill(Color.GREEN);
            }
        });

        // Validation du tarif
        tarifField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                tarifError.setText("Le tarif est obligatoire");
                tarifError.setTextFill(Color.RED);
            } else {
                try {
                    double tarif = Double.parseDouble(newVal);
                    if (tarif <= 0) {
                        tarifError.setText("Le tarif doit être > 0");
                        tarifError.setTextFill(Color.RED);
                    } else if (tarif > 1000000) {
                        tarifError.setText("Tarif trop élevé (max 1M)");
                        tarifError.setTextFill(Color.RED);
                    } else {
                        tarifError.setText("✓");
                        tarifError.setTextFill(Color.GREEN);
                    }
                } catch (NumberFormatException e) {
                    tarifError.setText("Format invalide");
                    tarifError.setTextFill(Color.RED);
                }
            }
        });

        // Validation du type
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                typeError.setText("Le type est obligatoire");
                typeError.setTextFill(Color.RED);
            } else {
                typeError.setText("✓");
                typeError.setTextFill(Color.GREEN);
            }
        });

        // Validation de la fréquence pour les abonnements
        frequenceCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (typeCombo.getValue() == Service.TypeService.abonnement && newVal == null) {
                frequenceError.setText("Fréquence obligatoire");
                frequenceError.setTextFill(Color.RED);
            } else {
                frequenceError.setText("✓");
                frequenceError.setTextFill(Color.GREEN);
            }
        });

        // Validation des dates
        dateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDates();
        });

        dateFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDates();
        });

        // Validation du statut
        statutCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                statutError.setText("Le statut est obligatoire");
                statutError.setTextFill(Color.RED);
            } else {
                statutError.setText("✓");
                statutError.setTextFill(Color.GREEN);
            }
        });
    }

    private void validateDates() {
        LocalDate debut = dateDebut.getValue();
        LocalDate fin = dateFin.getValue();

        if (typeCombo.getValue() == Service.TypeService.abonnement) {
            if (debut == null) {
                dateError.setText("Date début obligatoire");
                dateError.setTextFill(Color.RED);
            } else if (fin != null && fin.isBefore(debut)) {
                dateError.setText("Date fin > date début");
                dateError.setTextFill(Color.RED);
            } else if (fin != null && fin.isBefore(LocalDate.now())) {
                dateError.setText("Date fin déjà passée");
                dateError.setTextFill(Color.ORANGE);
            } else {
                dateError.setText("✓");
                dateError.setTextFill(Color.GREEN);
            }
        } else {
            dateError.setText("");
            if (dateFinError != null) dateFinError.setText("");
        }
    }

    public boolean validateForm() {
        boolean isValid = true;

        // Validation nom
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            nomError.setText("Le nom est obligatoire");
            nomError.setTextFill(Color.RED);
            isValid = false;
        } else if (nomField.getText().length() < 3) {
            nomError.setText("Minimum 3 caractères");
            nomError.setTextFill(Color.RED);
            isValid = false;
        }

        // Validation type
        if (typeCombo.getValue() == null) {
            typeError.setText("Type obligatoire");
            typeError.setTextFill(Color.RED);
            isValid = false;
        }

        // Validation tarif
        if (tarifField.getText() == null || tarifField.getText().trim().isEmpty()) {
            tarifError.setText("Tarif obligatoire");
            tarifError.setTextFill(Color.RED);
            isValid = false;
        } else {
            try {
                double tarif = Double.parseDouble(tarifField.getText());
                if (tarif <= 0) {
                    tarifError.setText("Tarif doit être > 0");
                    tarifError.setTextFill(Color.RED);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tarifError.setText("Format invalide");
                tarifError.setTextFill(Color.RED);
                isValid = false;
            }
        }

        // Validation pour les abonnements
        if (typeCombo.getValue() == Service.TypeService.abonnement) {
            if (frequenceCombo.getValue() == null) {
                frequenceError.setText("Fréquence obligatoire");
                frequenceError.setTextFill(Color.RED);
                isValid = false;
            }

            if (dateDebut.getValue() == null) {
                dateError.setText("Date début obligatoire");
                dateError.setTextFill(Color.RED);
                isValid = false;
            }

            LocalDate debut = dateDebut.getValue();
            LocalDate fin = dateFin.getValue();
            if (debut != null && fin != null && fin.isBefore(debut)) {
                dateError.setText("Date fin doit être après début");
                dateError.setTextFill(Color.RED);
                isValid = false;
            }
        }

        // Validation statut
        if (statutCombo.getValue() == null) {
            statutError.setText("Statut obligatoire");
            statutError.setTextFill(Color.RED);
            isValid = false;
        }

        return isValid;
    }

    public Service getService() {
        if (!validateForm()) {
            return null;
        }

        try {
            Service s = new Service();
            s.setNomService(nomField.getText().trim());
            s.setTypeService(typeCombo.getValue());
            s.setTarif(new BigDecimal(tarifField.getText().trim()));
            s.setFrequence(frequenceCombo.getValue());
            s.setDateDebut(dateDebut.getValue());
            s.setDateFin(dateFin.getValue());
            s.setStatut(statutCombo.getValue());

            // Récupérer l'ID du produit sélectionné (peut être null)
            Produit selectedProduit = produitCombo.getValue();
            if (selectedProduit != null) {
                s.setIdProduit(selectedProduit.getIdProduit());
            } else {
                s.setIdProduit(null);
            }

            return s;
        } catch (Exception e) {
            showAlert("Erreur", "Données invalides: " + e.getMessage());
            return null;
        }
    }

    public void setService(Service service) {
        this.service = service;
        this.isEditMode = true;

        if (service != null) {
            nomField.setText(service.getNomService());
            typeCombo.setValue(service.getTypeService());
            tarifField.setText(service.getTarif().toString());
            frequenceCombo.setValue(service.getFrequence());
            dateDebut.setValue(service.getDateDebut());
            dateFin.setValue(service.getDateFin());
            statutCombo.setValue(service.getStatut());

            // ✅ CORRIGÉ : Utilisation de equals() au lieu de ==
            Integer idProduit = service.getIdProduit();
            if (idProduit != null) {
                try {
                    // Chercher le produit dans la liste
                    for (Produit p : produitCombo.getItems()) {
                        if (p != null && idProduit.equals(p.getIdProduit())) {
                            produitCombo.setValue(p);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                produitCombo.setValue(null); // Sélectionner "Aucun produit"
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}