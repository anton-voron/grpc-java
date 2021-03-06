package com.voron.grpc.greeting.client;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Futures;
import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

public class GreetingClient {

    private ManagedChannel channel;

    public void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 8000)
                .usePlaintext()
                .build();
//        asyncGreet(channel);
//        syncGreet(channel);
//        syncGreetManyTime(channel);
//        asyncGreetManyTime(channel);
//        syncLongGreet(channel);
        greetEveryOne(channel);

        channel.shutdown();
    }


    // Unary call
    private static void asyncGreet (ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceFutureStub client = GreetServiceGrpc.newFutureStub(channel);
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Tom")
                .setLastName("Jerry")
                .build();

        GreetRequest request = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        ListenableFuture<GreetResponse> future = client.greet(request);
        Futures.addCallback(future, new FutureCallback<GreetResponse>() {
            @Override
            public void onSuccess(GreetResponse response) {
                System.out.println("Server response: " + response.getResult());
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, MoreExecutors.directExecutor());
    }

    private static void syncGreet(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub client = GreetServiceGrpc.newBlockingStub(channel);
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Tom")
                .setLastName("Jerry")
                .build();

        GreetRequest request = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        GreetResponse response = client.greet(request);
        System.out.println("doUnaryCallSync: " + response.getResult());
    }


    // Server streaming
    private static void syncGreetManyTime(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub client = GreetServiceGrpc.newBlockingStub(channel);
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Paulo")
                .setLastName("Karachi")
                .build();

        GreetManyTimesRequest request = GreetManyTimesRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        client.greetManyTimes(request).forEachRemaining(response -> {
            System.out.println("Response manyTime: " + response);
        });
    }

    private static void asyncGreetManyTime(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asynClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Paulo")
                .setLastName("Karachi")
                .build();

        GreetManyTimesRequest request = GreetManyTimesRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        asynClient.greetManyTimes(request, new StreamObserver<GreetManyTimesResponse> () {

           @Override
           public void onNext(GreetManyTimesResponse value) {
               System.out.println("Response async manyTime: " + value);
           }

           @Override
           public void onError(Throwable t) {

           }

           @Override
           public void onCompleted() {
               System.out.println("async onCompleted: ");
               latch.countDown();
           }
       });
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Client streaming
    private static void syncLongGreet(ManagedChannel channel) {
        //create async client
        GreetServiceGrpc.GreetServiceStub asynClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestObserver = asynClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                // we get response form server
                //onNext will be called only once
                System.out.println("Received response form the server: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                // get error form server
            }

            @Override
            public void onCompleted() {
                // server done sending us
                //onCompleted will be called right after onNext()
                System.out.println("Server has completed sending us something");
                latch.countDown();
            }
        });

        for (int i = 0; i < 5; i++) {
            Greeting greeting = Greeting.newBuilder()
                    .setFirstName("Paulo" + i)
                    .setLastName("Karachi")
                    .build();

            LongGreetRequest request = LongGreetRequest.newBuilder()
                    .setGreeting(greeting)
                    .build();
            requestObserver.onNext(request);
        }

        requestObserver.onCompleted();
        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    // BiDi streaming API
    private static void greetEveryOne(ManagedChannel channel) {
        //async client
        GreetServiceGrpc.GreetServiceStub asynClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);


       StreamObserver<GreetEveryOneRequest> reqeustObserver = asynClient.greetEveryOne(new StreamObserver<GreetEveryOneResponse>() {

           @Override
           public void onNext(GreetEveryOneResponse value) {
               System.out.println("Response form server: " + value.getResult());
           }

           @Override
           public void onError(Throwable t) {
                latch.countDown();
           }

           @Override
           public void onCompleted() {
               System.out.println("Server is done sending data");
               latch.countDown();
           }
       });

        Arrays.asList("Stephan", "Jhon", "Mark", "Patricia").forEach(name -> {
            System.out.println("Sending " + name);
            Greeting greeting = Greeting.newBuilder()
                    .setFirstName(name)
                    .setLastName("Forename")
                    .build();
            GreetEveryOneRequest request = GreetEveryOneRequest.newBuilder()
                    .setGreeting(greeting)
                    .build();
            reqeustObserver.onNext(request);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        reqeustObserver.onCompleted();
        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        System.out.println("Hello I am gRPC client");

        GreetingClient greet = new GreetingClient();
        greet.run();
    }
}
