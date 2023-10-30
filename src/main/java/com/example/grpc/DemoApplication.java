// package com.example.demo;

package com.example.grpc;

import io.grpc.*;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

// MYSQL
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Statement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.openai.models.Completions;

@SpringBootApplication
@RestController
public class DemoApplication {

	public static void main(String[] args) throws Exception
	{

		// SPRING BOOT SERVER
		SpringApplication.run(DemoApplication.class, args);

		// gRPC SERVER
		Server server = ServerBuilder.forPort(8585)
		.addService(new GreetingServiceImpl())
		.build();
	
		// Start gRPC server
		server.start();

		// Server threads are running in the background.
		System.out.println("gRPC server started listening on 8585 - (Local Dev) Spring App=8282");
		
		// Don't exit the main thread. Wait until server is terminated.
		server.awaitTermination();

	}

	@RequestMapping("/")
	String sayHello() {
		return "gRPC server ready for requests.";
	}
}
