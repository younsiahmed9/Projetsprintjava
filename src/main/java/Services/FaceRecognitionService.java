package Services;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.global.opencv_core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class FaceRecognitionService {
    private static final String CASCADE_RESOURCE = "/models/haarcascade_frontalface_alt.xml";
    private static CascadeClassifier faceCascade;
    private static boolean opencvLoaded = false;

    static {
        opencvLoaded = true;
        // Load cascade from resource to temp file
        try {
            Path tmp = Files.createTempFile("haarcascade", ".xml");
            var is = FaceRecognitionService.class.getResourceAsStream(CASCADE_RESOURCE);
            if (is == null) {
                System.err.println("Cascade resource not found: " + CASCADE_RESOURCE + " - please put the cascade in resources/models/");
            } else {
                Files.copy(is, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                faceCascade = new CascadeClassifier(tmp.toAbsolutePath().toString());
            }
        } catch (IOException ex) {
            System.err.println("Failed to load cascade: " + ex.getMessage());
        }
    }

    public static boolean isOpenCvAvailable() {
        return opencvLoaded && faceCascade != null && !faceCascade.empty();
    }

    // capture a number of face Mats from the default camera (cameraIndex 0)
    public List<Mat> captureFacesFromCamera(int count, int timeoutMs, boolean preview) throws Exception {
        if (!isOpenCvAvailable()) throw new IllegalStateException("OpenCV not available or cascade not loaded");
        List<Mat> faces = new ArrayList<>();
        VideoCapture cap = new VideoCapture(0);
        if (!cap.isOpened()) {
            cap.release();
            throw new IOException("Camera not available");
        }
        final Stage[] stageHolder = new Stage[1];
        final ImageView[] viewHolder = new ImageView[1];
        final boolean[] stop = {false};
        final Button[] closeBtnHolder = new Button[1];
        if (preview) {
            Platform.runLater(() -> {
                Stage stage = new Stage();
                ImageView view = new ImageView();
                view.setFitWidth(480);
                view.setFitHeight(360);
                Button closeBtn = new Button("Close");
                closeBtn.setOnAction(e -> stage.close());
                VBox root = new VBox(view, closeBtn);
                Scene scene = new Scene(root);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(scene);
                stage.setTitle("Camera preview - close to stop");
                stage.show();
                stageHolder[0] = stage;
                viewHolder[0] = view;
                closeBtnHolder[0] = closeBtn;
            });
            // wait a bit for stage to open
            Thread.sleep(300);
        }
        long start = System.currentTimeMillis();
        Mat frame = new Mat();
        while (faces.size() < count && (System.currentTimeMillis() - start) < timeoutMs && !stop[0]) {
            if (!cap.read(frame)) continue;
            if (frame.empty()) continue;
            Mat gray = new Mat();
            opencv_imgproc.cvtColor(frame, gray, opencv_imgproc.COLOR_BGR2GRAY);
            RectVector rects = new RectVector();
            faceCascade.detectMultiScale(gray, rects);
            long n = rects.size();
            if (n > 0) {
                // choose the biggest rectangle (likely the user's face)
                Rect best = rects.get(0);
                for (int i = 1; i < n; i++) {
                    Rect r = rects.get(i);
                    if (r.area() > best.area()) best = r;
                }
                Mat face = new Mat(gray, best);
                Mat resized = new Mat();
                opencv_imgproc.resize(face, resized, new Size(200,200));
                faces.add(resized);
            }
            if (preview && viewHolder[0] != null) {
                BufferedImage img = matToBufferedImage(frame);
                WritableImage fxImg = SwingFXUtils.toFXImage(img, null);
                Platform.runLater(() -> viewHolder[0].setImage(fxImg));
            }
        }
        // Attendre au moins 6 secondes avant de fermer la fenêtre
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed < 6000) {
            Thread.sleep(6000 - elapsed);
        }
        // close preview
        if (preview) {
            Platform.runLater(() -> {
                // find any open windows and close the preview stage
                // Best-effort: close all modal stages created earlier
                for (Stage s : Stage.getWindows().filtered(w -> w instanceof Stage).toArray(new Stage[0])) {
                    if (s.getTitle() != null && s.getTitle().contains("Camera preview")) s.close();
                }
            });
        }
        cap.release();
        return faces;
    }

    public void trainAndSave(List<Mat> images, Path modelPath) throws Exception {
        if (images == null || images.isEmpty()) throw new IllegalArgumentException("No images to train");
        MatVector mats = new MatVector(images.size());
        Mat labels = new Mat(images.size(), 1, opencv_core.CV_32SC1);
        int idx = 0;
        for (Mat m : images) {
            mats.put(idx, m);
            labels.ptr(idx).putInt(1);
            idx++;
        }
        // LBPH recognizer
        var recognizer = LBPHFaceRecognizer.create();
        recognizer.train(mats, labels);
        // ensure parent dir
        Files.createDirectories(modelPath.getParent());
        recognizer.save(modelPath.toString());
    }

    public double verifyWithModel(Path modelPath, Mat image) throws Exception {
        if (!Files.exists(modelPath)) throw new IOException("Model not found: " + modelPath);
        var recognizer = LBPHFaceRecognizer.create();
        recognizer.read(modelPath.toString());
        int[] label = new int[1];
        double[] confidence = new double[1];
        recognizer.predict(image, label, confidence);
        return confidence[0];
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels()*mat.cols()*mat.rows();
        byte[] b = new byte[bufferSize];
        mat.data().get(b);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    public byte[] matToPngBytes(Mat mat) throws IOException {
        BufferedImage img = matToBufferedImage(mat);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        }
    }
}
