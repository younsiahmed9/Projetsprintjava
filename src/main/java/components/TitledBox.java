package components;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class TitledBox extends VBox {

    public TitledBox(String title) {
        super(10);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #182d88;");

        VBox contentBox = new VBox(10);
        contentBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(24,45,136,0.1), 10, 0, 0, 2);");

        this.getChildren().addAll(titleLabel, contentBox);
    }

    public VBox getContentBox() {
        return (VBox) this.getChildren().get(1);
    }
}