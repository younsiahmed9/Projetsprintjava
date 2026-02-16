package Controllers.Dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import utils.UiStyles;

public class DeleteConfirmationDialog extends Dialog<ButtonType> {

    public DeleteConfirmationDialog(String entityType, String entityName, String additionalInfo) {
        this.setTitle("⚠️ Confirmation de suppression");
        UiStyles.applyDialogStyles(this.getDialogPane());
        this.setWidth(500);

        VBox headerBox = createHeaderBox();
        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(20));

        Label titleLabel = new Label("Êtes-vous sûr de vouloir supprimer ?");
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        VBox entityBox = createEntityBox(entityType, entityName);
        Label warningLabel = new Label("⚠️  Cette action ne peut pas être annulée");
        warningLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11; -fx-font-weight: bold;");

        contentBox.getChildren().addAll(titleLabel, entityBox, warningLabel);
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            contentBox.getChildren().add(createAdditionalInfoBox(additionalInfo));
        }

        ButtonType deleteBtn = new ButtonType("🗑️  Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = ButtonType.CANCEL;

        this.getDialogPane().setHeader(headerBox);
        this.getDialogPane().setContent(contentBox);
        this.getDialogPane().getButtonTypes().addAll(deleteBtn, cancelBtn);

        Button deleteButton = (Button) this.getDialogPane().lookupButton(deleteBtn);
        if (deleteButton != null) {
            deleteButton.setStyle("-fx-padding: 10 20; -fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold;");
        }

        this.setResultConverter(buttonType -> buttonType);
    }

    private VBox createHeaderBox() {
        VBox headerBox = new VBox();
        headerBox.setPadding(new Insets(20));
        headerBox.setStyle("-fx-background-color: #fee2e2;");
        Label titleLabel = new Label("⚠️  Attention - Action irréversible");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #991b1b;");
        headerBox.getChildren().add(titleLabel);
        return headerBox;
    }

    private VBox createEntityBox(String entityType, String entityName) {
        VBox entityBox = new VBox(8);
        entityBox.setPadding(new Insets(15));
        entityBox.setStyle("-fx-background-color: #fafafa; -fx-border-color: #d1d5db; -fx-border-width: 1;");

        Label typeLabel = new Label("Type :");
        typeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #6b7280;");
        Label typeValue = new Label(entityType);
        HBox typeBox = new HBox(10, typeLabel, typeValue);

        Label nameLabel = new Label("Élément :");
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label nameValue = new Label("\"" + entityName + "\"");
        nameValue.setStyle("-fx-font-weight: bold;");
        nameValue.setWrapText(true);
        HBox nameBox = new HBox(10, nameLabel, nameValue);
        HBox.setHgrow(nameValue, Priority.ALWAYS);

        entityBox.getChildren().addAll(typeBox, nameBox);
        return entityBox;
    }

    private VBox createAdditionalInfoBox(String info) {
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: #fef3c7; -fx-border-color: #fde68a; -fx-border-width: 1;");

        Label infoLabel = new Label("ℹ️  Information importante :");
        infoLabel.setStyle("-fx-font-weight: bold;");

        Label infoValue = new Label(info);
        infoValue.setWrapText(true);

        infoBox.getChildren().addAll(infoLabel, infoValue);
        return infoBox;
    }

    public static boolean showAndWait(String entityType, String entityName, String additionalInfo) {
        DeleteConfirmationDialog dialog = new DeleteConfirmationDialog(entityType, entityName, additionalInfo);
        ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
        return result.getButtonData() == ButtonBar.ButtonData.OK_DONE;
    }

    public static boolean showAndWait(String entityType, String entityName) {
        return showAndWait(entityType, entityName, null);
    }
}

