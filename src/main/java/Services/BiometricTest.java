package Services;

/**
 * Classe de test simple pour vérifier que BiometricAuthService fonctionne
 * Exécutez ce fichier pour tester la disponibilité du lecteur d'empreintes
 */
public class BiometricTest {
    
    public static void main(String[] args) {
        System.out.println("=== Test du Service d'Authentification Biométrique ===");
        System.out.println();
        
        try {
            BiometricAuthService service = BiometricAuthService.getInstance();
            
            System.out.println("1. Vérification de la disponibilité du lecteur...");
            boolean available = service.isAvailable();
            
            if (available) {
                System.out.println("   ✓ Lecteur d'empreintes DISPONIBLE");
                System.out.println();
                System.out.println("2. Test de capture d'empreinte...");
                System.out.println("   → Veuillez poser votre doigt sur le lecteur...");
                
                try {
                    byte[] template = service.captureFingerprint();
                    System.out.println("   ✓ Empreinte capturée avec succès!");
                    System.out.println("   → Taille du template: " + template.length + " octets");
                    
                    System.out.println();
                    System.out.println("3. Test de vérification...");
                    System.out.println("   → Veuillez reposer votre doigt...");
                    
                    boolean verified = service.verifyFingerprint(template);
                    if (verified) {
                        System.out.println("   ✓ Empreinte VÉRIFIÉE avec succès!");
                    } else {
                        System.out.println("   ✗ Empreinte NON RECONNUE");
                    }
                    
                } catch (BiometricAuthService.BiometricException e) {
                    System.out.println("   ✗ Erreur: " + e.getMessage());
                    System.out.println();
                    System.out.println("Note: Ceci peut être normal si vous n'avez pas posé votre doigt");
                }
                
            } else {
                System.out.println("   ✗ Lecteur d'empreintes NON DISPONIBLE");
                System.out.println();
                System.out.println("Raisons possibles:");
                System.out.println("  - Windows Hello n'est pas configuré");
                System.out.println("  - Le service Windows Biometric n'est pas démarré");
                System.out.println("  - Les drivers du lecteur HP ProBook ne sont pas installés");
                System.out.println("  - Vous n'êtes pas sur Windows 10/11");
                System.out.println();
                System.out.println("Solution:");
                System.out.println("  1. Ouvrez Paramètres Windows");
                System.out.println("  2. Allez dans Comptes > Options de connexion");
                System.out.println("  3. Configurez Windows Hello Empreinte digitale");
            }
            
            System.out.println();
            System.out.println("=== Test Terminé ===");
            
        } catch (Exception e) {
            System.err.println("ERREUR FATALE: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
