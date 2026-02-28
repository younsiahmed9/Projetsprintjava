package Controllers;

import Services.GroqApiService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class AiAssistantController {

    @FXML
    private TextArea userInputArea;
    @FXML
    private TextArea aiSuggestionArea;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Button improveBtn;

    private final GroqApiService groqService = new GroqApiService();

    @FXML
    public void onImproveMessage(ActionEvent event) {
        String input = userInputArea.getText();
        if (input == null || input.isBlank()) {
            return;
        }

        loadingIndicator.setVisible(true);
        improveBtn.setDisable(true);
        aiSuggestionArea.setText("Réflexion en cours...");

        groqService.getFintechSuggestion(input)
                .thenAccept(suggestion -> Platform.runLater(() -> {
                    aiSuggestionArea.setText(suggestion);
                    loadingIndicator.setVisible(false);
                    improveBtn.setDisable(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        aiSuggestionArea.setText("Erreur : " + ex.getMessage());
                        loadingIndicator.setVisible(false);
                        improveBtn.setDisable(false);
                    });
                    return null;
                });
    }

    @FXML
    public void onCopy(ActionEvent event) {
        String text = aiSuggestionArea.getText();
        if (text != null && !text.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            clipboard.setContent(content);
        }
    }
}
