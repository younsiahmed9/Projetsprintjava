package utils;

import javafx.scene.control.DialogPane;

public final class UiStyles {
    private UiStyles() {
    }

    public static void applyDialogStyles(DialogPane pane) {
        if (pane == null) {
            return;
        }
        String css = UiStyles.class.getResource("/css/styles.css").toExternalForm();
        if (!pane.getStylesheets().contains(css)) {
            pane.getStylesheets().add(css);
        }
        if (!pane.getStyleClass().contains("main-container")) {
            pane.getStyleClass().add("main-container");
        }
    }
}

