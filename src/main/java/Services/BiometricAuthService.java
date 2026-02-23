package Services;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Service d'authentification biométrique utilisant Windows Biometric Framework (WBF)
 * Compatible avec les lecteurs d'empreintes digitales HP ProBook via Windows Hello
 */
public class BiometricAuthService {

    private static final int WINBIO_TYPE_FINGERPRINT = 0x00000008;
    private static final int WINBIO_POOL_SYSTEM = 0x00000001;
    private static final int WINBIO_FLAG_DEFAULT = 0x00000000;

    // Codes de retour Windows Biometric Framework
    private static final int S_OK = 0;
    private static final int WINBIO_E_ENROLLMENT_IN_PROGRESS = 0x8009802D;

    private static BiometricAuthService instance;
    private boolean available = false;

    // Interface JNA pour winbio.dll
    public interface WinBio extends StdCallLibrary {
        WinBio INSTANCE = Native.load("winbio", WinBio.class, W32APIOptions.DEFAULT_OPTIONS);

        int WinBioOpenSession(
            int Factor,
            int PoolType,
            int Flags,
            Pointer UnitArray,
            int UnitCount,
            Pointer DatabaseId,
            IntByReference SessionHandle
        );

        int WinBioCloseSession(int SessionHandle);

        int WinBioVerify(
            int SessionHandle,
            IntByReference Identity,
            IntByReference SubFactor,
            IntByReference Match,
            IntByReference RejectDetail
        );

        int WinBioCaptureSample(
            int SessionHandle,
            int Purpose,
            IntByReference RejectDetail,
            Pointer Sample,
            IntByReference SampleSize
        );

        int WinBioEnrollBegin(
            int SessionHandle,
            int SubFactor,
            int UnitId
        );

        int WinBioEnrollCapture(
            int SessionHandle,
            IntByReference RejectDetail
        );

        int WinBioEnrollCommit(
            int SessionHandle,
            IntByReference Identity,
            IntByReference IsNewTemplate
        );

        int WinBioEnrollDiscard(int SessionHandle);
    }

    private WinBio winbio;

    private BiometricAuthService() {
        try {
            winbio = WinBio.INSTANCE;
            // Tester la disponibilité
            IntByReference sessionHandle = new IntByReference();
            int result = winbio.WinBioOpenSession(
                WINBIO_TYPE_FINGERPRINT,
                WINBIO_POOL_SYSTEM,
                WINBIO_FLAG_DEFAULT,
                null, 0, null,
                sessionHandle
            );

            if (result == S_OK) {
                available = true;
                winbio.WinBioCloseSession(sessionHandle.getValue());
            }
        } catch (Exception e) {
            System.err.println("Windows Biometric Framework non disponible: " + e.getMessage());
            available = false;
        }
    }

    public static synchronized BiometricAuthService getInstance() {
        if (instance == null) {
            instance = new BiometricAuthService();
        }
        return instance;
    }

