package Controllers.Dialogs;

import Models.Document;
import Controllers.AlertUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import utils.UiStyles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import java.awt.Color;
import java.awt.Desktop;

public class TranslateActionDialog {

    public static void showTranslateDialog(Document doc) {
        if (doc == null)
            return;

        Dialog<Void> dialog = new Dialog<>();
        UiStyles.applyDialogStyles(dialog.getDialogPane());
        dialog.setTitle("Traduction Professionnelle");
        dialog.setTitle("Fintrack - Traduction Intelligente");
        dialog.setHeaderText(null); // Custom header inside content for more control

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setPrefWidth(900);
        content.setPrefHeight(800);
        content.setAlignment(Pos.TOP_CENTER);

        // Professional Header
        VBox headerBox = new VBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        Label headerTitle = new Label("🌐 Traduction Professionnelle");
        headerTitle.setStyle("-fx-font-size: 24; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        Label headerSubtitle = new Label("Traduisez instantanément vos documents (Titre, Description, Path, OCR)");
        headerSubtitle.setStyle("-fx-font-size: 14; -fx-text-fill: #64748b;");
        headerBox.getChildren().addAll(headerTitle, headerSubtitle);

        // Language selection
        GridPane langGrid = new GridPane();
        langGrid.setHgap(15);
        langGrid.setVgap(10);
        langGrid.setAlignment(Pos.CENTER);

        ComboBox<String> cbSource = new ComboBox<>();
        cbSource.getItems().addAll("Automatique (auto)", "Français (fr)", "Anglais (en)", "Espagnol (es)", "Arabe (ar)",
                "Allemand (de)", "Italien (it)");
        cbSource.getSelectionModel().selectFirst();
        cbSource.getStyleClass().add("modern-input");

        ComboBox<String> cbTarget = new ComboBox<>();
        cbTarget.getItems().addAll("Français (fr)", "Anglais (en)", "Espagnol (es)", "Arabe (ar)", "Allemand (de)",
                "Italien (it)");
        cbTarget.getSelectionModel().select(1); // Default to Anglais
        cbTarget.getStyleClass().add("modern-input");

        langGrid.add(new Label("De :"), 0, 0);
        langGrid.add(cbSource, 1, 0);
        langGrid.add(new Label("Vers :"), 2, 0);
        langGrid.add(cbTarget, 3, 0);

        // Text Areas
        Label lblSource = new Label("📋 SOURCE : Document complet");
        lblSource.setStyle("-fx-font-size: 13; -fx-font-weight: 800; -fx-text-fill: #475569; -fx-padding: 0 0 5 0;");

        TextArea taSource = new TextArea();
        taSource.setWrapText(true);
        taSource.setPrefRowCount(12);
        taSource.getStyleClass().add("modern-input");
        taSource.setStyle("-fx-font-size: 14; -fx-font-family: 'Segoe UI', Tahoma, sans-serif;");

        // Helper to populate source text
        populateSourceText(taSource, doc);

        Label lblTarget = new Label("✨ TRADUCTION : Résultat");
        lblTarget.setStyle("-fx-font-size: 13; -fx-font-weight: 800; -fx-text-fill: #2563eb; -fx-padding: 10 0 5 0;");
        TextArea taTarget = new TextArea();
        taTarget.setWrapText(true);
        taTarget.setEditable(false);
        taTarget.setPrefRowCount(14); // Increased size
        taTarget.getStyleClass().add("modern-input");
        taTarget.setStyle(
                "-fx-font-size: 14; -fx-font-family: 'Segoe UI', sans-serif; -fx-border-color: #2563eb; -fx-border-width: 1px;");
        taTarget.setPromptText("Le texte traduit apparaîtra ici...");

        ProgressIndicator progress = new ProgressIndicator();
        progress.setVisible(false);
        progress.setMaxSize(30, 30);

        Button btnTranslate = new Button("🔄 Traduire Tout");
        btnTranslate.getStyleClass().add("btn-primary");
        btnTranslate.setMaxWidth(Double.MAX_VALUE);
        btnTranslate.setStyle("-fx-padding: 12; -fx-font-size: 14;");

        Button btnScan = new Button("🔍 Extraire Texte PDF (OCR)");
        btnScan.getStyleClass().add("btn-secondary");
        btnScan.setMaxWidth(Double.MAX_VALUE);
        btnScan.setVisible(doc.getContenuTexte() == null || doc.getContenuTexte().trim().isEmpty());

        Button btnExport = new Button("📄 Exporter en PDF");
        btnExport.getStyleClass().add("btn-secondary");
        btnExport.setMaxWidth(Double.MAX_VALUE);
        btnExport.setStyle("-fx-padding: 10; -fx-background-color: #0f172a; -fx-text-fill: white;");
        btnExport.setDisable(true); // Disable until translated

        btnTranslate.setOnAction(e -> {
            String text = taSource.getText();
            String sl = getLangCode(cbSource.getValue(), "auto");
            String tl = getLangCode(cbTarget.getValue(), "en");

            if (text.trim().isEmpty()) {
                AlertUtils.showError("Erreur", "Veuillez saisir du texte à traduire.");
                return;
            }

            btnTranslate.setDisable(true);
            progress.setVisible(true);
            taTarget.setText("Traduction en cours...");

            new Thread(() -> {
                String result = translateText(text, sl, tl);
                Platform.runLater(() -> {
                    taTarget.setText(result);
                    btnTranslate.setDisable(false);
                    btnExport.setDisable(false); // Enable export
                    progress.setVisible(false);
                });
            }).start();
        });

        btnExport.setOnAction(e -> {
            exportToPDF(taTarget.getText(), cbTarget.getValue(), doc);
        });

        btnScan.setOnAction(e -> {
            btnScan.setDisable(true);
            progress.setVisible(true);
            taSource.setText("Scan OCR en cours... Veuillez patienter.");

            new Thread(() -> {
                try {
                    Services.ServiceScanner scanner = new Services.ServiceScanner();
                    String extracted = scanner.scanFile(new File(doc.getFilePath()));
                    doc.setContenuTexte(extracted); // Cache locally
                    Platform.runLater(() -> {
                        populateSourceText(taSource, doc);
                        btnScan.setVisible(false);
                        progress.setVisible(false);
                        btnScan.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        AlertUtils.showError("Erreur OCR", "Impossible de scanner le fichier : " + ex.getMessage());
                        populateSourceText(taSource, doc);
                        btnScan.setDisable(false);
                        progress.setVisible(false);
                    });
                }
            }).start();
        });

        content.getChildren().addAll(headerBox, new Separator(), langGrid, lblSource, taSource, btnScan, btnTranslate,
                progress, lblTarget, taTarget, btnExport);
        VBox.setVgrow(taSource, Priority.ALWAYS);
        VBox.setVgrow(taTarget, Priority.ALWAYS);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Button closeButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        if (closeButton != null)
            closeButton.getStyleClass().add("btn-cancel");

        dialog.showAndWait();
    }

    private static void populateSourceText(TextArea ta, Document doc) {
        StringBuilder sb = new StringBuilder();
        sb.append("Titre : ").append(doc.getTitre()).append("\n");
        sb.append("Description : ").append(doc.getDescription() != null ? doc.getDescription() : "N/A").append("\n");
        sb.append("Chemin : ").append(doc.getFilePath()).append("\n");

        if (doc.getContenuTexte() != null && !doc.getContenuTexte().trim().isEmpty()) {
            sb.append("\n--- CONTENU EXTRAIT DU PDF ---\n");
            sb.append(doc.getContenuTexte());
        } else {
            sb.append("\n(Contenu du PDF non extrait. Utilisez le bouton 'Extraire Texte' si besoin.)");
        }
        ta.setText(sb.toString());
    }

    private static String getLangCode(String selection, String defaultCode) {
        if (selection == null)
            return defaultCode;
        int start = selection.indexOf("(");
        int end = selection.indexOf(")");
        if (start != -1 && end != -1)
            return selection.substring(start + 1, end);
        return defaultCode;
    }

    private static String translateText(String text, String sl, String tl) {
        try {
            String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + sl + "&tl=" + tl
                    + "&dt=t&q=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();

            String json = response.toString();
            return parseGoogleTranslateJson(json);
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la traduction : " + e.getMessage();
        }
    }

    private static String parseGoogleTranslateJson(String json) {
        try {
            StringBuilder result = new StringBuilder();
            com.google.gson.JsonArray jsonArray = com.google.gson.JsonParser.parseString(json).getAsJsonArray();

            if (jsonArray.size() > 0 && jsonArray.get(0).isJsonArray()) {
                com.google.gson.JsonArray segments = jsonArray.get(0).getAsJsonArray();
                for (com.google.gson.JsonElement segmentElement : segments) {
                    if (segmentElement.isJsonArray()) {
                        com.google.gson.JsonArray segment = segmentElement.getAsJsonArray();
                        if (segment.size() > 0 && !segment.get(0).isJsonNull()) {
                            result.append(segment.get(0).getAsString());
                        }
                    }
                }
                return result.toString();
            }
            return "Format de réponse inconnu.";
        } catch (Exception e) {
            return "Erreur d'analyse : " + json;
        }
    }

    private static void exportToPDF(String text, String language, Models.Document doc) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la Traduction");
        fileChooser.setInitialFileName("Traduction_" + doc.getTitre().replaceAll("\\s+", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file == null)
            return;

        com.lowagie.text.Document pdfDoc = new com.lowagie.text.Document(PageSize.A4, 40, 40, 50, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(pdfDoc, new FileOutputStream(file));
            pdfDoc.open();

            Color primaryBlue = new Color(30, 58, 138);
            Color grayText = new Color(100, 116, 139);

            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell cell = new PdfPCell(new Phrase("FINTRACK - TRADUCTION DE DOCUMENT",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, primaryBlue)));
            cell.setBorder(Rectangle.BOTTOM);
            cell.setPaddingBottom(10);
            cell.setBorderColor(Color.LIGHT_GRAY);
            header.addCell(cell);
            pdfDoc.add(header);

            pdfDoc.add(new Paragraph(" "));

            pdfDoc.add(new Paragraph("Titre Original : " + doc.getTitre(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
            pdfDoc.add(new Paragraph("Langue de Destination : " + language,
                    FontFactory.getFont(FontFactory.HELVETICA, 10, grayText)));
            pdfDoc.add(new Paragraph(
                    "Date d'export : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    FontFactory.getFont(FontFactory.HELVETICA, 9, grayText)));

            pdfDoc.add(new Paragraph(" "));
            pdfDoc.add(new Phrase("______________________________________________________________________________",
                    FontFactory.getFont(FontFactory.HELVETICA, 10, Color.LIGHT_GRAY)));
            pdfDoc.add(new Paragraph(" "));

            Paragraph contentPara = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 12));
            contentPara.setLeading(16f);
            pdfDoc.add(contentPara);

            PdfPTable footer = new PdfPTable(1);
            footer.setWidthPercentage(100);
            PdfPCell fCell = new PdfPCell(new Phrase("Document généré par FinTrack Intelligence Engine",
                    FontFactory.getFont(FontFactory.HELVETICA, 8, Color.LIGHT_GRAY)));
            fCell.setBorder(Rectangle.TOP);
            fCell.setPaddingTop(20);
            fCell.setBorderColor(Color.LIGHT_GRAY);
            fCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            footer.addCell(fCell);

            footer.setTotalWidth(pdfDoc.getPageSize().getWidth() - 80);
            footer.writeSelectedRows(0, -1, 40, 40, writer.getDirectContent());

            pdfDoc.close();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            AlertUtils.showError("Erreur d'export", "Impossible de générer le PDF : " + ex.getMessage());
        }
    }
}
