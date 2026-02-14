package Models;

public class Session {
    private static Utilisateur utilisateurConnecte;
    private static Integer selectedPortefeuilleId;

    public static void setUtilisateur(Utilisateur utilisateur) {
        utilisateurConnecte = utilisateur;
    }

    public static Utilisateur getUtilisateur() {
        return utilisateurConnecte;
    }

    public static void clear() {
        utilisateurConnecte = null;
        selectedPortefeuilleId = null;
    }

    public static boolean isLoggedIn() {
        return utilisateurConnecte != null;
    }

    public static Integer getSelectedPortefeuilleId() {
        return selectedPortefeuilleId;
    }

    public static void setSelectedPortefeuilleId(Integer id) {
        selectedPortefeuilleId = id;
    }
}