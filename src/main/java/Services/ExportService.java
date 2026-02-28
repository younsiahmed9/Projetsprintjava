package Services;

import Models.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.borders.SolidBorder;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Exporte les transferts vers un fichier PDF format A4 avec mise en page professionnelle
     */
    public static boolean exportToPDF(List<TransferData> transferts, Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("transferts_" +
                LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".pdf");

        File file = fileChooser.showSaveDialog(ownerWindow);
        if (file == null) return false;

        try {
            // Initialisation du document PDF au format A4
            PdfWriter writer = new PdfWriter(file.getAbsolutePath());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(50, 50, 50, 50); // Marges: haut, droit, bas, gauche

            // ===== EN-TÊTE =====
            Paragraph title = new Paragraph("FinTrack - Historique des transferts")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(title);

            // Sous-titre / date de génération
            Paragraph dateGeneration = new Paragraph("Généré le " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm:ss")))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(dateGeneration);

            // Ligne de séparation
            Paragraph separator = new Paragraph("__________________________________________________")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(separator);

            // ===== STATISTIQUES =====
            if (!transferts.isEmpty()) {
                Table statsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                        .setWidth(UnitValue.createPercentValue(100))
                        .setMarginBottom(20);

                // Calcul des statistiques
                double montantTotal = transferts.stream().mapToDouble(TransferData::getMontant).sum();
                long reussis = transferts.stream().filter(t -> "SUCCESS".equals(t.getStatut())).count();
                long echoues = transferts.stream().filter(t -> "FAILED".equals(t.getStatut())).count();
                String premierType = transferts.stream()
                        .map(TransferData::getType)
                        .findFirst().orElse("-");

                // Ajout des cartes de statistiques
                addStatCell(statsTable, "Total", String.format("%.2f", montantTotal) + " DT", ColorConstants.BLUE);
                addStatCell(statsTable, "Réussis", String.valueOf(reussis), ColorConstants.GREEN);
                addStatCell(statsTable, "Échoués", String.valueOf(echoues), ColorConstants.RED);
                addStatCell(statsTable, "Type principal", premierType, ColorConstants.ORANGE);

                document.add(statsTable);
            }

            // ===== RÉSUMÉ =====
            Paragraph resumeTitle = new Paragraph("Résumé des transferts")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10);
            document.add(resumeTitle);

            // ===== TABLEAU PRINCIPAL =====
            // Définition des colonnes
            float[] columnWidths = {4, 8, 10, 6, 8, 12, 12, 6, 8, 15, 15};
            Table table = new Table(UnitValue.createPercentArray(columnWidths))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);

            // En-têtes du tableau
            String[] headers = {"ID", "Type", "Montant", "Devise", "Date",
                    "Carte source", "Carte dest", "Statut", "Description",
                    "Email", "Propriétaire"};

            // Style des en-têtes
            for (String header : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(header))
                        .setBold()
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
                table.addCell(headerCell);
            }

            // Données
            for (TransferData t : transferts) {
                table.addCell(createCell(String.valueOf(t.getId())));
                table.addCell(createCell(t.getType()));
                table.addCell(createCell(String.format("%.2f", t.getMontant())));
                table.addCell(createCell(t.getDevise()));
                table.addCell(createCell(t.getDate()));
                table.addCell(createCell(t.getCarteSource()));
                table.addCell(createCell(t.getCarteDest()));

                // Cellule avec couleur selon statut
                Cell statutCell = new Cell().add(new Paragraph(t.getStatut()));
                if ("SUCCESS".equals(t.getStatut())) {
                    statutCell.setBackgroundColor(ColorConstants.GREEN);
                    statutCell.setFontColor(ColorConstants.WHITE);
                } else if ("FAILED".equals(t.getStatut())) {
                    statutCell.setBackgroundColor(ColorConstants.RED);
                    statutCell.setFontColor(ColorConstants.WHITE);
                }
                statutCell.setTextAlignment(TextAlignment.CENTER);
                table.addCell(statutCell);

                table.addCell(createCell(t.getDescription()));
                table.addCell(createCell(t.getEmailProprietaire()));
                table.addCell(createCell(t.getNomProprietaire()));
            }

            document.add(table);

            // ===== PIED DE PAGE =====
            Paragraph footer = new Paragraph("FinTrack - Application de gestion financière - Document généré automatiquement")
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

            // Fermeture du document
            document.close();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void addStatCell(Table table, String label, String value, com.itextpdf.kernel.colors.Color color) {
        Cell cell = new Cell()
                .add(new Paragraph(label + "\n" + value))
                .setBackgroundColor(color)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
        table.addCell(cell);
    }

    private static com.itextpdf.layout.element.Cell createCell(String content) {
        return new Cell()
                .add(new Paragraph(content != null ? content : "-"))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(9)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
    }

    /**
     * Exporte les transferts vers un fichier Excel avec mise en page professionnelle
     */
    public static boolean exportToExcel(List<TransferData> transferts, Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx")
        );
        fileChooser.setInitialFileName("transferts_" +
                LocalDateTime.now().format(FILE_DATE_FORMATTER) + ".xlsx");

        File file = fileChooser.showSaveDialog(ownerWindow);
        if (file == null) return false;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transferts");

            // ===== STYLES =====
            // Style pour l'en-tête principal
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Style pour les en-têtes de colonnes
            CellStyle columnHeaderStyle = workbook.createCellStyle();
            Font columnHeaderFont = workbook.createFont();
            columnHeaderFont.setBold(true);
            columnHeaderStyle.setFont(columnHeaderFont);
            columnHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            columnHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            columnHeaderStyle.setBorderTop(BorderStyle.THIN);
            columnHeaderStyle.setBorderBottom(BorderStyle.THIN);
            columnHeaderStyle.setBorderLeft(BorderStyle.THIN);
            columnHeaderStyle.setBorderRight(BorderStyle.THIN);
            columnHeaderStyle.setAlignment(HorizontalAlignment.CENTER);

            // Style pour les cellules de données
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);

            // Style pour les cellules de succès (vert)
            CellStyle successStyle = workbook.createCellStyle();
            successStyle.cloneStyleFrom(dataStyle);
            successStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            successStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Style pour les cellules d'échec (rouge)
            CellStyle failedStyle = workbook.createCellStyle();
            failedStyle.cloneStyleFrom(dataStyle);
            failedStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            failedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ===== EN-TÊTE PRINCIPAL =====
            Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("FinTrack - Historique des transferts");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10)); // Fusionner les 11 premières colonnes

            // ===== LIGNE DE DATE =====
            Row dateRow = sheet.createRow(1);
            org.apache.poi.ss.usermodel.Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            dateCell.setCellStyle(columnHeaderStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 10));

            // ===== LIGNE BLANCHE =====
            sheet.createRow(2);

            // ===== STATISTIQUES =====
            if (!transferts.isEmpty()) {
                Row statsTitleRow = sheet.createRow(3);
                org.apache.poi.ss.usermodel.Cell statsTitleCell = statsTitleRow.createCell(0);
                statsTitleCell.setCellValue("STATISTIQUES");
                statsTitleCell.setCellStyle(columnHeaderStyle);

                // Calcul des statistiques
                double montantTotal = transferts.stream().mapToDouble(TransferData::getMontant).sum();
                long reussis = transferts.stream().filter(t -> "SUCCESS".equals(t.getStatut())).count();
                long echoues = transferts.stream().filter(t -> "FAILED".equals(t.getStatut())).count();

                // Création des statistiques
                Row statsRow = sheet.createRow(4);
                statsRow.createCell(0).setCellValue("Total:");
                statsRow.createCell(1).setCellValue(montantTotal + " DT");
                statsRow.createCell(3).setCellValue("Réussis:");
                statsRow.createCell(4).setCellValue(reussis);
                statsRow.createCell(6).setCellValue("Échoués:");
                statsRow.createCell(7).setCellValue(echoues);

                // Style pour les cellules de statistiques
                for (int i = 0; i < 10; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = statsRow.getCell(i);
                    if (cell != null) {
                        cell.setCellStyle(dataStyle);
                    }
                }

                // Ligne vide après les stats
                sheet.createRow(5);
            }

            // ===== EN-TÊTES DES COLONNES =====
            int startRow = (transferts.isEmpty()) ? 3 : 7;
            Row headerRow = sheet.createRow(startRow);
            String[] headers = {"ID", "Type", "Montant", "Devise", "Date",
                    "Carte source", "Carte dest", "Statut", "Description",
                    "Email", "Propriétaire"};

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(columnHeaderStyle);
            }

            // ===== DONNÉES =====
            int rowNum = startRow + 1;
            for (TransferData t : transferts) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(t.getId());
                row.createCell(1).setCellValue(t.getType());
                row.createCell(2).setCellValue(t.getMontant());
                row.createCell(3).setCellValue(t.getDevise());
                row.createCell(4).setCellValue(t.getDate());
                row.createCell(5).setCellValue(t.getCarteSource());
                row.createCell(6).setCellValue(t.getCarteDest());
                row.createCell(7).setCellValue(t.getStatut());
                row.createCell(8).setCellValue(t.getDescription());
                row.createCell(9).setCellValue(t.getEmailProprietaire());
                row.createCell(10).setCellValue(t.getNomProprietaire());

                // Application du style selon le statut
                for (int i = 0; i <= 10; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.getCell(i);
                    if (cell != null) {
                        if ("SUCCESS".equals(t.getStatut())) {
                            cell.setCellStyle(successStyle);
                        } else if ("FAILED".equals(t.getStatut())) {
                            cell.setCellStyle(failedStyle);
                        } else {
                            cell.setCellStyle(dataStyle);
                        }
                    }
                }
            }

            // ===== FORMATAGE =====
            // Ajuster automatiquement la largeur des colonnes
            for (int i = 0; i <= 10; i++) {
                sheet.autoSizeColumn(i);
                // Ajouter un peu d'espace
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3500);
                }
            }

            // Figer la première ligne d'en-tête
            sheet.createFreezePane(0, startRow + 1);

            // ===== FILTRES =====
            sheet.setAutoFilter(new CellRangeAddress(startRow, rowNum - 1, 0, 10));

            // ===== PIED DE PAGE =====
            Row footerRow = sheet.createRow(rowNum + 2);
            org.apache.poi.ss.usermodel.Cell footerCell = footerRow.createCell(0);
            footerCell.setCellValue("FinTrack - Application de gestion financière - Document généré automatiquement");
            footerCell.setCellStyle(dataStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum + 2, rowNum + 2, 0, 10));

            // Écrire dans le fichier
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Classe de données pour les transferts
     */
    public static class TransferData {
        private int id;
        private String type;
        private double montant;
        private String devise;
        private String date;
        private String carteSource;
        private String carteDest;
        private String statut;
        private String description;
        private String emailProprietaire;
        private String nomProprietaire;

        public TransferData(Transaction t, String carteSourceNum, String carteDestNum,
                            String email, String nom) {
            this.id = t.getId();
            this.type = t.getType().toString();
            this.montant = t.getMontant();
            this.devise = t.getDevise();
            this.date = t.getDate().format(DATE_FORMATTER);
            this.carteSource = carteSourceNum;
            this.carteDest = carteDestNum;
            this.statut = t.getStatut().toString();
            this.description = t.getDescription();
            this.emailProprietaire = email;
            this.nomProprietaire = nom;
        }

        // Getters
        public int getId() { return id; }
        public String getType() { return type; }
        public double getMontant() { return montant; }
        public String getDevise() { return devise; }
        public String getDate() { return date; }
        public String getCarteSource() { return carteSource; }
        public String getCarteDest() { return carteDest; }
        public String getStatut() { return statut; }
        public String getDescription() { return description; }
        public String getEmailProprietaire() { return emailProprietaire; }
        public String getNomProprietaire() { return nomProprietaire; }
    }
}