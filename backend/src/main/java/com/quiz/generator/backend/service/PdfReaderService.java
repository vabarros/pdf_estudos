package com.quiz.generator.backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfReaderService {

    public String readPdf(MultipartFile file, Integer startPage, Integer endPage) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();

            // Configurar o intervalo de páginas se fornecido
            if (startPage != null && startPage > 0) {
                pdfStripper.setStartPage(startPage);
            }
            if (endPage != null && endPage > 0 && endPage >= startPage) {
                pdfStripper.setEndPage(endPage);
            } else if (endPage != null && endPage < startPage) {
                System.err.println("A página final não pode ser menor que a página inicial. Ignorando endPage.");
                pdfStripper.setEndPage(document.getNumberOfPages()); // Usa todas as páginas a partir da inicial
            } else {
                pdfStripper.setEndPage(document.getNumberOfPages()); // Padrão: vai até o final
            }

            String text = pdfStripper.getText(document);
            return text;
        }
    }
}