package com.thiago;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;


@Configuration
@EnableMongoRepositories(basePackages = "com.thiago.repository")
@PropertySource("classpath:application.properties")
public class MongoConfig extends AbstractMongoConfiguration {	
	@Value("${mongodb.host}")
	private String host; 
	
	@Value("${mongodb.port}")
	private int port;

	@Value("${mongodb.database}")
	private String database; 

	
	@Override
	protected String getDatabaseName() {
		return database;
	}

	public MongoClient mongoClient() {
		return new MongoClient(host, port);
	}

	@Override
	protected String getMappingBasePackage() {
		return "com.thiago";
	}
}
