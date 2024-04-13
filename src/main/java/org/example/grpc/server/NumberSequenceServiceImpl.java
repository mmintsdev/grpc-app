package org.example.grpc.server;

import com.example.grpc.Number;
import com.example.grpc.NumberRange;
import com.example.grpc.NumberSequenceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

@GrpcService
public class NumberSequenceServiceImpl extends NumberSequenceGrpc.NumberSequenceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(NumberSequenceServiceImpl.class);

    @Override
    public void getNumberSequence(NumberRange request, StreamObserver<Number> responseObserver) {
        IntStream.rangeClosed(request.getFirstValue(), request.getLastValue()).forEach(value -> {
            try {
                Number number = Number.newBuilder().setValue(value).build();
                responseObserver.onNext(number);
                Thread.sleep(2000); // delay to simulate streaming
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted!", e);
                Thread.currentThread().interrupt();
            }
        });

        responseObserver.onCompleted();
    }
}