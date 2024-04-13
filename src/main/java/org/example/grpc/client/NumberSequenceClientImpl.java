package org.example.grpc.client;

import com.example.grpc.NumberRange;
import com.example.grpc.NumberSequenceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class NumberSequenceClientImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(NumberSequenceClientImpl.class);
    private final NumberSequenceGrpc.NumberSequenceBlockingStub blockingStub;

    public NumberSequenceClientImpl(ManagedChannel channel) {
        blockingStub = NumberSequenceGrpc.newBlockingStub(channel);
    }

    public void getNumbers(int firstValue, int lastValue) {
        NumberRange request = NumberRange.newBuilder().setFirstValue(firstValue).setLastValue(lastValue).build();
        AtomicInteger lastNumber = new AtomicInteger(0);

        blockingStub.getNumberSequence(request).forEachRemaining(number -> {
            LOGGER.info("new value:" + number.getValue());
            lastNumber.set(number.getValue());
        });

        new Thread(() -> {
            int currentValue = 0;
            for (int i = 0; i <= 50; i++) {
                currentValue += lastNumber.getAndSet(0) + 1;
                LOGGER.info("currentValue:" + currentValue);

                try {
                    Thread.sleep(1000); // delay to simulate streaming
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted!", e);
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public static void main(String[] args) throws InterruptedException {
        String target = "localhost:50051";
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        try {
            NumberSequenceClientImpl client = new NumberSequenceClientImpl(channel);
            client.getNumbers(0, 30);
        } finally {
            channel.shutdownNow().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        }
    }
}