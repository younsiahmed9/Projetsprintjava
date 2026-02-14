import java.io.*;
import java.net.*;

public class MavenWrapperDownloader {
    private static final String WRAPPER_VERSION = "3.3.2";
    private static final String DEFAULT_DOWNLOAD_URL =
            "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/" + WRAPPER_VERSION +
                    "/maven-wrapper-" + WRAPPER_VERSION + ".jar";

    public static void main(String[] args) {
        File baseDirectory = new File(System.getProperty("maven.multiModuleProjectDirectory", "."));
        File wrapperJar = new File(baseDirectory, ".mvn/wrapper/maven-wrapper.jar");
        if (wrapperJar.exists()) {
            return;
        }

        String downloadUrl = DEFAULT_DOWNLOAD_URL;
        System.out.println("Downloading Maven wrapper from: " + downloadUrl);
        try {
            wrapperJar.getParentFile().mkdirs();
            downloadFileFromURL(downloadUrl, wrapperJar);
        } catch (Exception e) {
            throw new RuntimeException("Could not download maven-wrapper.jar", e);
        }
    }

    private static void downloadFileFromURL(String urlString, File destination) throws IOException {
        URL url = URI.create(urlString).toURL();
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);

        try (InputStream is = connection.getInputStream();
             FileOutputStream fos = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = is.read(buffer)) >= 0) {
                fos.write(buffer, 0, n);
            }
        }
    }
}
