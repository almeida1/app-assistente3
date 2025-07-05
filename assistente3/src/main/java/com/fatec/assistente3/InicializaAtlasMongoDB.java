package com.fatec.assistente3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fatec.assistente3.service.MongoConnectionService;

@Component
public class InicializaAtlasMongoDB implements CommandLineRunner {

	private final MongoConnectionService mongoService;
	Logger logger = LogManager.getLogger(this.getClass());

	public InicializaAtlasMongoDB(MongoConnectionService mongoService) {
		this.mongoService = mongoService;
	}

	@Override
	public void run(String... args) {
		try {
			mongoService.testConnection();
		} catch (BeanCreationException e) {
			logger.info(">>>>>> Erro na configuração do MongoDB=> " + e.getMessage());
		}
	}
}
