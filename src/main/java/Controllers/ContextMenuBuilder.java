package Controllers;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * Constructeur professionnel de menus contextuels
 * Fournit des menus standardisés pour les opérations CRUD
 */
public class ContextMenuBuilder {

    /**
     * Crée un menu contextuel pour les opérations de suppression sécurisée
     */
    public static ContextMenu createDeleteMenu(String entityName, Runnable onDelete) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem deleteItem = new MenuItem("🗑️  Supprimer \"" + entityName + "\"");
        deleteItem.setStyle(
            "-fx-text-fill: #dc2626; " +
            "-fx-font-weight: bold;"
        );
        deleteItem.setOnAction(e -> onDelete.run());

        contextMenu.getItems().add(deleteItem);
        return contextMenu;
    }

    /**
     * Crée un menu contextuel complet avec édition et suppression
     */
    public static ContextMenu createEditDeleteMenu(String entityName, Runnable onEdit, Runnable onDelete) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("✎  Modifier");
        editItem.setStyle("-fx-font-size: 11;");
        editItem.setOnAction(e -> onEdit.run());

        MenuItem deleteItem = new MenuItem("🗑️  Supprimer");
        deleteItem.setStyle(
            "-fx-text-fill: #dc2626; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 11;"
        );
        deleteItem.setOnAction(e -> onDelete.run());

        contextMenu.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);
        return contextMenu;
    }

    /**
     * Crée un menu contextuel avec 3 actions : Éditer, Dupliquer, Supprimer
     */
    public static ContextMenu createFullCrudMenu(
            String entityName,
            Runnable onEdit,
            Runnable onDuplicate,
            Runnable onDelete) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("✎  Modifier");
        editItem.setOnAction(e -> onEdit.run());

        MenuItem duplicateItem = new MenuItem("⎘  Dupliquer");
        duplicateItem.setOnAction(e -> onDuplicate.run());

        MenuItem deleteItem = new MenuItem("🗑️  Supprimer");
        deleteItem.setStyle(
            "-fx-text-fill: #dc2626; " +
            "-fx-font-weight: bold;"
        );
        deleteItem.setOnAction(e -> onDelete.run());

        contextMenu.getItems().addAll(editItem, duplicateItem, new SeparatorMenuItem(), deleteItem);
        return contextMenu;
    }

    /**
     * Crée un menu contextuel avec actions personnalisées
     */
    public static class Builder {
        private final ContextMenu menu = new ContextMenu();

        public Builder addItem(String label, Runnable action) {
            MenuItem item = new MenuItem(label);
            item.setOnAction(e -> action.run());
            menu.getItems().add(item);
            return this;
        }

        public Builder addItem(String label, String style, Runnable action) {
            MenuItem item = new MenuItem(label);
            item.setStyle(style);
            item.setOnAction(e -> action.run());
            menu.getItems().add(item);
            return this;
        }

        public Builder addSeparator() {
            menu.getItems().add(new SeparatorMenuItem());
            return this;
        }

        public ContextMenu build() {
            return menu;
        }
    }
}

