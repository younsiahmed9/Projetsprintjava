package Controllers;

import Models.Compte;
import Models.Credit;
import Services.ServiceCompte;
import Services.ServiceCredit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.image.ImageDataFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.awt.Desktop;
import java.util.List;

public class DetailCompteController {

    @FXML private Label lblNumero, lblType, lblSolde, lblEtat, lblSpecifiqueTitre, lblSpecifiqueValeur;
    @FXML private Button btnCredits, btnPDF;

    private Compte compteActuel;
    private ServiceCompte service = new ServiceCompte();

    @FXML
    public void initialize() {
        if (btnPDF != null) {
            btnPDF.setOnMouseEntered(e -> {
                btnPDF.setStyle("-fx-background-color: #c0392b; -fx-background-radius: 50; -fx-text-fill: white; -fx-font-size: 22; -fx-cursor: hand; -fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 12, 0, 0, 5);");
                btnPDF.setScaleX(1.1);
                btnPDF.setScaleY(1.1);
            });
            btnPDF.setOnMouseExited(e -> {
                btnPDF.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 50; -fx-text-fill: white; -fx-font-size: 20; -fx-cursor: hand; -fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);");
                btnPDF.setScaleX(1.0);
                btnPDF.setScaleY(1.0);
            });
        }
    }

    public void setCompteId(int id) {
        try {
            this.compteActuel = service.recupererParId(id);
            remplirChamps();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void remplirChamps() {
        if (compteActuel != null) {
            lblNumero.setText(compteActuel.getNumeroCompte());
            lblType.setText("COMPTE " + compteActuel.getTypeCompte().toUpperCase());
            lblSolde.setText(String.format("%.2f DT", compteActuel.getSolde()));

            lblEtat.setText(compteActuel.getEtat().toUpperCase());
            lblEtat.getStyleClass().removeAll("badge-actif", "badge-bloque");
            if(compteActuel.getEtat().equalsIgnoreCase("ACTIF")) {
                lblEtat.getStyleClass().add("badge-actif");
            } else {
                lblEtat.getStyleClass().add("badge-bloque");
            }

            if ("EPARGNE".equalsIgnoreCase(compteActuel.getTypeCompte())) {
                lblSpecifiqueTitre.setText("TAUX D'INTÉRÊT ANNUEL");
                lblSpecifiqueValeur.setText(compteActuel.getTauxInteret() + " %");
                btnCredits.setVisible(false);
                btnCredits.setManaged(false);
            } else {
                lblSpecifiqueTitre.setText("PLAFOND DE DÉCOUVERT");
                lblSpecifiqueValeur.setText(compteActuel.getPlafondDecouvert() + " DT");
                btnCredits.setVisible(true);
                btnCredits.setManaged(true);
            }
        }
    }

    @FXML
    private void handleModifier() {
        // --- CORRECTION : Utilisation de Parent au lieu de VBox ---
        Parent root = lblNumero.getScene().getRoot();
        root.setOpacity(0.5);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierCompte.fxml"));
            Parent editRoot = loader.load();
            ModifierCompteController controller = loader.getController();
            controller.setCompte(compteActuel);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(editRoot);
            scene.setFill(null);
            stage.setScene(scene);
            stage.showAndWait();

            if (controller.isModificationReussie()) {
                remplirChamps();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            root.setOpacity(1.0);
        }
    }

    @FXML
    private void handleSupprimer() {
        // --- CORRECTION : Utilisation de Parent au lieu de VBox ---
        Parent root = lblNumero.getScene().getRoot();
        root.setOpacity(0.4);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SupprimerCompte.fxml"));
            Parent popupRoot = loader.load();
            SupprimerCompteController controller = loader.getController();
            controller.setCompte(compteActuel);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(popupRoot);
            scene.setFill(null);
            stage.setScene(scene);
            stage.showAndWait();

            if (controller.isSuppressionConfirmee()) {
                handleFermer();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            root.setOpacity(1.0);
        }
    }

    @FXML
    private void handleConsulterCredits() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AfficherCredit.fxml"));
            Parent root = loader.load();
            AfficherCreditController controller = loader.getController();
            controller.setCompteData(compteActuel.getIdCompte(), compteActuel.getNumeroCompte());
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Tableau de bord - Crédits");
            stage.setMaximized(true);
            stage.show();
            handleFermer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFermer() {
        if (lblNumero.getScene() != null && lblNumero.getScene().getWindow() != null) {
            Stage stage = (Stage) lblNumero.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void handleGenererPDF() {
        String numeroCompte = lblNumero.getText();
        String fileName = "Releve_Compte_" + numeroCompte + ".pdf";

        try {
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // --- 1. LOGO FINTRACK ---
            try {
                URL logoUrl = getClass().getResource("/assets/logo.png");
                if (logoUrl != null) {
                    Image logo = new Image(ImageDataFactory.create(logoUrl));
                    logo.setWidth(150);
                    logo.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.LEFT);
                    logo.setMarginBottom(10);
                    document.add(logo);
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement logo : " + e.getMessage());
            }

            // --- 2. TITRE ---
            document.add(new Paragraph("RELEVÉ DE COMPTE")
                    .setBold().setFontSize(22).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            // --- 3. INFORMATIONS ---
            document.add(new Paragraph("Informations Générales").setBold().setFontSize(14).setUnderline());
            Table table = new Table(2).useAllAvailableWidth();
            table.addCell("Numéro de Compte"); table.addCell(numeroCompte);
            table.addCell("Type de Compte"); table.addCell(lblType.getText());
            table.addCell("État du Compte"); table.addCell(lblEtat.getText());
            table.addCell("Solde Actuel"); table.addCell(lblSolde.getText());

            if (lblSpecifiqueValeur.getText() != null && !lblSpecifiqueValeur.getText().isEmpty()) {
                table.addCell(lblSpecifiqueTitre.getText());
                table.addCell(lblSpecifiqueValeur.getText());
            }
            document.add(table.setMarginBottom(20));

            // --- 4. LISTE DES CRÉDITS ---
            if (lblType.getText().contains("COURANT")) {
                document.add(new Paragraph("Détails des Crédits Associés").setBold().setFontSize(14).setUnderline().setMarginTop(10));
                ServiceCredit sc = new ServiceCredit();
                List<Credit> creditsList = sc.recupererParCompte(compteActuel.getIdCompte());

                if (creditsList.isEmpty()) {
                    document.add(new Paragraph("Aucun crédit associé à ce compte.").setItalic());
                } else {
                    Table creditTable = new Table(4).useAllAvailableWidth();
                    creditTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Montant").setBold()));
                    creditTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Mensualité").setBold()));
                    creditTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Taux").setBold()));
                    creditTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Statut").setBold()));

                    for (Models.Credit c : creditsList) {
                        creditTable.addCell(String.format("%.2f DT", c.getMontant()));
                        creditTable.addCell(String.format("%.2f DT", c.getMensualite()));
                        creditTable.addCell(c.getTauxInteret() + " %");
                        creditTable.addCell(c.getStatut());
                    }
                    document.add(creditTable.setMarginBottom(30));
                }
            }

            // --- 5. QR CODE ---
            String dateInfo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String qrData = "FinTrack - Certifié\nCompte: " + numeroCompte + "\nSolde: " + lblSolde.getText() + "\nDate: " + dateInfo;
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 130, 130);
            Path tempQrPath = new File("temp_qr.png").toPath();
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", tempQrPath);

            Image qrImage = new Image(ImageDataFactory.create("temp_qr.png"));
            document.add(new Paragraph("Validation Numérique :").setItalic().setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            document.add(qrImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER));

            document.close();
            new File("temp_qr.png").delete();

            File pdfFile = new File(fileName);
            if (Desktop.isDesktopSupported() && pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Le relevé a été généré avec succès.");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).showAndWait();
        }
    }
}