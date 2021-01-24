package com.voron.grpc.calculator.client;

import com.google.common.util.concurrent.ListenableFuture;
import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.ExecutionException;

public class CalculatorClient {

    private static void asyncSum(CalculatorServiceGrpc.CalculatorServiceFutureStub asyncClient) {
        int firstNumber = 4;
        int secondNumber = 3;

        SumRequest request = SumRequest.newBuilder()
                .setFirstNumber(firstNumber)
                .setSecondNumber(secondNumber)
                .build();

        ListenableFuture<SumResponse> future = asyncClient.sum(request);
        try {
            SumResponse response =  future.get();
            System.out.println("Calculator async Sum Response: " + response.getResult());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void syncSum(CalculatorServiceGrpc.CalculatorServiceBlockingStub syncClient) {
        int firstNumber = 4;
        int secondNumber = 3;

        SumRequest request = SumRequest.newBuilder()
                .setFirstNumber(firstNumber)
                .setSecondNumber(secondNumber)
                .build();

        SumResponse response = syncClient.sum(request);
        System.out.println("Calculator sync Sum Response: " + response.getResult());
    }

    public static void main(String[] args) {
        System.out.println("Hello I am Calculator client");
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        CalculatorServiceGrpc.CalculatorServiceBlockingStub syncClient = CalculatorServiceGrpc.newBlockingStub(channel);
        CalculatorServiceGrpc.CalculatorServiceFutureStub asyncClient = CalculatorServiceGrpc.newFutureStub(channel);

        asyncSum(asyncClient);
        syncSum(syncClient);

        channel.shutdown();

    }
}
