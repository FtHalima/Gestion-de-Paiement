package com.gestionpaiements.app.service;

import com.gestionpaiements.app.dao.PaiementRepository;
import com.gestionpaiements.app.model.LigneDeplacement;
import com.gestionpaiements.app.model.Paiement;
import com.gestionpaiements.app.model.Professeur;
import com.gestionpaiements.app.util.MontantEnLettresConverter;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.io.image.ImageDataFactory;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

//ligne 
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.element.LineSeparator;

@Service
public class DeplacementPdfGenerationService {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private PdfGenerationService pdfGenerationService;

    private static final Color BLACK = ColorConstants.BLACK;
    private static final Color WHITE = ColorConstants.WHITE;

    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HEURE_FORMAT = DateTimeFormatter.ofPattern("H'H'");

    // Chemins des polices Windows — bascule automatiquement sur une police standard si introuvable
    private static final String CALIBRI_PATH = "C:/Windows/Fonts/calibri.ttf";
    private static final String CALIBRI_BOLD_PATH = "C:/Windows/Fonts/calibrib.ttf";
    private static final String CALIBRI_BOLD_ITALIC_PATH = "C:/Windows/Fonts/calibriz.ttf";
    private static final String TIMES_PATH = "C:/Windows/Fonts/times.ttf";


    private PdfFont calibriRegular;
    private PdfFont calibriBold;
    private PdfFont calibriBoldItalic;
    private PdfFont timesRegular;

