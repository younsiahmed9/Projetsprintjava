package Controllers;

import Models.Document;
import Services.ServiceDoublon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

/**
 * Panneau de détection et gestion des doublons
 */
public class DoublonDetectionPanel {

    private final ServiceDoublon doublonService;
    private Stage stage;
    private VBox resultsContainer;
    private Label lblStatus;
    private Slider sliderSeuil;
    private Label lblSeuilValue;

    public DoublonDetectionPanel() {
        this.doublonService = new ServiceDoublon();
    }

    /**
     * Affiche le panneau de détection des doublons
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Détection des Doublons");
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox root = createMainLayout();

        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createMainLayout() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // En-tête
        Label title = new Label("🔍 Détection des Documents en Doublon");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #1d4ed8;");

        Label subtitle = new Label("Identifiez et gérez les documents similaires ou en double");
        subtitle.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");

        // Contrôles
        HBox controls = createControlsPanel();

        // Statut
        lblStatus = new Label("Prêt à analyser");
        lblStatus.setStyle("-fx-font-size: 12; -fx-text-fill: #666; -fx-padding: 10;");

        // Zone de résultats
        resultsContainer = new VBox(10);
        resultsContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(resultsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Boutons d'action
        HBox bottomButtons = createBottomButtons();

        root.getChildren().addAll(title, subtitle, new Separator(), controls, lblStatus, scrollPane, bottomButtons);

        return root;
    }

    private HBox createControlsPanel() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));
        controls.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        Label lblSeuil = new Label("Seuil de similarité:");
        lblSeuil.setStyle("-fx-font-weight: bold;");

        sliderSeuil = new Slider(0, 100, 70);
        sliderSeuil.setShowTickLabels(true);
        sliderSeuil.setShowTickMarks(true);
        sliderSeuil.setMajorTickUnit(10);
        sliderSeuil.setMinorTickCount(1);
        sliderSeuil.setPrefWidth(300);

        lblSeuilValue = new Label("70%");
        lblSeuilValue.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1d4ed8; -fx-min-width: 50;");

        sliderSeuil.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblSeuilValue.setText(String.format("%.0f%%", newVal.doubleValue()));
        });

        Button btnAnalyser = new Button("🔍 Analyser");
        btnAnalyser.setStyle(
                "-fx-font-size: 13; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: linear-gradient(to right, #3b82f6, #1d4ed8); " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 8 20; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(29, 78, 216, 0.3), 5, 0, 0, 2);");
        btnAnalyser.setOnAction(e -> analyserDoublons());

        Label lblInfo = new Label("ℹ️ Plus le seuil est élevé, plus les documents doivent être similaires");
        lblInfo.setStyle("-fx-font-size: 11; -fx-text-fill: #999;");

        controls.getChildren().addAll(lblSeuil, sliderSeuil, lblSeuilValue, btnAnalyser);

        VBox wrapper = new VBox(10, controls, lblInfo);
        return new HBox(wrapper);
    }

    private HBox createBottomButtons() {
        HBox bottomButtons = new HBox(10);
        bottomButtons.setAlignment(Pos.CENTER_RIGHT);
        bottomButtons.setPadding(new Insets(10, 0, 0, 0));

        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle("-fx-padding: 8 20;");
        btnFermer.setOnAction(e -> stage.close());

        bottomButtons.getChildren().add(btnFermer);

        return bottomButtons;
    }

    private void analyserDoublons() {
        resultsContainer.getChildren().clear();
        lblStatus.setText("⏳ Analyse en cours...");
        lblStatus.setStyle("-fx-font-size: 12; -fx-text-fill: #f59e0b; -fx-padding: 10;");

        // Exécution en arrière-plan pour ne pas bloquer l'UI
        Thread analysisThread = new Thread(() -> {
            try {
                double seuil = sliderSeuil.getValue() / 100.0;
                ServiceDoublon.DoublonReport report = doublonService.generateReport(seuil);

                // Mise à jour de l'UI sur le thread JavaFX
                javafx.application.Platform.runLater(() -> {
                    afficherResultats(report);
                });

            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    lblStatus.setText("❌ Erreur lors de l'analyse: " + e.getMessage());
                    lblStatus.setStyle("-fx-font-size: 12; -fx-text-fill: #dc2626; -fx-padding: 10;");
                    AlertUtils.showError("Erreur", "Impossible d'analyser les doublons: " + e.getMessage());
                });
            }
        });

        analysisThread.setDaemon(true);
        analysisThread.start();
    }

    private void afficherResultats(ServiceDoublon.DoublonReport report) {
        resultsContainer.getChildren().clear();

        if (report.getGroups().isEmpty()) {
            lblStatus.setText("✅ Aucun doublon détecté avec ce seuil");
            lblStatus.setStyle("-fx-font-size: 12; -fx-text-fill: #10b981; -fx-padding: 10;");

            Label noResult = new Label("🎉 Aucun document en doublon trouvé !");
            noResult.setStyle("-fx-font-size: 16; -fx-text-fill: #666; -fx-padding: 50;");
            resultsContainer.getChildren().add(noResult);
            resultsContainer.setAlignment(Pos.CENTER);
            return;
        }

        resultsContainer.setAlignment(Pos.TOP_LEFT);

        // Statistiques
        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        Label statsTitle = new Label("📊 Résumé");
        statsTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label statsContent = new Label(String.format(
                "• %d groupe(s) de doublons détectés\n" +
                        "• %d document(s) au total concernés\n" +
                        "• %d doublon(s) haute confiance (≥90%%)\n" +
                        "• Seuil utilisé: %.0f%%",
                report.getGroupCount(),
                report.getTotalDoublons(),
                report.getHighConfidence(),
                report.getSeuil() * 100));
        statsContent.setStyle("-fx-font-size: 13;");

        statsBox.getChildren().addAll(statsTitle, statsContent);
        resultsContainer.getChildren().add(statsBox);

        // Groupes de doublons
        Label groupsTitle = new Label("🔍 Doublons Détectés");
        groupsTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-padding: 10 0;");
        resultsContainer.getChildren().add(groupsTitle);

        for (int i = 0; i < report.getGroups().size(); i++) {
            ServiceDoublon.DoublonGroup group = report.getGroups().get(i);
            VBox groupCard = createGroupCard(group, i + 1);
            resultsContainer.getChildren().add(groupCard);
        }

        lblStatus.setText(String.format("✅ Analyse terminée - %d groupe(s) trouvé(s)", report.getGroupCount()));
        lblStatus.setStyle("-fx-font-size: 12; -fx-text-fill: #10b981; -fx-padding: 10;");
    }

    private VBox createGroupCard(ServiceDoublon.DoublonGroup group, int groupNumber) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        Label groupTitle = new Label(String.format("Groupe #%d - Document original:", groupNumber));
        groupTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1d4ed8;");

        VBox originalDoc = createDocumentCard(group.getOriginal(), true);

        Label similarsLabel = new Label("Documents similaires:");
        similarsLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        VBox similarsContainer = new VBox(5);
        for (ServiceDoublon.DoublonPair pair : group.getSimilars()) {
            HBox pairBox = createDoublonPairBox(pair);
            similarsContainer.getChildren().add(pairBox);
        }

        card.getChildren().addAll(groupTitle, originalDoc, similarsLabel, similarsContainer);

        return card;
    }

    private VBox createDocumentCard(Document doc, boolean isOriginal) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle(isOriginal
                ? "-fx-background-color: #eff6ff; -fx-border-color: #1d4ed8; -fx-border-radius: 5; -fx-background-radius: 5;"
                : "-fx-background-color: #fef3c7; -fx-border-color: #f59e0b; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label titre = new Label(isOriginal ? "📄 " + doc.getTitre() : "📄 " + doc.getTitre());
        titre.setStyle("-fx-font-weight: bold;");

        Label dossier = new Label("📁 " + (doc.getDossier() != null ? doc.getDossier().getNom() : "N/A"));
        dossier.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        card.getChildren().addAll(titre, dossier);

        return card;
    }

    private HBox createDoublonPairBox(ServiceDoublon.DoublonPair pair) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5));

        VBox docCard = createDocumentCard(pair.getDoc2(), false);
        HBox.setHgrow(docCard, Priority.ALWAYS);

        Label similarity = new Label(pair.getSimilarityPercent());
        similarity.setStyle(String.format(
                "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: %s; -fx-min-width: 60; -fx-alignment: center;",
                pair.getSimilarity() >= 0.9 ? "#dc2626" : pair.getSimilarity() >= 0.75 ? "#f59e0b" : "#10b981"));

        Button btnComparer = new Button("Comparer");
        btnComparer.setStyle("-fx-font-size: 11; -fx-padding: 5 10;");
        btnComparer.setOnAction(e -> comparerDocuments(pair));

        box.getChildren().addAll(docCard, similarity, btnComparer);

        return box;
    }

    private void comparerDocuments(ServiceDoublon.DoublonPair pair) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Comparaison de Documents");
        alert.setHeaderText(String.format("Similarité: %s", pair.getSimilarityPercent()));

        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        // Document 1
        Label doc1Title = new Label("📄 Document 1: " + pair.getDoc1().getTitre());
        doc1Title.setStyle("-fx-font-weight: bold;");
        Label doc1Details = new Label(String.format(
                "Dossier: %s\nCatégorie: %s",
                pair.getDoc1().getDossier() != null ? pair.getDoc1().getDossier().getNom() : "N/A",
                pair.getDoc1().getCategorie() != null ? pair.getDoc1().getCategorie().getNom() : "N/A"));

        // Document 2
        Label doc2Title = new Label("📄 Document 2: " + pair.getDoc2().getTitre());
        doc2Title.setStyle("-fx-font-weight: bold;");
        Label doc2Details = new Label(String.format(
                "Dossier: %s\nCatégorie: %s",
                pair.getDoc2().getDossier() != null ? pair.getDoc2().getDossier().getNom() : "N/A",
                pair.getDoc2().getCategorie() != null ? pair.getDoc2().getCategorie().getNom() : "N/A"));

        content.getChildren().addAll(
                doc1Title, doc1Details,
                new Separator(),
                doc2Title, doc2Details);

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
}
