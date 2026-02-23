package Controllers;

import Models.User;

/** Etat simple en mémoire pour partager l'utilisateur connecté entre les écrans. */
public final class AppState {
    private static User currentUser;

    private AppState() {
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User u) {
        currentUser = u;
    }

    public static void clear() {
        currentUser = null;
    }
}
