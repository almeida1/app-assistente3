package com.fatec.assistente3.controller;


import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fatec.assistente3.model.PerguntaRequest;
import com.fatec.assistente3.service.PdfToJsonConverter;
import com.fatec.assistente3.service.RagService;

@RestController
@RequestMapping("/rag")
public class AssistenteController {

    @Autowired
    private RagService ragService;

    @PostMapping("/consultar")
    public ResponseEntity<String> consultar(@RequestBody PerguntaRequest requesicao) throws IOException {
        String resposta = ragService.responderPergunta(requesicao.getPergunta());
        return ResponseEntity.ok(resposta);
    }
    @PostMapping("/carregarpdf")
    public ResponseEntity<String> carregarpdf(@RequestBody PerguntaRequest requesicao) throws IOException {
    	PdfToJsonConverter converter = new PdfToJsonConverter();
        converter.converterParaJson("src/main/resources/Improve Software Testing  29110.pdf", "src/main/resources/test-docs.json");
    	return null;
    }
}