    private void loadFonts() {
        calibriRegular = loadFont(CALIBRI_PATH, com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
        calibriBold = loadFont(CALIBRI_BOLD_PATH, com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        calibriBoldItalic = loadFont(CALIBRI_BOLD_ITALIC_PATH, com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLDOBLIQUE);
        timesRegular = loadFont(TIMES_PATH, com.itextpdf.io.font.constants.StandardFonts.TIMES_ROMAN);
    }

    private PdfFont loadFont(String path, String standardFallback) {
        try {
            File f = new File(path);
            if (f.exists()) {
                return PdfFontFactory.createFont(path, PdfEncodings.IDENTITY_H);
            }
        } catch (Exception ignored) {
        }
        try {
            return PdfFontFactory.createFont(standardFallback);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger une police PDF", e);
        }
    }

    public ByteArrayInputStream genererPdfDeplacement(Long paiementId) throws Exception {
        Paiement paiement = paiementRepository.findPaiementWithLignesDeplacementById(paiementId)
                .orElseThrow(() -> new Exception("Paiement not found with ID: " + paiementId));

        loadFonts();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        Professeur prof = paiement.getProfesseur();
        String affectation = (prof != null) ? prof.getAffectation() : null;

        pdfGenerationService.addHeader(document, affectation);
        pdfGenerationService.addMainTitle(document, com.gestionpaiements.app.model.TypePaiement.DEPLACEMENT);
        addBudgetLine(document, paiement);
        addIdentificationBlock(document, prof);
        addMotifLigne(document, paiement);
        BigDecimal total = addTrajetsTable(document, paiement);
        pdfGenerationService.addArreteDeSomme(document, total);
        pdfGenerationService.addIntermediateSignatures(document);
        pdfGenerationService.addFooterValidation(document, total);

        document.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private void addBudgetLine(Document document, Paiement paiement) {
        Paragraph line = new Paragraph()
                .setFontSize(9.5f)
                .setFontColor(BLACK)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(10);

        // EXERCICE
        line.add(new com.itextpdf.layout.element.Text("EXERCICE : ").setFont(calibriBold))
                .add(new com.itextpdf.layout.element.Text(getFieldValue(paiement.getExercice())).setFont(calibriRegular))
                .add("      ");
        
        //CREANCED'ORIGINE
        line.add(new com.itextpdf.layout.element.Text("Créance d'origine : ").setFont(calibriBold))
                .add(new com.itextpdf.layout.element.Text(getFieldValue(paiement.getCreanceDOrigine())).setFont(calibriRegular))
                .add("      ");

        // Code CGNC
        line.add(new com.itextpdf.layout.element.Text("Code CGNC : ").setFont(calibriBold))
                .add(new com.itextpdf.layout.element.Text(getFieldValue(paiement.getCodeCgnc())).setFont(calibriRegular))
                .add("             ");

        // Article
        line.add(new com.itextpdf.layout.element.Text("Article : ").setFont(calibriBold))
                .add(new com.itextpdf.layout.element.Text(getFieldValue(paiement.getArticle())).setFont(calibriRegular))
                .add("             ");

        // Paragraphe
        line.add(new com.itextpdf.layout.element.Text("Paragraphe : ").setFont(calibriBold))
                .add(new com.itextpdf.layout.element.Text(getFieldValue(paiement.getPar())).setFont(calibriRegular))
                .add("             ");

        // Ligne
        line.add(new com.itextpdf.layout.element.Text("Ligne : ").setFont(calibriBold))
                .add(new com.itextpdf.layout.element.Text(getFieldValue(paiement.getLig())).setFont(calibriRegular));

        document.add(line);

        // Ligne horizontale en gras sous l'écriture
        SolidLine solidLine = new SolidLine(1.5f);
        LineSeparator separator = new LineSeparator(solidLine);

        separator.setMarginTop(2);
        separator.setMarginBottom(8);

        document.add(separator);
                //ligne

    }



    private void addIdentificationBlock(Document document, Professeur prof) {
        String nomComplet = prof != null ? (getFieldValue(prof.getNom()) + " " + getFieldValue(prof.getPrenom())) : "";
        String ppr = prof != null ? getFieldValue(prof.getPpr()) : "";
        String cin = prof != null ? getFieldValue(prof.getCin()) : "";
        String cadre = prof != null ? getFieldValue(prof.getGrade()) : "";
        String grade = prof != null && prof.getEchelle() != null ? prof.getEchelle().toString() : "";
        String lieuTravail = prof != null ? getFieldValue(prof.getAffectation()) : "";
        String compteN = prof != null ? (getFieldValue(prof.getRibBanque()) + " " + getFieldValue(prof.getRibVille()) + " "
                + getFieldValue(prof.getRibNumeroCompte()) + " " + getFieldValue(prof.getRibCle())).trim() : "";

        Table table = new Table(new float[]{1f, 1f});
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(10);

        // Ligne 1 : Présenté par | PPR
        table.addCell(cellNoBorderIdentificationLeft(new Paragraph()
                .add(new com.itextpdf.layout.element.Text("Présenté par: M, Mme:        ").setFont(calibriBold).setFontSize(10.5f))
                .add(new com.itextpdf.layout.element.Text(nomComplet).setFont(calibriBold))));
        table.addCell(cellNoBorderIdentificationRight(new Paragraph()
                .add(new com.itextpdf.layout.element.Text("PPR:        ").setFont(calibriBold).setFontSize(10.5f))
                .add(new com.itextpdf.layout.element.Text(ppr).setFont(calibriBold))));

        // Ligne 2 : CIN | Cadre
        table.addCell(cellNoBorderIdentificationLeft(new Paragraph()
                .add(new com.itextpdf.layout.element.Text("CIN:        ").setFont(calibriBold).setFontSize(10.5f))
                .add(new com.itextpdf.layout.element.Text(cin).setFont(calibriBold))));
        table.addCell(cellNoBorderIdentificationRight(new Paragraph()
                .add(new com.itextpdf.layout.element.Text("Cadre:        ").setFont(calibriBold).setFontSize(10.5f))
                .add(new com.itextpdf.layout.element.Text(cadre).setFont(calibriBold))));

        // Ligne 3 : Grade | Lieu de travail (même ligne, comme demandé)
        table.addCell(cellNoBorderIdentificationLeft(new Paragraph()
                .add(new com.itextpdf.layout.element.Text("Grade:        ").setFont(calibriBold).setFontSize(10.5f))
                .add(new com.itextpdf.layout.element.Text(grade).setFont(calibriBold))));
        table.addCell(cellNoBorderIdentificationRight(new Paragraph()
                .add(new com.itextpdf.layout.element.Text("Lieu de travail:        ").setFont(calibriBold).setFontSize(10.5f))
                .add(new com.itextpdf.layout.element.Text(lieuTravail).setFont(calibriBold))));

        // Ligne 4 : Compte N° — UNE SEULE FOIS, centré, pleine largeur
        Cell compteCell = new Cell(1, 2).add(new Paragraph()
                .add(new com.itextpdf.layout.element.Text("Compte N°:        ").setFont(calibriBold).setFontSize(10.5f))
                .add(new com.itextpdf.layout.element.Text(compteN).setFont(calibriBold)))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(2);
        table.addCell(compteCell);

        document.add(table);
        }

    private Cell cellNoBorderIdentificationLeft(Paragraph p) {
        return new Cell().add(p.setTextAlignment(TextAlignment.LEFT))
                .setBorder(Border.NO_BORDER)
                .setPadding(2);
    }

    private Cell cellNoBorderIdentificationRight(Paragraph p) {
        return new Cell().add(p.setTextAlignment(TextAlignment.LEFT)) // Label left, value will follow left alignment
                .setBorder(Border.NO_BORDER)
                .setPadding(2);
    }

    private Cell cellNoBorder(Paragraph p, TextAlignment align) {
        return new Cell().add(p.setTextAlignment(align)).setBorder(Border.NO_BORDER).setPadding(2);
    }

    private void addMotifLigne(Document document, Paiement paiement) {
        Paragraph p = new Paragraph()
                .add(new com.itextpdf.layout.element.Text("Motif de déplacement: ").setFont(calibriBold).setFontSize(10.5f))
                .add(new com.itextpdf.layout.element.Text(getFieldValue(paiement.getMotifDeplacement())).setFont(calibriRegular).setFontSize(10.5f))
                .setMarginBottom(8);
        document.add(p);
    }

    private BigDecimal addTrajetsTable(Document document, Paiement paiement) {
        Table table = new Table(new float[]{1f, 1f, 2.2f, 0.8f, 0.8f, 0.9f, 0.9f, 1.1f});
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(4);

        // Ligne d'en-tête 1
        addHeaderCell(table, "Date de déplacement", 1, 2);
        addHeaderCell(table, "Indication Précise du parcours", 2, 1);
        addHeaderCell(table, "Heures", 1, 2);
        addHeaderCell(table, "Nb.De Taux Taux de Base", 2, 1);
        addHeaderCell(table, "Taux de Base Appliqué", 2, 1);
        addHeaderCell(table, "Montant", 2, 1);

        // Ligne d'en-tête 2 (sous-colonnes)
        addHeaderCell(table, "Départ", 1, 1);
        addHeaderCell(table, "Arrivée", 1, 1);
        addHeaderCell(table, "Départ", 1, 1);
        addHeaderCell(table, "Retour", 1, 1);

        BigDecimal total = BigDecimal.ZERO;
        var trajets = paiement.getLignesDeplacement().stream()
                .filter(this::estTrajetRempli).toList();


        for (LigneDeplacement l : trajets) {
                addDataCellCalibri(table, l.getDateDepart() != null ? l.getDateDepart().format(DATE_FORMAT) : "");
                addDataCellCalibri(table, l.getDateArrivee() != null ? l.getDateArrivee().format(DATE_FORMAT) : "");
                addDataCellCalibri(table, getFieldValue(l.getParcours()));
                addDataCellTimes(table, l.getHeureDepart() != null ? l.getHeureDepart().format(HEURE_FORMAT) : "");
                addDataCellTimes(table, l.getHeureRetour() != null ? l.getHeureRetour().format(HEURE_FORMAT) : "");
                addDataCellCalibri(table, l.getNombreTauxBase() != null ? l.getNombreTauxBase().stripTrailingZeros().toPlainString() : "");
                addDataCellCalibri(table, l.getTauxBaseApplique() != null ? l.getTauxBaseApplique().stripTrailingZeros().toPlainString() : "");

                BigDecimal montant = l.getMontant() != null ? l.getMontant() : BigDecimal.ZERO;
                total = total.add(montant);
                Cell montantCell = new Cell().add(new Paragraph(formatMontant(montant))
                                .setFont(calibriBold).setFontSize(9))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorder(new SolidBorder(BLACK, 1.2f))
                        .setPadding(3);
                table.addCell(montantCell);
        }

// Ligne Total Général : label sur 5 colonnes (Dates + Parcours + Heures),
        // Somme du "Nombre de taux de base" sur tous les trajets
        BigDecimal sommeNombre = BigDecimal.ZERO;
        for (LigneDeplacement l : trajets) {
        if (l.getNombreTauxBase() != null) {
                sommeNombre = sommeNombre.add(l.getNombreTauxBase());
        }
        }

        // Colonnes Dates + Parcours + Heures : vides sur cette ligne
        Cell emptyDatesParcoursHeures = new Cell(1, 5).add(new Paragraph(""))
                .setBorder(new SolidBorder(BLACK, 1.2f))
                .setPadding(4);
        table.addCell(emptyDatesParcoursHeures);

        // Somme du nombre de taux de base, sous sa propre colonne
        Cell sommeNombreCell = new Cell().add(new Paragraph(sommeNombre.stripTrailingZeros().toPlainString())
                        .setFont(calibriBold).setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(BLACK, 1.2f))
                .setPadding(4);
        table.addCell(sommeNombreCell);

        // Label "Total Général" dans la colonne "Taux appliqué"
        Cell totalLabel = new Cell().add(new Paragraph("Total Général")
                        .setFont(calibriRegular).setFontSize(9).setBold())
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(BLACK, 1.2f))
                .setPadding(4);
        table.addCell(totalLabel);

        // Montant : total général dans la colonne "Montant"
        Cell totalValue = new Cell().add(new Paragraph(formatMontant(total))
                        .setFont(calibriBold).setFontSize(9.72f))
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(BLACK, 1.2f))
                .setPadding(4);
        table.addCell(totalValue);


        document.add(table);
        return total;
        }

    private void addHeaderCell(Table table, String text, int rowSpan, int colSpan) {
        Cell cell = new Cell(rowSpan, colSpan).add(new Paragraph(text).setFont(calibriBold).setFontSize(8.28f))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBackgroundColor(WHITE)
                .setBorder(new SolidBorder(BLACK, 1.2f))
                .setPadding(2);
        table.addCell(cell);
    }

    private boolean estTrajetRempli(LigneDeplacement ligne) {
        return ligne != null && (ligne.getDateDepart() != null || ligne.getDateArrivee() != null
                || !getFieldValue(ligne.getParcours()).isBlank()
                || ligne.getHeureDepart() != null || ligne.getHeureRetour() != null
                || estNonNul(ligne.getNombreTauxBase()) || estNonNul(ligne.getTauxBaseApplique())
                || estNonNul(ligne.getMontant()));
    }

    private boolean estNonNul(BigDecimal valeur) {
        return valeur != null && valeur.signum() != 0;
    }

    private void addDataCellCalibri(Table table, String value) {
        Cell cell = new Cell().add(new Paragraph(value).setFont(calibriRegular).setFontSize(8.28f))
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(BLACK, 1.2f))
                .setPadding(2);
        table.addCell(cell);
    }

    private void addDataCellTimes(Table table, String value) {
        Cell cell = new Cell().add(new Paragraph(value).setFont(timesRegular).setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(BLACK, 1.2f))
                .setPadding(2);
        table.addCell(cell);
    }

    

    private String getFieldValue(String value) {
        return value != null ? value : "";
    }

    private String formatMontant(BigDecimal montant) {
        if (montant == null) montant = BigDecimal.ZERO;
        return montant.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString().replace(".", ",");
    }
}
