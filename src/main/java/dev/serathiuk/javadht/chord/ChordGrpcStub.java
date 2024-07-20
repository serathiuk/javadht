package dev.serathiuk.javadht.chord;

import dev.serathiuk.javadht.chord.grpc.ChordGrpc;
import dev.serathiuk.javadht.chord.grpc.Node;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

import java.util.concurrent.TimeUnit;

public class ChordGrpcStub implements AutoCloseable {

    private String address;
    private ManagedChannel channel;
    private ChordGrpc.ChordBlockingStub stub;

    public ChordGrpcStub(String host, int port) {
        this.address = host + ":" + port;
        init();
    }

    public ChordGrpcStub(Node node) {
        this.address = node.getHost() +":"+ node.getPort();
        init();
    }

    private void init() {
        if(stub != null) return;
        channel = Grpc.newChannelBuilder(address, InsecureChannelCredentials.create()).build();
        stub = ChordGrpc.newBlockingStub(channel);
    }

    public ChordGrpc.ChordBlockingStub getStub() {
        init();
        return stub;
    }

    @Override
    public void close() throws Exception {
        stub = null;

        if (channel != null) {
            channel.shutdown();
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                channel.shutdownNow();
            }
        }
    }
}
