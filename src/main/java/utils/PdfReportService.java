package utils;

import com.lowagie.text.Document;
import com.lowagie.text.*;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.stage.FileChooser;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service de génération de rapports PDF ultra-professionnels pour FinTrack.
 * Inclut support TND, design moderne et conseils intelligents.
 */
public class PdfReportService {

        private static final Color PRIMARY_BLUE = new Color(30, 58, 138); // Dark blue
        private static final Color ACCENT_BLUE = new Color(59, 130, 246); // Bright blue
        private static final Color LIGHT_GRAY = new Color(248, 250, 252);
        private static final Color BORDER_GRAY = new Color(226, 232, 240);
        private static final Color SUCCESS_GREEN = new Color(22, 101, 52);
        private static final Color ADVICE_BG = new Color(239, 246, 255);
        private static final Color TEXT_DARK = new Color(15, 23, 42);

        private static final DecimalFormat TND_FORMAT = new DecimalFormat("#,##0.000 TND");

        public static void generateDashboardReport(
                        String totalDocs, String totalDossiers, String totalCats, String monthDocs,
                        String totalAmount, String avgAmount,
                        Map<String, Integer> catStats,
                        List<String> recentDocs) {

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Exporter le Rapport Financier FinTrack");
                fileChooser.setInitialFileName("Rapport_Activité_"
                                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Professionnel", "*.pdf"));

                File file = fileChooser.showSaveDialog(null);
                if (file == null)
                        return;

                Document document = new Document(PageSize.A4, 30, 30, 40, 40);

                try {
                        PdfWriter.getInstance(document, new FileOutputStream(file));
                        document.open();

                        // --- HEADER DESIGN ---
                        PdfPTable header = new PdfPTable(2);
                        header.setWidthPercentage(100);
                        header.setWidths(new float[] { 3, 1 });

                        // Title and Subtitle
                        PdfPCell titleBox = new PdfPCell();
                        titleBox.setBorder(Rectangle.NO_BORDER);
                        Paragraph mainTitle = new Paragraph("FINTRACK EXECUTIVE REPORT",
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, PRIMARY_BLUE));
                        Paragraph subTitle = new Paragraph("Analyse Financière & Gestion de Documents Intellectuelle",
                                        FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY));
                        titleBox.addElement(mainTitle);
                        titleBox.addElement(subTitle);
                        header.addCell(titleBox);

                        // Date and System ID
                        PdfPCell infoBox = new PdfPCell();
                        infoBox.setBorder(Rectangle.NO_BORDER);
                        infoBox.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        Paragraph dateP = new Paragraph(
                                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_DARK));
                        dateP.setAlignment(Element.ALIGN_RIGHT);
                        infoBox.addElement(dateP);
                        header.addCell(infoBox);

                        document.add(header);
                        document.add(new Paragraph(" "));

                        // Separation Line
                        PdfPTable lineTable = new PdfPTable(1);
                        lineTable.setWidthPercentage(100);
                        PdfPCell lineCell = new PdfPCell();
                        lineCell.setFixedHeight(2f);
                        lineCell.setBackgroundColor(ACCENT_BLUE);
                        lineCell.setBorder(Rectangle.NO_BORDER);
                        lineTable.addCell(lineCell);
                        document.add(lineTable);
                        document.add(new Paragraph(" "));

