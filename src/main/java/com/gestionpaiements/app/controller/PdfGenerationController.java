package com.gestionpaiements.app.controller;

import com.gestionpaiements.app.service.PdfGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

/**
 * Controller for generating PDF État des sommes dues for payments.
 */
@RestController
public class PdfGenerationController {

    @Autowired
    private PdfGenerationService pdfGenerationService;

    /**
     * Generates and downloads the PDF État des sommes dues for a payment.
     *
     * @param paiementId the ID of the payment
     * @return the PDF as a downloadable file
     * @throws Exception if payment not found or error during generation
     */
    @GetMapping("/paiements/{id}/pdf")
    public ResponseEntity<ByteArrayResource> genererPdfEstadoSums(@PathVariable Long paiementId) throws Exception {
        ByteArrayInputStream pdfInputStream = pdfGenerationService.genererPdfEstadoSums(paiementId);

        // Prepare the response
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=etat_sommes_dues_" + paiementId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(pdfInputStream.readAllBytes()));
    }
}