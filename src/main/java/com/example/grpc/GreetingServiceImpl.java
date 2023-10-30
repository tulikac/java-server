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

    // test q - local
    // String queueName ="testq";

    // QueueClient queueClient = new QueueClientBuilder()
    // .endpoint("https://ignitedemostoragetest.queue.core.windows.net/")
    // .queueName(queueName)
    // .credential(new DefaultAzureCredentialBuilder().build())
    // .buildClient();

    // ignitewebjob store - needs to add role assignment
    // String queueName ="destinationq";

    // QueueClient queueClient = new QueueClientBuilder()
    // .endpoint("https://ignitewebjob.queue.core.windows.net/")
    // .queueName(queueName)
    // .credential(new DefaultAzureCredentialBuilder().build())
    // .buildClient();


    // Get messages from the queue
    queueClient.receiveMessages(10).forEach(
        // "Process" the message
        receivedMessage -> {
            qData = qData.concat(receivedMessage.getMessageText().toString());
        }
    );
    // END - AZURE Q //

		// START - MYSQL
		// String url="jdbc:mysql://ignite-demo-db.mysql.database.azure.com/petclinic";

    // String petData = " ";

		// System.out.println("mySQL response:");
		// try {
		// 	Class.forName("com.mysql.cj.jdbc.Driver");

		// 	Connection connection = DriverManager.getConnection(url, "petclinic", "petclinic");

		// 	Statement statement = connection.createStatement();

		// 	ResultSet resultSet = statement.executeQuery("select * from pets");

		// 	while (resultSet.next()){
		// 		System.out.println(resultSet.getInt(1) + " " + resultSet.getString(2) + " " + resultSet.getDate(3));

    //   petData = petData.concat(resultSet.getString(2) + " ");

    //   }
		// 	connection.close();
		// }
		// catch(Exception e){
		// 	System.out.println(e);
		// }
		// END - MYSQL

    // START - OpenAI Chat //
		OpenAIClient client = new OpenAIClientBuilder()
			.credential(new AzureKeyCredential("9cbad1833fb3408292daa202d6a13aee"))
			.endpoint("https://grpc-openai.openai.azure.com")
			.buildClient();

    // qData: data set from queue storage account
    String systemContext = qData;
		
    List<ChatMessage> chatMessages = new ArrayList<>();
    chatMessages.add(new ChatMessage(ChatRole.SYSTEM, systemContext));
    chatMessages.add(new ChatMessage(ChatRole.USER, request.getName()));

    ChatCompletions chatCompletions = client.getChatCompletions("gpt-35-turbo-model",
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
