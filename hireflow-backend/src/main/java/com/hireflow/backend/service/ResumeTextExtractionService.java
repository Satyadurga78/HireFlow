package com.hireflow.backend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ResumeTextExtractionService {

    public String extractText(byte[] resumeBytes)
            throws IOException {

        if (resumeBytes == null || resumeBytes.length == 0) {
            throw new IOException("Resume file is empty");
        }

        try (PDDocument document = Loader.loadPDF(resumeBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();

            String extractedText = stripper.getText(document);

            if (extractedText == null || extractedText.isBlank()) {
                throw new IOException(
                        "No readable text found in the resume PDF"
                );
            }

            return extractedText.trim();
        }
    }
}