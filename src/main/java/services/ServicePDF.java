package services;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import models.Facture;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ServicePDF {

    private static final String PDF_DIRECTORY = "factures/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ServicePDF() {
        File directory = new File(PDF_DIRECTORY);
        if (!directory.exists()) directory.mkdirs();
    }

    public String genererFacturePDF(Facture facture) throws IOException, DocumentException {
        String fileName = PDF_DIRECTORY + "facture_" + facture.getNumeroFacture() + ".pdf";
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.BOLD, Color.BLUE);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        // Titre
        Paragraph title = new Paragraph("FACTURE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Numéro et dates
        document.add(new Paragraph("N° " + facture.getNumeroFacture(), normalFont));
        document.add(new Paragraph("Date : " + facture.getDateFacture().format(DATE_FORMATTER), normalFont));
        document.add(new Paragraph("Échéance : " + facture.getDateEcheance().format(DATE_FORMATTER), normalFont));
        document.add(new Paragraph(" "));

        // Client
        document.add(new Paragraph("Client : " + (facture.getNomClient() != null ? facture.getNomClient() : "N/A"), normalFont));
        document.add(new Paragraph("Email : " + (facture.getEmailClient() != null ? facture.getEmailClient() : "N/A"), normalFont));
        document.add(new Paragraph(" "));

        // Détail du produit/service
        document.add(new Paragraph("Détail de la facture :", boldFont));
        document.add(new Paragraph(" "));

        if (facture.getIdService() != null && facture.getService() != null) {
            document.add(new Paragraph("Service : " + facture.getService().getNomService(), normalFont));
            document.add(new Paragraph("Type : " + facture.getService().getTypeService(), normalFont));
            document.add(new Paragraph("Fréquence : " + facture.getService().getFrequence(), normalFont));
        } else if (facture.getIdProduit() != null && facture.getProduit() != null) {
            document.add(new Paragraph("Produit : " + facture.getProduit().getNomProduit(), normalFont));
            document.add(new Paragraph("Type : " + facture.getProduit().getTypeProduit(), normalFont));
            document.add(new Paragraph("Code unique : " + facture.getProduit().getCodeUnique(), normalFont));
        } else {
            document.add(new Paragraph("Aucun détail spécifique.", normalFont));
        }
        document.add(new Paragraph(" "));

        // Montant et statut
        document.add(new Paragraph("Montant TTC : " + String.format("%.2f DT", facture.getMontant()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        document.add(new Paragraph("Statut : " + facture.getStatut(), normalFont));

        document.close();
        System.out.println("✅ PDF généré : " + fileName);
        return fileName;
    }
}