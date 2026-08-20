package com.gestionpaiements.app.service;

import com.gestionpaiements.app.dao.PaiementRepository;
import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.model.TypePaiement;
import com.gestionpaiements.app.model.Professeur;
import com.gestionpaiements.app.util.MontantEnLettresConverter;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.DashedBorder;
import com.itextpdf.layout.borders.DottedBorder;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.image.ImageDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Service for generating PDF État des sommes dues for payments.
 */
@Service
public class PdfGenerationService {

    // Color constants as per specification
    private static final Color PRIMARY_BLUE = new DeviceRgb(0, 64, 128); // #004080
    private static final Color LIGHT_GRAY_BG = new DeviceRgb(244, 245, 247); // #F4F5F7
    private static final Color LIGHT_BORDER = new DeviceRgb(208, 213, 221); // #D0D5DD
    private static final Color ALERT_RED = new DeviceRgb(217, 56, 58); // #D9383A
    private static final Color NET_LABEL_GRAY = new DeviceRgb(90, 90, 90); // #5A5A5A
    private static final Color WHITE = ColorConstants.WHITE;
    private static final Color BLACK = ColorConstants.BLACK;

    private static final String LOGO_PATH = "C:/Users/PC/gest-paiement/gestion-paiements-v2/Logo.png";
    private static final String MINISTERE = "Ministère de l'Éducation Nationale, du Préscolaire et des Sports";
    private static final String DIRECTION = "Direction Provinciale de Nador";
    private static final String TITRE_BASE = "ETAT DES SOMMES DUES POUR FRAIS DE ";

    @Autowired
    private PaiementRepository paiementRepository;

    /**
     * Generates a PDF État des sommesdues for the given payment ID.
     *
     * @param paiementId the ID of the payment
     * @return the PDF as a ByteArrayInputStream
     * @throws Exception if payment not found or error during generation
     */
    public ByteArrayInputStream genererPdfEstadoSums(Long paiementId) throws Exception {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new Exception("Paiement not found with ID: " + paiementId));

        // Create PDF in memory
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(20, 20, 20, 20); // Set margins

        // a. Header (centered)
        String affectation = (paiement.getProfesseur() != null)
                ? paiement.getProfesseur().getAffectation()
                : null;
        addHeader(document, affectation);

        // b. Main Title
        addMainTitle(document, paiement.getTypePaiement());

        // c. Budget Block (Exercice, CGNC, etc.)
        addBudgetBlock(document, paiement);

        // d. Juridical Text (Visas)
        addAdministrativeText(document, paiement.getTypePaiement());

        // e. Beneficiary Information Block
        addBeneficiaryBlock(document, paiement.getProfesseur());

        // f. RIB Block
        addRibBlock(document, paiement.getProfesseur());

        // g. Operations Table
        addOperationsTable(document, paiement);

        // h. Arrêté de Somme (First Recall)
        addArreteDeSomme(document, paiement.getMontantNet());

        // i. Intermediate Signatures (2 columns)
        addIntermediateSignatures(document);

        // j. Final Net à Payer Block (centered)
        addNetAPayerBlock(document, paiement);

        // k. Footer / Validation Final
        addFooterValidation(document, paiement.getMontantNet());

        document.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    // ========== SECTION METHODS ==========

    private void addHeader(Document document, String affectation) {
        // ROYAUME DU MAROC
        Paragraph royaume = new Paragraph("ROYAUME DU MAROC")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(14)
                .setFontColor(BLACK);
        document.add(royaume);

        // Try to add logo
        try {
            File logoFile = new File(LOGO_PATH);
            if (logoFile.exists()) {
                Image logo = new Image(ImageDataFactory.create(LOGO_PATH))
                        .setWidth(60)
                        .setHeight(60)
                        .setTextAlignment(TextAlignment.CENTER);
                document.add(logo);
            } else {
                System.err.println("WARNING: Logo not found at " + LOGO_PATH);
            }
        } catch (Exception e) {
            System.err.println("ERROR loading logo: " + e.getMessage());
        }

        // Ministry and Direction
        Paragraph ministere = new Paragraph(MINISTERE)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setFontColor(PRIMARY_BLUE)
                .setBold();
        document.add(ministere);

        String directionText = (affectation != null && !affectation.isBlank()) ? affectation : DIRECTION;
        Paragraph direction = new Paragraph(directionText)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setFontColor(BLACK);
        document.add(direction);

        // Thin horizontal line (primary blue) across content width
        addHorizontalLine(document, PRIMARY_BLUE, 2f, 8f);
    }

