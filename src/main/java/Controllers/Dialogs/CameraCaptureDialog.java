package Controllers.Dialogs;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Boîte de dialogue professionnelle pour capturer un document via Webcam.
 */
public class CameraCaptureDialog {

    private Webcam webcam;
    private File capturedFile;

    public Optional<File> showAndWait() {
        Dialog<File> dialog = new Dialog<>();
        dialog.setTitle("Capture Caméra - OCR");
        dialog.setHeaderText("Alignez votre document dans le cadre et capturez");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f1f5f9;");

        // Initialisation de la webcam
        try {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                root.getChildren().add(new Label("⚠️ Aucune caméra détectée sur ce système."));
            } else {
                // Tentative d'utiliser HD (1280x720) pour une netteté maximale
                java.awt.Dimension hdSize = WebcamResolution.VGA.getSize(); // VGA plus stable sur toutes les machines
                webcam.setViewSize(hdSize);

                // Panel Swing intégré dans JavaFX
                WebcamPanel panel = new WebcamPanel(webcam);
                panel.setDrawMode(WebcamPanel.DrawMode.FILL); // Remplit toute la zone du panel
                panel.setFPSDisplayed(false);
                panel.setDisplayDebugInfo(false);
                panel.setImageSizeDisplayed(false);
                panel.setMirrored(true);

                // On fixe une taille préférée large pour le panel Swing
                panel.setPreferredSize(new java.awt.Dimension(800, 600));

                SwingNode swingNode = new SwingNode();
                swingNode.setContent(panel);

                root.getChildren().add(swingNode);

                // Redimensionnement forcé du container pour éviter le "collapse"
                root.setPrefHeight(650);
                root.setMinWidth(800);
                dialog.getDialogPane().setPrefSize(850, 780);

                Button btnCapture = new Button("📸 Capturer le document");
                btnCapture.setStyle(
                        "-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 25; -fx-cursor: hand;");

                btnCapture.setOnAction(e -> {
                    captureImage();
                    dialog.setResult(capturedFile);
                    dialog.close();
                });

                root.getChildren().add(btnCapture);
            }
        } catch (Exception e) {
            root.getChildren().add(new Label("Erreur caméra: " + e.getMessage()));
        }

        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        dialog.setOnCloseRequest(e -> {
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }
        });

        return dialog.showAndWait();
    }

    private void captureImage() {
        if (webcam != null) {
            BufferedImage image = webcam.getImage();
            try {
                File tempFile = File.createTempFile("scan_capture_", ".jpg");
                ImageIO.write(image, "JPG", tempFile);
                this.capturedFile = tempFile;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                webcam.close();
            }
        }
    }
}
