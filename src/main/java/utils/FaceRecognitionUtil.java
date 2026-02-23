package utils;

import org.bytedeco.opencv.opencv_core.Mat;

public class FaceRecognitionUtil {
    /**
     * Capture une image depuis la webcam (à implémenter)
     */
    public static Mat captureFaceFromWebcam() {
        // TODO: Implémenter la capture d'image depuis la webcam
        return null;
    }

    /**
     * Extrait un modèle facial à partir d'une image (à implémenter)
     */
    public static byte[] extractFaceTemplate(Mat faceImage) {
        // TODO: Implémenter l'extraction du modèle facial
        return null;
    }

    /**
     * Compare deux modèles faciaux (à implémenter)
     */
    public static boolean matchFaceTemplates(byte[] template1, byte[] template2) {
        // TODO: Implémenter la comparaison de modèles faciaux
        return false;
    }
}
