package com.fatec.assistente3.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
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
	
	public void testConnection() {
		//liberar o ip de acesso no mongodb
		Logger logger = LogManager.getLogger(this.getClass());
		logger.info(">>>>>> Tentativa de conexao com o Atlas");
		incorporaDoc = new CarregaDocumentosJson();

		String CONNECTION_URI = System.getenv("CONNECTION_URI");
		
		ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(CONNECTION_URI)).serverApi(serverApi).build();
		logger.info(">>>>>> Configuração realizada");
		// Create a new client and connect to the server
		try (MongoClient mongoClient = MongoClients.create(settings)) {
			try {
				// Send a ping to confirm a successful connection
				MongoDatabase database = mongoClient.getDatabase("admin");
				database.runCommand(new Document("ping", 1));
				logger.info(">>>>>> Ping realizado com sucesso app conectado para o MongoDB");
				// Embedding Store  
				EmbeddingStore<TextSegment> embeddingStore = createEmbeddingStore(mongoClient);
				// Embedding Model setup  
				OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()  
				        .apiKey("demo")  
				        .modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL)  
				        .build();
				// Chat Model setup
	            ChatLanguageModel chatModel = OpenAiChatModel.builder()
	                    .apiKey("demo")
	                    .modelName("gpt-4")
	                    .build();
	            // Load documents
	           // String resourcePath = "https://huggingface.co/datasets/MongoDB/devcenter-articles/raw/main/devcenter-content-snapshot.2024-05-20.json";
	            String resourcePath = "test-docs.json";
	            
	            // JsonDownloader loaderJson = new JsonDownloader();
	           // String dados = loaderJson.downloadJson(resourcePath);
	            List<TextSegment> documents = incorporaDoc.loadJsonDocuments(resourcePath, 800, 200);

	            logger.info(">>>>>> Loaded " + documents.size() + " documents");

	            for (int i = 0; i < documents.size()/10; i++) {
	                TextSegment segment = documents.get(i);
	                Embedding embedding = embeddingModel.embed(segment.text()).content();
	                embeddingStore.add(embedding, segment);
	            }

	            logger.info(">>>>>> Vetores embeddings criados");
	           
				// https://dev.to/mongodb/how-to-make-a-rag-application-with-langchain4j-1mad
			} 
			catch (IOException e) {
				System.out.println(e.getMessage());
			}
			catch (MongoException e) {
				e.printStackTrace();
			}
		}
	}
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