    private void addMainTitle(Document document, TypePaiement type) {
        String titre = getDynamicTitle(type);
        Paragraph titrePara = new Paragraph(titre)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(16)
                .setFontColor(BLACK)
                .setMarginTop(10)
                .setMarginBottom(5);
        document.add(titrePara);
        // Underline using a line below
        addHorizontalLine(document, PRIMARY_BLUE, 1f, 8f);
    }

    private void addBudgetBlock(Document document, Paiement paiement) {
        Table table = new Table(new float[]{1.2f, 0.8f, 1.5f, 0.6f, 1.2f, 0.6f, 0.8f, 0.6f, 0.8f, 0.6f});
        table.setWidth(UnitValue.createPercentValue(100));
        table.setBackgroundColor(LIGHT_GRAY_BG);
        table.setMarginBottom(4);

        // Exercice
        Cell label1 = new Cell().add(new Paragraph("Exercice:")
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
        Cell value1 = new Cell().add(new Paragraph(getFieldValue(paiement.getExercice()))
                .setBold()
                .setFontColor(BLACK)
                .setFontSize(9))
                .setBorder(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2)
                .setPadding(4);
        table.addCell(label1);
        table.addCell(value1);

        // Code CGNC
        Cell label2 = new Cell().add(new Paragraph("Code CGNC:")
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
        Cell value2 = new Cell().add(new Paragraph(getFieldValue(paiement.getCodeCgnc()))
                .setBold()
                .setFontColor(BLACK)
                .setFontSize(9))
                .setBorder(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2)
                .setPadding(4);
        table.addCell(label2);
        table.addCell(value2);

        // Article
        Cell label3 = new Cell().add(new Paragraph("Article:")
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
        Cell value3 = new Cell().add(new Paragraph(getFieldValue(paiement.getArticle()))
                .setBold()
                .setFontColor(BLACK)
                .setFontSize(9))
                .setBorder(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2)
                .setPadding(4);
        table.addCell(label3);
        table.addCell(value3);

        // Par
        Cell label4 = new Cell().add(new Paragraph("Par:")
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
        Cell value4 = new Cell().add(new Paragraph(getFieldValue(paiement.getPar()))
                .setBold()
                .setFontColor(BLACK)
                .setFontSize(9))
                .setBorder(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2)
                .setPadding(4);
        table.addCell(label4);
        table.addCell(value4);

        // Lig
        Cell label5 = new Cell().add(new Paragraph("Lig:")
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
        Cell value5 = new Cell().add(new Paragraph(getFieldValue(paiement.getLig()))
                .setBold()
                .setFontColor(BLACK)
                .setFontSize(9))
                .setBorder(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2)
                .setPadding(4);
        table.addCell(label5);
        table.addCell(value5);

        document.add(table);
    }

    private void addAdministrativeText(Document document, TypePaiement type) {
        String text = getTexteAdministratif(type);
        Paragraph adminText = new Paragraph(text)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setFontSize(7)
                .setFontColor(BLACK)
                .setItalic()
                .setMarginBottom(4);
        document.add(adminText);
    }

    private void addBeneficiaryBlock(Document document, Professeur professeur) {
        if (professeur == null) {
            document.add(new Paragraph("INFORMATIONS DU PROFESSEUR: Non disponible")
                    .setItalic()
                    .setMarginBottom(4));
            return;
        }

        Table table = new Table(new float[]{1f, 2f, 0.8f, 1.5f, 0.8f, 1.5f});
        table.setWidth(UnitValue.createPercentValue(100));
        table.setBackgroundColor(LIGHT_GRAY_BG);
        table.setMarginBottom(4);

        // Line 1: Mr Mme
        Cell label1 = new Cell().add(new Paragraph("Mr Mme:")
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
        Cell value1 = new Cell().add(new Paragraph(getFieldValue(professeur.getNom()) + " " + getFieldValue(professeur.getPrenom()))
                .setBold()
                .setFontColor(BLACK)
                .setFontSize(9))
                .setBorder(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2)
                .setPadding(4);
        table.addCell(label1);
        table.addCell(value1);

        // PPR
        Cell label2 = new Cell().add(new Paragraph("PPR:")
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
        Cell value2 = new Cell().add(new Paragraph(getFieldValue(professeur.getPpr()))
                .setBold()
                .setFontColor(BLACK)
                .setFontSize(9))
                .setBorder(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2)
                .setPadding(4);
        table.addCell(label2);
        table.addCell(value2);

        // CIN
        Cell label3 = new Cell().add(new Paragraph("C.I.N:")
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
        Cell value3 = new Cell().add(new Paragraph(getFieldValue(professeur.getCin()))
                .setBold()
                .setFontColor(BLACK)
                .setFontSize(9))
                .setBorder(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2)
                .setPadding(4);
        table.addCell(label3);
        table.addCell(value3);

        // Line 2: GRADE
        Cell label4 = new Cell().add(new Paragraph("GRADE:")
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
        Cell value4 = new Cell().add(new Paragraph(getFieldValue(professeur.getGrade()))
                .setBold()
                .setFontColor(BLACK)
                .setFontSize(9))
                .setBorder(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2)
                .setPadding(4);
        table.addCell(label4);
        table.addCell(value4);

        // ÉCHELLE
        Cell label5 = new Cell().add(new Paragraph("ÉCHELLE:")
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
        Cell value5 = new Cell().add(new Paragraph(getFieldValue(professeur.getEchelle() != null ? professeur.getEchelle().toString() : ""))
                .setBold()
                .setFontColor(BLACK)
                .setFontSize(9))
                .setBorder(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2)
                .setPadding(4);
        table.addCell(label5);
        table.addCell(value5);

        // DDR
        Cell label6 = new Cell().add(new Paragraph("DDR:")
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
        Cell value6 = new Cell().add(new Paragraph(getFieldValue(professeur.getDdr() != null ? professeur.getDdr().toString() : ""))
                .setBold()
                .setFontColor(BLACK)
                .setFontSize(9))
                .setBorder(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2)
                .setPadding(4);
        table.addCell(label6);
        table.addCell(value6);

        // Line 3: Affectation (full width)
        Cell affectationLabel = new Cell().add(new Paragraph("Affectation:")
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
        Cell affectationValue = new Cell(1, 5).add(new Paragraph(getFieldValue(professeur.getAffectation()))
                .setBold()
                .setFontColor(BLACK)
                .setFontSize(9))
                .setBorder(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2)
                .setPadding(4);
        table.addCell(affectationLabel);
        table.addCell(affectationValue);

        document.add(table);
    }

    private void addRibBlock(Document document, Professeur professeur) {
        if (professeur == null) {
            return;
        }
        String rib = getFormattedRib(professeur);
        if (rib.isEmpty()) {
            return;
        }

        Table table = new Table(1);
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(4);
        table.setBackgroundColor(LIGHT_GRAY_BG);
        // Set border: dashed 1.5px primary blue
        table.setBorder(new DashedBorder(PRIMARY_BLUE, 1.5f));

        Paragraph ribPara = new Paragraph(rib)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(11)
                .setFontColor(PRIMARY_BLUE)
                .setBold()
                .setMarginTop(5)
                .setMarginBottom(5);
        Cell cell = new Cell().add(ribPara)
                .setBorder(Border.NO_BORDER)
                .setPadding(6);
        table.addCell(cell);

        document.add(table);
    }

    private void addOperationsTable(Document document, Paiement paiement) {
        // Column widths: 3,2,2,2,2,2 (nature, date début, date fin, heures, taux, montant brut)
        Table table = new Table(new float[]{2.5f, 1.5f, 1.5f, 1f, 1f, 1.5f});
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(4);

        // Header row
        addTableHeaderCell(table, "NATURE DES OPÉRATIONS", PRIMARY_BLUE, WHITE);
        addTableHeaderCell(table, "DATE DÉBUT", PRIMARY_BLUE, WHITE);
        addTableHeaderCell(table, "DATE FIN", PRIMARY_BLUE, WHITE);
        addTableHeaderCell(table, "HEURES", PRIMARY_BLUE, WHITE);
        addTableHeaderCell(table, "TAUX", PRIMARY_BLUE, WHITE);
        addTableHeaderCell(table, "MONTANT BRUT", PRIMARY_BLUE, WHITE);

        // Data row
        addOperationsDataCell(table, getFieldValue(paiement.getObjetReglement()), null);
        addOperationsDataCell(table, getFieldValue(paiement.getDateDebut() != null ? paiement.getDateDebut().toString() : ""), null);
        addOperationsDataCell(table, getFieldValue(paiement.getDateFin() != null ? paiement.getDateFin().toString() : ""), null);
        addOperationsDataCell(table, getFieldValue(paiement.getNombreHeures() != null ? paiement.getNombreHeures().stripTrailingZeros().toPlainString() : ""), null);
        addOperationsDataCell(table, getFieldValue(paiement.getTaux() != null ? paiement.getTaux().stripTrailingZeros().toPlainString() : ""), null);
        String montantBrutStr = getFieldValue(paiement.getMontantBrut() != null ?
                paiement.getMontantBrut().stripTrailingZeros().toPlainString().replace(".", ",") : "") + " DH";
        addOperationsDataCell(table, montantBrutStr, null);

        // Spacer row
        for (int i = 0; i < 6; i++) {
            table.addCell(new Cell().setBorder(new SolidBorder(LIGHT_BORDER, 0.5f)).setHeight(10));
        }

        // Total row
        String heuresValue = getFieldValue(paiement.getNombreHeures() != null ? paiement.getNombreHeures().stripTrailingZeros().toPlainString() : "");
        addOperationsDataCell(table, "TOTAL", LIGHT_GRAY_BG);
        addOperationsDataCell(table, "", LIGHT_GRAY_BG);
        addOperationsDataCell(table, "", LIGHT_GRAY_BG);
        addOperationsDataCell(table, heuresValue, LIGHT_GRAY_BG);
        addOperationsDataCell(table, "", LIGHT_GRAY_BG);
        addOperationsDataCell(table, montantBrutStr, LIGHT_GRAY_BG);

        document.add(table);
    }

    private void addArreteDeSomme(Document document, BigDecimal montantNet) {
        // Create a paragraph with a left blue bar: we can use a table with two columns: first column width 3pt with background PRIMARY_BLUE, second column the text.
        Table table = new Table(new float[]{0.3f, 10}); // 3pt column, rest for text
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(4);

        // Blue bar cell
        Cell blueBar = new Cell()
                .setBackgroundColor(PRIMARY_BLUE)
                .setWidth(3)
                .setHeight(10);
        table.addCell(blueBar);

        // Text cell
        String amountInWords = getMontantEnLettres(montantNet);
        Paragraph textPara = new Paragraph("Arrêté à la somme de: ")
                .add(new Paragraph(amountInWords)
                        .setBold()
                        .setFontColor(PRIMARY_BLUE)
                        .setUnderline())
                .setFontColor(BLACK);
        Cell textCell = new Cell().add(textPara)
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(5)
                .setPaddingTop(2)
                .setPaddingBottom(2);
        table.addCell(textCell);

        document.add(table);
    }

    private void addIntermediateSignatures(Document document) {
        // Two signature lines: Responsable and Intéressé
        Table signaturesTable = new Table(new float[]{1, 1});
        signaturesTable.setWidth(UnitValue.createPercentValue(100));
        signaturesTable.setMarginBottom(6);

        // Première colonne : Le Responsable
        Cell respLabel = new Cell().add(new Paragraph("Fait à Oujda, le __________"))
                .setBorder(Border.NO_BORDER);
        Cell respName = new Cell().add(new Paragraph("Le Responsable"))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);
        signaturesTable.addCell(respLabel);
        signaturesTable.addCell(respName);

        // Deuxième colonne : L'Intéressé
        Cell intLabel = new Cell().add(new Paragraph("Fait à Oujda, le __________"))
                .setBorder(Border.NO_BORDER);
        Cell intName = new Cell().add(new Paragraph("L'Intéressé"))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);
        signaturesTable.addCell(intLabel);
        signaturesTable.addCell(intName);

        document.add(signaturesTable);
    }

    private void addNetAPayerBlock(Document document, Paiement paiement) {
        // Outer table with blue background and padding to simulate rounded corners
        Table outerTable = new Table(1);
        outerTable.setWidth(UnitValue.createPercentValue(100));
        outerTable.setMarginBottom(8);
        outerTable.setBorder(new SolidBorder(LIGHT_BORDER, 1f));
        outerTable.setPadding(4); // padding to simulate rounded corners and provide space inside

        // Inner table with two columns for label and value
        Table innerTable = new Table(new float[]{3, 2});
        innerTable.setWidth(UnitValue.createPercentValue(100));

        // MONTANT BRUT line
        Cell montantBrutLabel = new Cell().add(new Paragraph("MONTANT BRUT :"))
                .setBold()
                .setFontSize(10)
                .setFontColor(BLACK)
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(3);
        Cell montantBrutValue = new Cell().add(new Paragraph(
                        getFieldValue(paiement.getMontantBrut() != null ?
                                paiement.getMontantBrut().stripTrailingZeros().toPlainString().replace(".", ",") : "0,00") +
                                " DH"))
                .setFontSize(10)
                .setFontColor(BLACK) // black/gray as spec
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(3);
        innerTable.addCell(montantBrutLabel);
        innerTable.addCell(montantBrutValue);

        // Retenue IR line
        String retiLabel = "Retenue IR :";
        if (paiement.getTauxIr() != null && paiement.getTauxIr().compareTo(BigDecimal.ZERO) > 0) {
            retiLabel = "Retenue IR (" + paiement.getTauxIr().stripTrailingZeros().toPlainString() + "%) :";
        } else if (paiement.getMontantBrut() != null && paiement.getMontantBrut().compareTo(BigDecimal.ZERO) > 0 && paiement.getRetenueIr() != null) {
            BigDecimal taux = paiement.getRetenueIr()
                    .divide(paiement.getMontantBrut(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP);
            retiLabel = "Retenue IR (" + taux.toPlainString() + "%) :";
        }
        Cell retenueIRLabel = new Cell().add(new Paragraph(retiLabel))
                .setBold()
                .setFontSize(10)
                .setFontColor(BLACK)
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(3);
        Cell retenueIRValue = new Cell().add(new Paragraph(
                        getFieldValue(paiement.getRetenueIr() != null ?
                                paiement.getRetenueIr().stripTrailingZeros().toPlainString().replace(".", ",") : "0,00") +
                                " DH"))
                .setFontSize(10)
                .setFontColor(ALERT_RED)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(3);
        innerTable.addCell(retenueIRLabel);
        innerTable.addCell(retenueIRValue);

        // NET À PAYER banner - full width blue background, white text, bold, larger size
        Cell netLabelCell = new Cell().add(new Paragraph("NET À PAYER :"))
                .setBold()
                .setFontSize(11)
                .setFontColor(NET_LABEL_GRAY)
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(5)
                .setBorderTop(new SolidBorder(LIGHT_BORDER, 1f));
        Cell netValueCell = new Cell().add(new Paragraph(
                        getFieldValue(paiement.getMontantNet() != null ?
                                paiement.getMontantNet().stripTrailingZeros().toPlainString().replace(".", ",") : "0,00") +
                                " DH"))
                .setBold()
                .setFontSize(11)
                .setFontColor(NET_LABEL_GRAY)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(5)
                .setBorderTop(new SolidBorder(LIGHT_BORDER, 1f));

        innerTable.addCell(netLabelCell);
        innerTable.addCell(netValueCell);

        // Add inner table to outer table
        Cell outerCell = new Cell().add(innerTable)
                .setBorder(Border.NO_BORDER);
        outerTable.addCell(outerCell);

        document.add(outerTable);
    }

    private void addFooterValidation(Document document, BigDecimal montantNet) {
        // Paragraphe centré : "Arrêté par nous sous-Ordonnateur à la somme de: "
        Paragraph labelPara = new Paragraph("Arrêté par nous sous-Ordonnateur à la somme de: ")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setFontColor(BLACK);
        document.add(labelPara);

        // Ligne suivante : montant en toutes lettres (gras, centré)
        String amountInWords = getMontantEnLettres(montantNet);
        Paragraph amountPara = new Paragraph(amountInWords)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(10)
                .setFontColor(BLACK);
        document.add(amountPara);

        // Ensuite : "Fait à Oujda, le __________"
        Paragraph datePara = new Paragraph("Fait à Oujda, le __________")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setFontColor(BLACK);
        document.add(datePara);

        // Ensuite : "le sous-Ordonnateur" (gras, centré)
        Paragraph rolePara = new Paragraph("le sous-Ordonnateur")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setBold()
                .setFontColor(BLACK);
        document.add(rolePara);
    }

    // ========== HELPER METHODS ==========

    private String getDynamicTitle(TypePaiement type) {
        switch (type) {
            case VACATAIRE:
                return TITRE_BASE + "VACATION";
            case HEURE_SUP:
                return TITRE_BASE + "HEURES SUPPLEMENTAIRE";
            case DEPLACEMENT:
                return TITRE_BASE + "DEPLACEMENT";
            default:
                throw new IllegalArgumentException("Unknown payment type: " + type);
        }
    }

    private String getTexteAdministratif(TypePaiement type) {
        switch (type) {
            case VACATAIRE:
                return "Vu le décret n°2-93-534 du 20 Rabia I 1414 (8 septembre 1993) modifié par le décret n°2-02-427 du 9/10/2002 fixant les taux de vacations pour les heures de cours du personnel.";
            default:
                // HEURE_SUP and DEPLACEMENT
                return "VUE LE DECRET N°2.24.143 DU 05 RAJAB 1446 (06/01/2025) VUE LE DECRET N°2.25.539 DU 18 RABII 1ER 1447 (11/09/2025) FIXANT LES CONDITIONS D’ATTRIBUTION D’UNE INDEMNITÉ AUX FONCTIONNAIRES CHARGÉS DE L’ENCADREMENT ET DE L’ANIMATION DES STAGES DE FORMATION CONTINUE";
        }
    }

    private void addHorizontalLine(Document document, Color color, float width, float marginBottom) {
        // En utilisant une table avec une cellule et une bordure inférieure
        Table lineTable = new Table(1);
        lineTable.setWidth(UnitValue.createPercentValue(100));
        lineTable.setMarginBottom(marginBottom);
        Cell cell = new Cell()
                .setBorderBottom(new SolidBorder(color, width))
                .setPadding(0);
        lineTable.addCell(cell);
        document.add(lineTable);
    }

    // Helper for budget block values: black, bold, with dotted underline
    private void addBudgetFieldRow(Table table, String label, String value) {
        Cell labelCell = new Cell().add(new Paragraph(label))
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setBorder(Border.NO_BORDER);
        Cell valueCell = new Cell().add(new Paragraph(value))
                .setBold()
                .setFontColor(BLACK)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2); // space for dotted line
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // Helper for beneficiary block values: black, bold, with dotted underline
    private void addBeneficiaryFieldRow(Table table, String label, String value) {
        Cell labelCell = new Cell().add(new Paragraph(label))
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setBorder(Border.NO_BORDER);
        Cell valueCell = new Cell().add(new Paragraph(value))
                .setBold()
                .setFontColor(BLACK)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new DottedBorder(BLACK, 1f))
                .setPaddingBottom(2);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // Helper for operations table data cells: thin gray borders
    private void addOperationsDataCell(Table table, String value, Color bg) {
        Cell cell = new Cell().add(new Paragraph(value)
                .setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(LIGHT_BORDER, 1f))
                .setPadding(3);
        if (bg != null) {
            cell.setBackgroundColor(bg);
        }
        table.addCell(cell);
    }

    private void addTableHeaderCell(Table table, String text, Color bgColor, Color textColor) {
        Cell cell = new Cell().add(new Paragraph(text))
                .setBold()
                .setBackgroundColor(bgColor)
                .setFontColor(textColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
        table.addCell(cell);
    }

    private void addTableDataCell(Table table, String text) {
        Cell cell = new Cell().add(new Paragraph(text))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
        table.addCell(cell);
    }

    private void addAmountRow(Table table, String label, BigDecimal amount, Color labelColor, Color valueColor) {
        Cell labelCell = new Cell().add(new Paragraph(label))
                .setBold()
                .setFontColor(labelColor)
                .setBorder(Border.NO_BORDER);
        Cell valueCell = new Cell().add(new Paragraph(
                        getFieldValue(amount != null ?
                                amount.stripTrailingZeros().toPlainString().replace(".", ",") : "0,00") +
                                " DH"))
                .setFontColor(valueColor)
                .setBorder(Border.NO_BORDER);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String getFieldValue(String value) {
        return Objects.requireNonNullElse(value, "");
    }

    private String getFormattedRib(Professeur professeur) {
        if (professeur == null) {
            return "";
        }
        String banque = getFieldValue(professeur.getRibBanque());
        String ville = getFieldValue(professeur.getRibVille());
        String compte = getFieldValue(professeur.getRibNumeroCompte());
        String cle = getFieldValue(professeur.getRibCle());

        // Ne retourner que si toutes les parties sont présentes (ou au moins pas toutes vides)
        if (banque.isEmpty() && ville.isEmpty() && compte.isEmpty() && cle.isEmpty()) {
            return "";
        }
        return String.format("%s %s %s %s", banque, ville, compte, cle);
    }

    private String getMontantEnLettres(BigDecimal montant) {
        if (montant == null) {
            montant = BigDecimal.ZERO;
        }
        return MontantEnLettresConverter.convertir(montant);
    }
}