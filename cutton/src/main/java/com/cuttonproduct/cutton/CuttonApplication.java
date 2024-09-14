package com.cuttonproduct.cutton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {org.springframework.ai.autoconfigure.vectorstore.mongo.MongoDBAtlasVectorStoreAutoConfiguration.class})
public class CuttonApplication {

	public static void main(String[] args) {
		SpringApplication.run(CuttonApplication.class, args);
	}

}
