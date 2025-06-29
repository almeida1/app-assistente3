package com.fatec.assistente3;

import java.util.HashSet;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;

@SpringBootApplication
public class Assistente3Application {

	public static void main(String[] args) {
		//Logger logger = LogManager.getLogger(this.getClass());
		SpringApplication.run(Assistente3Application.class, args);
		String CONNECTION_URI = System.getenv("CONNECTION_URI");
		
		ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(CONNECTION_URI)).serverApi(serverApi).build();
		// Create a new client and connect to the server
		try (MongoClient mongoClient = MongoClients.create(settings)) {
			try {
				// Send a ping to confirm a successful connection
				MongoDatabase database = mongoClient.getDatabase("admin");
				database.runCommand(new Document("ping", 1));
				System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
				// Embedding Store  
				EmbeddingStore<TextSegment> embeddingStore = createEmbeddingStore(mongoClient);
				// Embedding Model setup  
				OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()  
				        .apiKey("OPEN_AI_API_KEY")  
				        .modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_ADA_002)  
				        .build();
				// Chat Model setup
	            ChatLanguageModel chatModel = OpenAiChatModel.builder()
	                    .apiKey("OPEN_AI_API_KEY")
	                    .modelName("gpt-4")
	                    .build();
	            //https://dev.to/mongodb/how-to-make-a-rag-application-with-langchain4j-1mad
			} catch (MongoException e) {
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
