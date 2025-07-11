package com.fatec.assistente3.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;

@Service
public class MongoConnectionService {
	CarregaDocumentosJson incorporaDoc;
	private static final Logger logger = LogManager.getLogger(MongoConnectionService.class);
	// o MongoClient configurado pelo Spring Boot ao ler o arquivo application.properties
	private MongoClient mongoClient;

	//O Spring injetará o MongoClient configurado automaticamente
	public MongoConnectionService(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
		this.incorporaDoc = new CarregaDocumentosJson(); 
	}

	public void testConnection() {
		try {
			logger.info(">>>>>> Tentativa de conexão com o Atlas");

			// Enviar um ping para confirmar se a conexão foi bem-sucedida
			MongoDatabase database = mongoClient.getDatabase("admin"); // Ou o nome do seu banco de dados
			database.runCommand(new Document("ping", 1));
			logger.info(">>>>>> Ping realizado com sucesso! Aplicação conectada ao MongoDB.");

			// Embedding Store configurado para armazenar, recuperar e gerenciar os embedding
			// os embedding simplificam a representação de dados do mundo real, 
			// preservando as relações semânticas e sintáticas.
			// Um embedding é um vetor (uma lista de números) que representa um texto de forma numéric
			EmbeddingStore<TextSegment> embeddingStore = createEmbeddingStore(mongoClient);
			logger.info(">>>>>> Embedding Store criada.");

			// Embedding Model setup - em um abiente de producao 
			// a chave apikey deve ser recuperada de maneira segura
			OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder().apiKey("demo") 
					.modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL).build();
			logger.info(">>>>>> Embedding Model configurado.");

			// Chat Model setup
			ChatLanguageModel chatModel = OpenAiChatModel.builder().apiKey("demo") // Substitua por ${OPENAI_API_KEY} ou
																					// similar
					.modelName("gpt-4").build();
			logger.info(">>>>>> Chat Model configurado.");

			String resourcePath = "test-docs.json";
			List<TextSegment> documents = incorporaDoc.loadJsonDocuments(resourcePath, 800, 200);

			logger.info(">>>>>> Carregados " + documents.size() + " documentos.");

			for (int i = 0; i < documents.size() / 10; i++) {
				TextSegment segment = documents.get(i);
				Embedding embedding = embeddingModel.embed(segment.text()).content();
				embeddingStore.add(embedding, segment);
			}

			logger.info(">>>>>> Vetores embeddings criados e adicionados à Embedding Store.");

		} catch (IOException e) {
			logger.error("IO Exception =>" + e.getMessage(), e); // Use error para exceções
		} catch (MongoException e) {
			logger.error("MongoDB exception => " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Exceção não esperada => " + e.getMessage(), e);
		}
	}
/**
 * Objetivo - preparar onde os vetores de embeddings (representações numéricas de texto) 
 * serão armazenados e como eles poderão ser consultados para encontrar similaridades.
 * @param mongoClient
 * @return
 */
	private static EmbeddingStore<TextSegment> createEmbeddingStore(MongoClient mongoClient) {
		String databaseName = "rag_app";
		String collectionName = "embeddings";
		String indexName = "embedding";
		Long maxResultRatio = 10L;
		CreateCollectionOptions createCollectionOptions = new CreateCollectionOptions();
		Bson filter = null;
		Set<String> metadataFields = new HashSet<>();
		IndexMapping indexMapping = new IndexMapping(1536, metadataFields);
		Boolean createIndex = true;

		return new MongoDbEmbeddingStore(
				mongoClient, 
				databaseName, 
				collectionName, 
				indexName, 
				maxResultRatio,
				createCollectionOptions, 
				filter, 
				indexMapping, 
				createIndex
			);
	}

}
