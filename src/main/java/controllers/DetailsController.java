package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Map;

public class DetailsController {

    @FXML private Label titleLabel;
    @FXML private GridPane detailsGrid;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setDetails(String title, Map<String, String> details) {
        titleLabel.setText(title);
        detailsGrid.getChildren().clear();
        int rowIndex = 0;
        for (Map.Entry<String, String> entry : details.entrySet()) {
            Label label = new Label(entry.getKey());
            label.getStyleClass().add("details-label");
            Label value = new Label(entry.getValue());
            value.getStyleClass().add("details-value");
            detailsGrid.add(label, 0, rowIndex);
            detailsGrid.add(value, 1, rowIndex++);
        }
    }

    @FXML
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
