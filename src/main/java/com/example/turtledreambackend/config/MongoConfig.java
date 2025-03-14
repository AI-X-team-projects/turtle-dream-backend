package com.example.turtledreambackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * 최초 작성자 : 김동규
 * 최초 작성일 : 2025-03-13
 *
 * MongoDB 연결을 위한 설정
 *
 * */
@Configuration
@EnableMongoAuditing
public class MongoConfig {

    /**
     * MongoDB 클라이언트 객체를 생성하는 Bean
     *
     * @return MongoClient 객체 (MongoDB 연결)
     */
    @Bean
    public MongoClient mongoClient() {
        try {
            System.out.println("MongoDB 연결 시도: mongodb://localhost:27017");
            MongoClient client = MongoClients.create("mongodb://localhost:27017");
            System.out.println("MongoDB 연결 성공");
            return client;
        } catch (Exception e) {
            System.err.println("MongoDB 연결 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * MongoTemplate 객체를 생성하는 Bean
     *
     * @return MongoTemplate 객체 (MongoDB와의 상호작용을 위한 템플릿)
     */
    @Bean
    public MongoTemplate mongoTemplate() {
        try {
            MongoTemplate template = new MongoTemplate(mongoClient(), "turtledream");
            System.out.println("MongoDB 템플릿 생성 성공: turtledream");
            return template;
        } catch (Exception e) {
            System.err.println("MongoDB 템플릿 생성 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}