package tn.esprit;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.net.URL;
import java.util.List;

public class FxmlRegisterChecker {
    public static void main(String[] args) throws Exception {
        URL url = FxmlRegisterChecker.class.getResource("/fxml/register.fxml");
        if (url == null) {
            System.out.println("FXML not found on classpath: /fxml/register.fxml");
            return;
        }
        System.out.println("Loading FXML: " + url);
        Parent root = FXMLLoader.load(url);
        boolean foundFaceBtn = findNodeById(root, "faceBtn");
        boolean foundFaceStatus = findNodeById(root, "faceStatusLabel");
        boolean foundDebugText = findNodeByText(root, "⚠️ DEBUG: FACE BUTTON BLOCK");
        System.out.println("faceBtn present in FXML tree: " + foundFaceBtn);
        System.out.println("faceStatusLabel present: " + foundFaceStatus);
        System.out.println("DEBUG text present: " + foundDebugText);
    }

    private static boolean findNodeById(Node node, String id) {
        if (node == null) return false;
        if (id.equals(node.getId())) return true;
        if (node instanceof Parent parent) {
            List<Node> children = parent.getChildrenUnmodifiable();
            for (Node c : children) {
                if (findNodeById(c, id)) return true;
            }
        }
        return false;
    }

    private static boolean findNodeByText(Node node, String text) {
        if (node == null) return false;
        try {
            String nodeText = (String) node.getProperties().get("text");
            // Not reliable; instead check toString
        } catch (Exception ignored) {}
        if (node.toString().contains(text)) return true;
        if (node instanceof Parent parent) {
            for (Node c : parent.getChildrenUnmodifiable()) {
                if (findNodeByText(c, text)) return true;
            }
        }
        return false;
    }
}
