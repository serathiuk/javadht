package dev.serathiuk.javadht.chord;

import dev.serathiuk.javadht.chord.grpc.Node;
import dev.serathiuk.javadht.chord.grpc.NodeData;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChordServer implements Runnable {
    private static final Logger logger = Logger.getLogger(ChordServer.class.getName());

    public static final String LOCALHOST = "127.0.0.1";

    private int nodePort;
    private int remotePort = -1;
    private Server server;
    private ChordGrpcImpl chordGrpc;
    private ScheduledExecutorService stabilizationExecutor;
    private ScheduledExecutorService fixFingerExecutor;

    public ChordServer(int nodePort) {
        this.nodePort = nodePort;
    }

    public ChordServer(int nodePort, int remotePort) {
        this.nodePort = nodePort;
        this.remotePort = remotePort;
    }

    @Override
    public void run() {
        try {
            chordGrpc = new ChordGrpcImpl();

            server = Grpc.newServerBuilderForPort(nodePort, InsecureServerCredentials.create())
                    .addService(chordGrpc)
                    .build()
                    .start();

            logger.info("Server started, listening on " + LOCALHOST+":"+nodePort);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.err.println();
                    logger.warning("*** shutting down gRPC server since JVM is shutting down");
                    try {
                        server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        logger.log(Level.SEVERE, "Error", e);
                    }
                    logger.warning("*** server shut down");
                    System.err.println();
                }
            });

            var node = Node.newBuilder()
                    .setPort(nodePort)
                    .setHost(LOCALHOST)
                    .setId(Key.hash(LOCALHOST, nodePort))
                    .build();

            chordGrpc.join(node, LOCALHOST, remotePort);

            stabilizationExecutor = Executors.newSingleThreadScheduledExecutor();
            stabilizationExecutor.scheduleWithFixedDelay(() -> {
                chordGrpc.stabilize();
                chordGrpc.fixFingers();
            }, 500, 1000, TimeUnit.MILLISECONDS);

            server.awaitTermination();
        } catch (InterruptedException | IOException e) {
            logger.log(Level.SEVERE, "Error", e);
        }
    }

    public boolean isStarted() {
        return chordGrpc.isStarted();
    }

    public int getNodePort() {
        return nodePort;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public NodeData getNodeData() {
        return chordGrpc.getNodeData();
    }

}
