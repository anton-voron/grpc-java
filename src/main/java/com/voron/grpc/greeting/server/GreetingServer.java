package com.voron.grpc.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GreetingServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello GRPC");

        Server server = ServerBuilder.forPort(8000)
                .addService(new GreetServerImpl())
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            System.out.println("Received Shutdown request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        } ));

        server.awaitTermination();

    }
}
