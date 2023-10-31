package com.example.grpc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.core.credential.AzureKeyCredential;

// AZURE Q
import com.azure.identity.*;
import com.azure.storage.queue.*;
import com.azure.storage.queue.models.*;
import java.io.*;

import io.grpc.stub.StreamObserver;

public class GreetingServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

  String qData = " ";

  @Override
  public void greeting(GreetingServiceOuterClass.HelloRequest request,
        StreamObserver<GreetingServiceOuterClass.HelloResponse> responseObserver) {

    // START - AZURE Q //

    // test q prod
    //String connectStr = System.getenv("AZURE_STORAGE_CONNECTION_STRING");

    // Create a unique name for the queue
    String queueName ="destinationq";

    // Instantiate a QueueClient
    // We'll use this client object to create and interact with the queue
    QueueClient queueClient = new QueueClientBuilder()
            .connectionString("DefaultEndpointsProtocol=https;AccountName=ignitewebjob;AccountKey=X+JqpubLLFKrzqRm+/qXNB+f3mbHI8x1MnjZYTg0ZJiVB0Ykr3IDJ2lrNO0KQsbg6XkXOxJqunHa+AStlrFJwA==;EndpointSuffix=core.windows.net")
            .queueName(queueName)
            .buildClient();

  

    // Get messages from the queue
    queueClient.receiveMessages(10).forEach(
        // "Process" the message
        receivedMessage -> {
            qData = qData.concat(receivedMessage.getMessageText().toString());
        }
    );
    // END - AZURE Q //

	

    // START - OpenAI Chat //
    OpenAIClient client = new OpenAIClient(
        new Uri("https://ignite-demo-apim.azure-api.net/deployments/deploy1/chat/completions?api-version=INSERT_API_VERSION_HERE"),
        new AzureKeyCredential("e448e88135da4b30ab51baf117cae890"));

		//OpenAIClient client = new OpenAIClientBuilder()
		//	.credential(new AzureKeyCredential("dc7b76ac36874ea0aef8674840e31eab"))
		//	.endpoint("https://ignite-demo-openai.openai.azure.com/")
		//	.buildClient();

    // qData: data set from queue storage account
    String systemContext = qData;
		
    List<ChatMessage> chatMessages = new ArrayList<>();
    chatMessages.add(new ChatMessage(ChatRole.SYSTEM, systemContext));
    chatMessages.add(new ChatMessage(ChatRole.USER, request.getName()));

    ChatCompletions chatCompletions = client.getChatCompletions("deploy1",
        new ChatCompletionsOptions(chatMessages));

    String outputChat = " ";

    for (ChatChoice choice : chatCompletions.getChoices()) {
        ChatMessage message = choice.getMessage();
        outputChat = message.getContent();
    }

    System.out.printf("Expected Output: " + outputChat);
    // END - OpenAI Chat //

    GreetingServiceOuterClass.HelloResponse response = GreetingServiceOuterClass.HelloResponse.newBuilder()
    .setGreeting(outputChat)
    .build();

    // Use responseObserver to send a single response back
    responseObserver.onNext(response);

    // When you are done, you must call onCompleted.
    responseObserver.onCompleted();
  }
}
