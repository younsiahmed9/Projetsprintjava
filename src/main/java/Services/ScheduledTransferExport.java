package Services;

import Models.ScheduledTransfer;
import Models.Utilisateur;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScheduledTransferExport {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static class ScheduledTransferData {
        private int id;
        private String type; // "Programmé" ou "Récurrent"
        private double montant;
        private String devise;
        private String dateExecution;
        private String carteSource;
        private String carteDest;
        private String statut;
        private String frequence;
        private String emailProprietaire;
        private String nomProprietaire;

        public ScheduledTransferData(ScheduledTransfer st, String carteSourceNum, String carteDestNum,
                                     String email, String nom, String frequenceStr) {
            this.id = st.getId();
            this.type = "Programmé";
            this.montant = st.getAmount();
            this.devise = "DT"; // À adapter selon votre modèle
            this.dateExecution = st.getScheduledDate().format(DATE_FORMATTER);
            this.carteSource = carteSourceNum;
            this.carteDest = carteDestNum;
            this.statut = st.getStatus();
            this.frequence = frequenceStr;
            this.emailProprietaire = email;
            this.nomProprietaire = nom;
        }

        // Getters
        public int getId() { return id; }
        public String getType() { return type; }
        public double getMontant() { return montant; }
        public String getDevise() { return devise; }
        public String getDateExecution() { return dateExecution; }
        public String getCarteSource() { return carteSource; }
        public String getCarteDest() { return carteDest; }
        public String getStatut() { return statut; }
        public String getFrequence() { return frequence; }
        public String getEmailProprietaire() { return emailProprietaire; }
        public String getNomProprietaire() { return nomProprietaire; }
    }

    public static boolean exportToPDF(List<ScheduledTransferData> transferts, Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("transferts_programmes_" +
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        File file = fileChooser.showSaveDialog(ownerWindow);
        if (file == null) return false;

        try {
            PdfWriter writer = new PdfWriter(file.getAbsolutePath());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Paragraph title = new Paragraph("Transferts programmés")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            Paragraph info = new Paragraph("Généré le: " +
                    java.time.LocalDateTime.now().format(DATE_FORMATTER))
                    .setFontSize(10)
                    .setMarginBottom(20);
            document.add(info);

            Table table = new Table(UnitValue.createPercentArray(new float[]{5, 8, 10, 8, 12, 12, 12, 10, 15}))
                    .useAllAvailableWidth();

            String[] headers = {"ID", "Type", "Montant", "Devise", "Date exécution",
                    "Carte source", "Carte dest", "Statut", "Fréquence"};
            for (String header : headers) {
                Cell cell = new Cell().add(new Paragraph(header))
                        .setBold()
                        .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addCell(cell);
            }

            for (ScheduledTransferData t : transferts) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(t.getId()))));
                table.addCell(new Cell().add(new Paragraph(t.getType())));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", t.getMontant()))));
                table.addCell(new Cell().add(new Paragraph(t.getDevise())));
                table.addCell(new Cell().add(new Paragraph(t.getDateExecution())));
                table.addCell(new Cell().add(new Paragraph(t.getCarteSource())));
                table.addCell(new Cell().add(new Paragraph(t.getCarteDest())));
                table.addCell(new Cell().add(new Paragraph(t.getStatut())));
                table.addCell(new Cell().add(new Paragraph(t.getFrequence())));
            }

            document.add(table);
            document.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}