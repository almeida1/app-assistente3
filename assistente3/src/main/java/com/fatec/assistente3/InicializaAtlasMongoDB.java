package com.fatec.assistente3;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fatec.assistente3.service.MongoConnectionService;

@Component
public class InicializaAtlasMongoDB implements CommandLineRunner {

    private final MongoConnectionService mongoService;

    public InicializaAtlasMongoDB(MongoConnectionService mongoService) {
        this.mongoService = mongoService;
    }

    @Override
    public void run(String... args) {
        mongoService.testConnection();
    }
}
