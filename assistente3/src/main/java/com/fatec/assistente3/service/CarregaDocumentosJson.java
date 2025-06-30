package com.fatec.assistente3.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.model.openai.OpenAiTokenizer;
@Service
public class CarregaDocumentosJson {

	public List<TextSegment> loadJsonDocuments(String resourcePath, int maxTokensPerChunk, int overlapTokens) throws IOException {
	    Logger logger = LogManager.getLogger(this.getClass());
	    logger.info(">>>>>> Carregamento dos documentos");

	    List<TextSegment> textSegments = new ArrayList<>();
	    String jsonContent;
	    JsonDownloader loaderJson = new JsonDownloader();

	    if (resourcePath.startsWith("http://") || resourcePath.startsWith("https://")) {
	        jsonContent = loaderJson.downloadJson(resourcePath);
	    } else {
	        InputStream inputStream = MongoConnectionService.class.getClassLoader().getResourceAsStream(resourcePath);
	        if (inputStream == null) {
	            throw new FileNotFoundException("Resource not found: " + resourcePath);
	        }
	        jsonContent = new BufferedReader(new InputStreamReader(inputStream))
	                .lines().collect(Collectors.joining("\n"));
	    }

	    logger.info(">>>>>> Mapeamento dos objetos");

	    ObjectMapper objectMapper = new ObjectMapper();
	    BufferedReader reader = new BufferedReader(new StringReader(jsonContent));

	    int batchSize = 500;
	    List<Document> batch = new ArrayList<>();

	    logger.info(">>>>>> Iniciando o spliting dos documentos");

	    String line;
	    while ((line = reader.readLine()) != null) {
	        JsonNode jsonNode = objectMapper.readTree(line);

	        String title = jsonNode.path("title").asText(null);
	        String body = jsonNode.path("body").asText(null);
	        JsonNode metadataNode = jsonNode.path("metadata");

	        if (body != null) {
	            String text = (title != null ? title + "\n\n" + body : body);

	            Metadata metadata = new Metadata();
	            if (metadataNode != null && metadataNode.isObject()) {
	                Iterator<String> fieldNames = metadataNode.fieldNames();
	                while (fieldNames.hasNext()) {
	                    String fieldName = fieldNames.next();
	                    metadata.put(fieldName, metadataNode.path(fieldName).asText());
	                }
	            }

	            Document document = Document.from(text, metadata);
	            batch.add(document);

	            if (batch.size() >= batchSize) {
	                textSegments.addAll(splitIntoChunks(batch, maxTokensPerChunk, overlapTokens));
	                batch.clear();
	            }
	        }
	    }

	    if (!batch.isEmpty()) {
	        textSegments.addAll(splitIntoChunks(batch, maxTokensPerChunk, overlapTokens));
	    }

	    return textSegments;
	}


	private static List<TextSegment> splitIntoChunks(List<Document> documents, int maxTokensPerChunk,
			int overlapTokens) {
		// Create a tokenizer for OpenAI
		OpenAiTokenizer tokenizer = new OpenAiTokenizer(OpenAiEmbeddingModelName.TEXT_EMBEDDING_ADA_002);

		// Create a recursive document splitter with the specified token size and
		// overlap
		DocumentSplitter splitter = DocumentSplitters.recursive(maxTokensPerChunk, overlapTokens, tokenizer);

		List<TextSegment> allSegments = new ArrayList<>();
		for (Document document : documents) {
			List<TextSegment> segments = splitter.split(document);
			allSegments.addAll(segments);
		}

		return allSegments;
	}
}