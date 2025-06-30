package com.fatec.assistente3.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.segment.TextSegment;
import jakarta.annotation.PostConstruct;

@Service
public class RagService {

    @Autowired
    private CarregaDocumentosJson loader;

    private List<TextSegment> documents;

    @PostConstruct
    public void carregarBase() throws IOException {
        String resourcePath = "test-docs.json";
        documents = loader.loadJsonDocuments(resourcePath, 800, 200);
    }

    public String responderPergunta(String pergunta) {
        // Busca simples simulando recuperação por palavra-chave
        return documents.stream()
                .filter(segment -> segment.text().toLowerCase().contains(pergunta.toLowerCase()))
                .findFirst()
                .map(TextSegment::text)
                .orElse("Nenhuma resposta encontrada para a pergunta: " + pergunta);
    }
}
