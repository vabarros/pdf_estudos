package com.quiz.generator.backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfTextExtractorService {

    /**
     * Extrai texto de um arquivo PDF carregado como MultipartFile.
     *
     * @param file O MultipartFile contendo o documento PDF.
     * @return O texto extraído do PDF.
     * @throws IOException Se houver um erro ao ler o arquivo PDF.
     * @throws IllegalArgumentException Se o arquivo não for um PDF.
     */
    public String extractText(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo PDF está vazio.");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new IllegalArgumentException("O arquivo enviado não é um PDF válido.");
        }

        PDDocument document = null;
        try {
            // PDDocument.load() pode receber um InputStream diretamente do MultipartFile
            document = PDDocument.load(file.getInputStream());
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        } finally {
            if (document != null) {
                document.close(); // Garante que o documento PDF seja fechado
            }
        }
    }
}