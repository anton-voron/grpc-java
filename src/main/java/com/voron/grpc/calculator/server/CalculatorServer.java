package com.voron.grpc.calculator.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class CalculatorServer {
    public static void main(String[] args) {
        System.out.println("Calculator Server started");
        Server server = ServerBuilder.forPort(50052)
                .addService(new CalculatorServerImpl())
                .build();

        Runtime.getRuntime().addShutdownHook(
                new Thread(
                        () -> {
                            System.out.println("Received Shutdown request");
                            server.shutdown();
                            System.out.println("Successfully stopped the server");
                        }
                )
        );

        try {
            server.start();

            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