    /**
     * Vérifie si le lecteur d'empreintes est disponible
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Capture une empreinte digitale et retourne son template sous forme de hash
     * Pour des raisons de sécurité, on ne stocke pas le template brut mais un hash
     */
    public byte[] captureFingerprint() throws BiometricException {
        System.out.println("[BiometricAuthService] ====== DÉBUT CAPTURE EMPREINTE ======");

        if (!available) {
            System.err.println("[BiometricAuthService] ❌ Lecteur non disponible");
            throw new BiometricException("Lecteur d'empreintes non disponible");
        }

        System.out.println("[BiometricAuthService] ✅ Lecteur disponible");

        IntByReference sessionHandle = new IntByReference();
        try {
            // Ouvrir une session biométrique
            System.out.println("[BiometricAuthService] Ouverture session biométrique...");

            int result = winbio.WinBioOpenSession(
                WINBIO_TYPE_FINGERPRINT,
                WINBIO_POOL_SYSTEM,
                WINBIO_FLAG_DEFAULT,
                null, 0, null,
                sessionHandle
            );

            System.out.println("[BiometricAuthService] WinBioOpenSession result: 0x" + Integer.toHexString(result));

            if (result != S_OK) {
                System.err.println("[BiometricAuthService] ❌ Échec ouverture session: 0x" + Integer.toHexString(result));
                throw new BiometricException("Impossible d'ouvrir une session biométrique (code: 0x" + Integer.toHexString(result) + ")");
            }

            System.out.println("[BiometricAuthService] ✅ Session ouverte, handle: " + sessionHandle.getValue());
            System.out.println("[BiometricAuthService] ⏳ En attente de votre empreinte digitale...");
            System.out.println("[BiometricAuthService] 👆 POSEZ VOTRE DOIGT SUR LE CAPTEUR MAINTENANT !");

            // Utiliser WinBioVerify pour capturer l'identité biométrique
            // C'est plus simple et plus rapide que WinBioCaptureSample
            IntByReference identity = new IntByReference();
            IntByReference subFactor = new IntByReference();
            IntByReference match = new IntByReference();
            IntByReference rejectDetail = new IntByReference();

            System.out.println("[BiometricAuthService] Appel WinBioVerify (scan du doigt)...");

            // Mesurer le temps de scan
            long startTime = System.currentTimeMillis();

            result = winbio.WinBioVerify(
                sessionHandle.getValue(),
                identity,
                subFactor,
                match,
                rejectDetail
            );

            long scanDuration = System.currentTimeMillis() - startTime;
            System.out.println("[BiometricAuthService] Scan terminé en " + scanDuration + " ms");
            System.out.println("[BiometricAuthService] WinBioVerify result: 0x" + Integer.toHexString(result));
            System.out.println("[BiometricAuthService] Identity: " + identity.getValue());
            System.out.println("[BiometricAuthService] SubFactor: " + subFactor.getValue());
            System.out.println("[BiometricAuthService] Match: " + match.getValue());
            System.out.println("[BiometricAuthService] RejectDetail: 0x" + Integer.toHexString(rejectDetail.getValue()));

            // Codes d'erreur acceptables :
            // 0x00000000 (S_OK) = Empreinte reconnue (utilisateur déjà enregistré)
            // 0x80098011 (WINBIO_E_NO_MATCH) = Empreinte non reconnue (nouveau doigt - PARFAIT pour l'enregistrement !)
            // 0x8009800B (WINBIO_E_BAD_CAPTURE) = Mauvaise capture (réessayer)

            if (result != S_OK && result != 0x80098011) {
                // Erreur réelle (pas juste "non reconnu")
                String errorMsg = "Scan échoué (code: 0x" + Integer.toHexString(result) + ")";
                if (rejectDetail.getValue() != 0) {
                    errorMsg += ", rejet: 0x" + Integer.toHexString(rejectDetail.getValue());
                }
                System.err.println("[BiometricAuthService] ❌ " + errorMsg);
                throw new BiometricException(errorMsg);
            }

            System.out.println("[BiometricAuthService] ✅ Doigt scanné avec succès !");

            // CAPTURE DES VRAIES DONNÉES BIOMÉTRIQUES via WinBioCaptureSample
            System.out.println("[BiometricAuthService] Capture des données biométriques brutes...");

            // Allouer un buffer pour les données brutes de l'empreinte
            int maxSampleSize = 65536; // 64 KB devrait suffire
            Memory sampleBuffer = new Memory(maxSampleSize);
            IntByReference actualSampleSize = new IntByReference(maxSampleSize);
            IntByReference rejectDetail2 = new IntByReference();

            result = winbio.WinBioCaptureSample(
                sessionHandle.getValue(),
                1, // Purpose = Verify/Identify (1)
                rejectDetail2,
                sampleBuffer,
                actualSampleSize
            );

            if (result != S_OK) {
                System.err.println("[BiometricAuthService] ⚠️ Échec WinBioCaptureSample: 0x" + Integer.toHexString(result));
                // Fallback : utiliser identity + subfactor
                System.out.println("[BiometricAuthService] Utilisation du fallback (identity + subfactor)");
                String fallbackData = String.format("%d-%d", identity.getValue(), subFactor.getValue());
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(fallbackData.getBytes("UTF-8"));
                System.out.println("[BiometricAuthService] ✅ Empreinte capturée (fallback) → hash: " +
                                  bytesToHex(hash).substring(0, 16) + "... (longueur: " + hash.length + " bytes)");
                return hash;
            }

            int sampleSize = actualSampleSize.getValue();
            System.out.println("[BiometricAuthService] ✅ Données biométriques capturées : " + sampleSize + " bytes");

            // Lire les données brutes
            byte[] rawSample = sampleBuffer.getByteArray(0, sampleSize);

            // Hasher les données brutes pour stockage sécurisé
            System.out.println("[BiometricAuthService] Génération hash SHA-256 des données biométriques...");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawSample);

            System.out.println("[BiometricAuthService] ✅ Empreinte capturée : " + sampleSize + " bytes → hash: " +
                              bytesToHex(hash).substring(0, 16) + "... (longueur: " + hash.length + " bytes)");
            System.out.println("[BiometricAuthService] ====== FIN CAPTURE EMPREINTE ======");

            return hash;

        } catch (BiometricException e) {
            throw e;
        } catch (Exception e) {
            throw new BiometricException("Erreur lors de la capture de l'empreinte: " + e.getMessage());
        } finally {
            if (sessionHandle.getValue() != 0) {
                winbio.WinBioCloseSession(sessionHandle.getValue());
            }
        }
    }

    /**
     * Vérifie une empreinte digitale contre un template stocké
     */
    public boolean verifyFingerprint(byte[] storedTemplate) throws BiometricException {
        if (!available) {
            throw new BiometricException("Lecteur d'empreintes non disponible");
        }

        if (storedTemplate == null || storedTemplate.length == 0) {
            throw new BiometricException("Aucun template d'empreinte enregistré");
        }

        IntByReference sessionHandle = new IntByReference();
        try {
            // Ouvrir une session biométrique
            int result = winbio.WinBioOpenSession(
                WINBIO_TYPE_FINGERPRINT,
                WINBIO_POOL_SYSTEM,
                WINBIO_FLAG_DEFAULT,
                null, 0, null,
                sessionHandle
            );

            if (result != S_OK) {
                throw new BiometricException("Impossible d'ouvrir une session biométrique");
            }

            // Vérifier l'empreinte actuelle
            IntByReference identity = new IntByReference();
            IntByReference subFactor = new IntByReference();
            IntByReference match = new IntByReference();
            IntByReference rejectDetail = new IntByReference();

            result = winbio.WinBioVerify(
                sessionHandle.getValue(),
                identity,
                subFactor,
                match,
                rejectDetail
            );

            if (result == S_OK && match.getValue() == 1) {
                // Créer un hash de l'empreinte actuelle pour comparaison
                String currentData = identity.getValue() + "-" + subFactor.getValue();
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] currentHash = digest.digest(currentData.getBytes("UTF-8"));

                System.out.println("[BiometricAuthService] Vérification - Identity: " + identity.getValue() + ", SubFactor: " + subFactor.getValue());
                System.out.println("[BiometricAuthService] Hash stocké: " + bytesToHex(storedTemplate));
                System.out.println("[BiometricAuthService] Hash actuel: " + bytesToHex(currentHash));

                // Comparer les deux hashs
                boolean hashMatch = Arrays.equals(storedTemplate, currentHash);
                System.out.println("[BiometricAuthService] Correspondance des hashs: " + hashMatch);

                return hashMatch;
            }

            System.out.println("[BiometricAuthService] Windows Hello n'a pas validé l'empreinte (result: " + result + ", match: " + match.getValue() + ")");
            return false;

        } catch (BiometricException e) {
            throw e;
        } catch (Exception e) {
            throw new BiometricException("Erreur lors de la vérification: " + e.getMessage());
        } finally {
            if (sessionHandle.getValue() != 0) {
                winbio.WinBioCloseSession(sessionHandle.getValue());
            }
        }
    }

    /**
     * Exception pour les erreurs biométriques
     */
    public static class BiometricException extends Exception {
        public BiometricException(String message) {
            super(message);
        }

        public BiometricException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Convertit un tableau de bytes en string hexadécimal pour le debug
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
