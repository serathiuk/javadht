package dev.serathiuk.javadht.chord;

import dev.serathiuk.javadht.chord.grpc.Node;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static  org.awaitility.Awaitility.*;

class ChordGrpcImplTest {

    private static final String LOCALHOST = "127.0.0.1";

    @Test
    public void testJoinBootstrapServer() {
        var server = new ChordGrpcImpl();

        var node = Node.newBuilder()
                .setId(1)
                .setHost(LOCALHOST)
                .setPort(8001)
                .build();

        server.join(node, LOCALHOST, -1);

        assertTrue(server.isStarted());

        assertNull(server.getSuccessor());

        assertEquals(node, server.getNode());
    }

    @Test
    public void testJoinSecondServer() {
        var remoteServer = 8001;
        var localServer = 8002;

        var chordServer = new ChordServer(remoteServer);

        var thread = new Thread(chordServer);
        thread.start();

        await().atMost(5, TimeUnit.SECONDS).until(() -> chordServer.isStarted());

        var server = new ChordGrpcImpl();

        var node = Node.newBuilder()
                .setHost(LOCALHOST)
                .setPort(localServer)
                .setId(Key.hash(LOCALHOST, localServer))
                .build();

        server.join(node, LOCALHOST, remoteServer);

        assertTrue(server.isStarted());
        assertEquals(remoteServer, server.getSuccessor().getNode().getPort());
    }

}