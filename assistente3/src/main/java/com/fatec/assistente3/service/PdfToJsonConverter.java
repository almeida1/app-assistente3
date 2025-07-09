package com.fatec.assistente3.service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PdfToJsonConverter {

    public void converterParaJson(String pdfPath, String jsonPath) throws IOException {
        PDDocument document = PDDocument.load(new File(pdfPath));
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String texto = pdfStripper.getText(document);
        document.close();

        // Dividir por parágrafos
        String[] paragrafos = texto.split("\\r?\\n\\r?\\n");

        List<Map<String, Object>> documentos = new ArrayList<>();

        int contador = 1;
        for (String paragrafo : paragrafos) {
            if (paragrafo.trim().length() > 50) { // Ignorar quebras vazias
                Map<String, Object> doc = new HashMap<>();
                doc.put("title", "Segmento " + contador);
                doc.put("body", paragrafo.trim());
                doc.put("metadata", Map.of("origem", "PDF"));

                documentos.add(doc);
                contador++;
            }
        }

        // Salvar como JSON linha por linha
        ObjectMapper mapper = new ObjectMapper();
        try (FileWriter writer = new FileWriter(jsonPath)) {
            for (Map<String, Object> doc : documentos) {
                writer.write(mapper.writeValueAsString(doc) + "\n");
            }
        }

        System.out.println("Conversão concluída: " + documentos.size() + " segmentos exportados para JSON.");
    }
}