                        // --- KPI CARDS SECTION ---
                        document.add(new Paragraph("SYNTHÈSE DES INDICATEURS CLÉS (KPIs)",
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_BLUE)));
                        document.add(new Paragraph(" "));

                        PdfPTable kpiTable = new PdfPTable(4);
                        kpiTable.setWidthPercentage(100);
                        kpiTable.setSpacingBefore(10f);
                        kpiTable.setSpacingAfter(20f);

                        addKPICell(kpiTable, "DOCUMENTS", totalDocs, "Total Archivé");
                        addKPICell(kpiTable, "DOSSIERS", totalDossiers, "Organisation");
                        addKPICell(kpiTable, "CATÉGORIES", totalCats, "Segmentation");
                        addKPICell(kpiTable, "MOIS EN COURS", monthDocs, "Nouvelles Entrées");

                        document.add(kpiTable);

                        // --- FINANCIAL ANALYSIS SECTION ---
                        document.add(new Paragraph("ANALYSE DES FLUX FINANCIERS",
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_BLUE)));
                        document.add(new Paragraph(" "));

                        PdfPTable financeTable = new PdfPTable(2);
                        financeTable.setWidthPercentage(100);
                        financeTable.setSpacingBefore(10f);

                        addFinancialCard(financeTable, "MONTANT TOTAL CUMULÉ", totalAmount, true);
                        addFinancialCard(financeTable, "MOYENNE PAR DOCUMENT", avgAmount, false);

                        document.add(financeTable);
                        document.add(new Paragraph(" "));

                        // --- CATEGORY DISTRIBUTION TABLE ---
                        document.add(new Paragraph("RÉPARTITION ANALYTIQUE PAR CATÉGORIE",
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_BLUE)));
                        document.add(new Paragraph(" "));

                        PdfPTable catTable = new PdfPTable(2);
                        catTable.setWidthPercentage(100);
                        catTable.setSpacingBefore(5f);

                        PdfPCell h1 = new PdfPCell(new Phrase("DÉSIGNATION CATÉGORIE",
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                        h1.setBackgroundColor(PRIMARY_BLUE);
                        h1.setPadding(10);
                        h1.setBorderColor(Color.WHITE);

                        PdfPCell h2 = new PdfPCell(new Phrase("VOLUME DOCS",
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                        h2.setBackgroundColor(PRIMARY_BLUE);
                        h2.setPadding(10);
                        h2.setBorderColor(Color.WHITE);
                        h2.setHorizontalAlignment(Element.ALIGN_CENTER);

                        catTable.addCell(h1);
                        catTable.addCell(h2);

                        catStats.forEach((name, count) -> {
                                PdfPCell c1 = new PdfPCell(new Phrase(name,
                                                FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_DARK)));
                                c1.setPadding(8);
                                c1.setBorderColor(BORDER_GRAY);
                                c1.setBackgroundColor(LIGHT_GRAY);

                                PdfPCell c2 = new PdfPCell(new Phrase(String.valueOf(count),
                                                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_BLUE)));
                                c2.setPadding(8);
                                c2.setBorderColor(BORDER_GRAY);
                                c2.setBackgroundColor(LIGHT_GRAY);
                                c2.setHorizontalAlignment(Element.ALIGN_CENTER);

                                catTable.addCell(c1);
                                catTable.addCell(c2);
                        });
                        document.add(catTable);
                        document.add(new Paragraph(" "));

                        // --- SMART ADVICE SECTION (PRO AI) ---
                        document.add(new Paragraph("RECOMMANDATIONS STRATÉGIQUES",
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, SUCCESS_GREEN)));
                        document.add(new Paragraph(" "));

                        java.util.List<String> tips = generateSmartTips(totalDocs, monthDocs, totalAmount, catStats);
                        PdfPTable tipsTable = new PdfPTable(1);
                        tipsTable.setWidthPercentage(100);

                        for (String tip : tips) {
                                PdfPCell tipCell = new PdfPCell();
                                Paragraph tipPara = new Paragraph();
                                tipPara.add(new Chunk("💡 CONSEIL PRO : ",
                                                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, SUCCESS_GREEN)));
                                tipPara.add(new Chunk(tip, FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_DARK)));
                                tipCell.addElement(tipPara);
                                tipCell.setPadding(15);
                                tipCell.setBackgroundColor(ADVICE_BG);
                                tipCell.setBorderColor(SUCCESS_GREEN);
                                tipCell.setBorderWidthLeft(5f);
                                tipCell.setBorder(Rectangle.LEFT);
                                tipsTable.addCell(tipCell);

                                // Add tiny spacer cell manually by adding empty cell with padding
                                PdfPCell spacer = new PdfPCell(new Phrase(" "));
                                spacer.setBorder(Rectangle.NO_BORDER);
                                spacer.setFixedHeight(5f);
                                tipsTable.addCell(spacer);
                        }
                        document.add(tipsTable);

                        // --- FOOTER ---
                        document.add(new Paragraph(" "));
                        Paragraph footer = new Paragraph(
                                        "Rapport généré automatiquement par FinTrack Intelligence Engine. Confidentialité de niveau entreprise.",
                                        FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY));
                        footer.setAlignment(Element.ALIGN_CENTER);
                        document.add(footer);

                        document.close();

                        if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().open(file);
                        }

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        private static void addKPICell(PdfPTable table, String label, String value, String subLabel) {
                PdfPCell cell = new PdfPCell();
                cell.setPadding(12);
                cell.setBorderColor(BORDER_GRAY);
                cell.setBackgroundColor(LIGHT_GRAY);

                Paragraph pLabel = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.GRAY));
                Paragraph pValue = new Paragraph(value,
                                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PRIMARY_BLUE));
                Paragraph pSub = new Paragraph(subLabel,
                                FontFactory.getFont(FontFactory.HELVETICA, 7, Color.LIGHT_GRAY));

                cell.addElement(pLabel);
                cell.addElement(pValue);
                cell.addElement(pSub);
                table.addCell(cell);
        }

        private static void addFinancialCard(PdfPTable table, String title, String value, boolean highlighted) {
                PdfPCell cell = new PdfPCell();
                cell.setPadding(20);
                cell.setBorderColor(highlighted ? ACCENT_BLUE : BORDER_GRAY);
                cell.setBorderWidth(highlighted ? 1.5f : 1f);
                cell.setBackgroundColor(highlighted ? new Color(240, 249, 255) : Color.WHITE);

                Paragraph pTitle = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10,
                                highlighted ? PRIMARY_BLUE : Color.GRAY));
                Paragraph pValue = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22,
                                highlighted ? PRIMARY_BLUE : TEXT_DARK));

                cell.addElement(pTitle);
                cell.addElement(pValue);
                table.addCell(cell);
        }

        private static List<String> generateSmartTips(String totalDocs, String monthDocs, String totalAmount,
                        Map<String, Integer> catStats) {
                List<String> tips = new ArrayList<>();
                int count = Integer.parseInt(totalDocs);
                int thisMonth = Integer.parseInt(monthDocs);

                if (count == 0) {
                        tips.add("Bienvenue sur votre nouvel espace FinTrack ! Pour commencer l'analyse, scannez vos 5 premiers documents financiers.");
                }

                if (thisMonth > 5) {
                        tips.add("Forte activité détectée. Vos archives s'agrandissent : nous vous recommandons de vérifier si certains documents nécessitent une classification en 'Urgent'.");
                }

                if (totalAmount.contains("0.00") || totalAmount.contains("0,00")) {
                        tips.add("Attention : Le montant cumulé est de 0. Pensez à utiliser l'outil d'extraction automatique pour lire les montants sur vos PDF.");
                }

                tips.add("Optimisation : Regroupez vos documents par dossiers trimestriels pour une meilleure lisibilité lors de l'export des bilans.");
                tips.add("Sécurité : Vos données sont locales et privées. Pensez à effectuer une sauvegarde régulière de votre base de données MySQL.");

                return tips;
        }
}
