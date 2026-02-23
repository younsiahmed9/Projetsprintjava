package tn.esprit;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.net.URL;
import java.util.List;

public class FxmlHeadlessChecker {
    public static void main(String[] args) throws Exception {
        URL url = FxmlHeadlessChecker.class.getResource("/fxml/login.fxml");
        if (url == null) {
            System.out.println("FXML not found on classpath: /fxml/login.fxml");
            return;
        }
        System.out.println("Loading FXML: " + url);
        Parent root = FXMLLoader.load(url);
        boolean found = findNodeById(root, "faceLoginBtn");
        System.out.println("faceLoginBtn present in FXML tree: " + found);
        boolean foundFaceLabel = findNodeById(root, "faceStatusLabel");
        System.out.println("faceStatusLabel present: " + foundFaceLabel);
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
}
