package dev.serathiuk.javadht.chord;

import dev.serathiuk.javadht.chord.grpc.Empty;
import dev.serathiuk.javadht.chord.grpc.NodeData;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ChordServerTest {

    private static final String LOCALHOST = "127.0.0.1";

    @Test
    public void testGetNodeData() throws Exception{
        var remoteServer = 8001;
        var id1 = 147;
        var id2 = 143;

        var executor = Executors.newFixedThreadPool(10);

        var chordServer1 = new ChordServer(remoteServer);
        var chordServer2 = new ChordServer(8002, remoteServer);


        executor.execute(chordServer1::run);
        await().atMost(5, TimeUnit.SECONDS).until(chordServer1::isStarted);

        executor.execute(chordServer2::run);
        await().atMost(5, TimeUnit.SECONDS).until(chordServer2::isStarted);


        // Test node id
        assertEquals(id1, getNodeData(chordServer1).getNode().getId());
        assertEquals(id2, getNodeData(chordServer2).getNode().getId());

        //Test Successors
        await().atMost(10, TimeUnit.SECONDS).until(() -> Integer.compare(getNodeData(chordServer1).getNodeSuccessor().getId(), id2) != 0);
        await().atMost(10, TimeUnit.SECONDS).until(() -> Integer.compare(getNodeData(chordServer2).getNodeSuccessor().getId(), id1) != 0);

        //Test Predecessors
        await().atMost(10, TimeUnit.SECONDS).until(() -> Integer.compare(getNodeData(chordServer1).getNodePredecessor().getId(), id2) != 0);
        await().atMost(10, TimeUnit.SECONDS).until(() -> Integer.compare(getNodeData(chordServer2).getNodePredecessor().getId(), id1) != 0);
    }

    private NodeData getNodeData(ChordServer chordServer) throws Exception{
        try (var stub = new ChordGrpcStub(chordServer.LOCALHOST, chordServer.getNodePort())) {
            return stub.getStub().getNodeData(Empty.getDefaultInstance());
        }
    }

    @Test
    public void testRingWith2Nodes() {
        var remoteServer = 8001;
        // 1 -> 2 -> 1 -> 4
        var id1 = 147;
        var id2 = 143;

        var executor = Executors.newFixedThreadPool(10);

        var chordServer1 = new ChordServer(remoteServer);
        var chordServer2 = new ChordServer(8002, remoteServer);


        executor.execute(chordServer1::run);
        await().atMost(5, TimeUnit.SECONDS).until(chordServer1::isStarted);

        executor.execute(chordServer2::run);
        await().atMost(5, TimeUnit.SECONDS).until(chordServer2::isStarted);

        // Test node id
        assertEquals(id1, chordServer1.getNodeData().getNode().getId());
        assertEquals(id2, chordServer2.getNodeData().getNode().getId());

        //Test Successors
        await().atMost(10, TimeUnit.SECONDS).until(() -> Integer.compare(chordServer1.getNodeData().getNodeSuccessor().getId(), id2) != 0);
        await().atMost(10, TimeUnit.SECONDS).until(() -> Integer.compare(chordServer2.getNodeData().getNodeSuccessor().getId(), id1) != 0);

        //Test Predecessors
        await().atMost(10, TimeUnit.SECONDS).until(() -> Integer.compare(chordServer1.getNodeData().getNodePredecessor().getId(), id2) != 0);
        await().atMost(10, TimeUnit.SECONDS).until(() -> Integer.compare(chordServer2.getNodeData().getNodePredecessor().getId(), id1) != 0);
    }

    @Test
    public void testRingWith5Nodes() {
        var remoteServer = 8001;
        // 4 -> 2 -> 1 -> 5 -> 3 -> 4
        var id1 = 147;
        var id2 = 143;
        var id3 = 258;
        var id4 = 119;
        var id5 = 200;

        var executor = Executors.newFixedThreadPool(10);

        var chordServer1 = new ChordServer(remoteServer);
        var chordServer2 = new ChordServer(8002, remoteServer);
        var chordServer3 = new ChordServer(8003, remoteServer);
        var chordServer4 = new ChordServer(8004, remoteServer);
        var chordServer5 = new ChordServer(8005, remoteServer);


        executor.execute(chordServer1::run);
        await().atMost(5, TimeUnit.SECONDS).until(chordServer1::isStarted);

        executor.execute(chordServer2::run);
        await().atMost(5, TimeUnit.SECONDS).until(() -> chordServer2.isStarted());

        executor.execute(chordServer3::run);
        await().atMost(5, TimeUnit.SECONDS).until(() -> chordServer3.isStarted());

        executor.execute(chordServer4::run);
        await().atMost(5, TimeUnit.SECONDS).until(() -> chordServer4.isStarted());

        executor.execute(chordServer5::run);
        await().atMost(5, TimeUnit.SECONDS).until(() -> chordServer5.isStarted());

        // Test node id
        assertEquals(id1, chordServer1.getNodeData().getNode().getId());
        assertEquals(id2, chordServer2.getNodeData().getNode().getId());
        assertEquals(id3, chordServer3.getNodeData().getNode().getId());
        assertEquals(id4, chordServer4.getNodeData().getNode().getId());
        assertEquals(id5, chordServer5.getNodeData().getNode().getId());

        //Test Successors
        await().atMost(10, TimeUnit.SECONDS).until(() -> Integer.compare(chordServer1.getNodeData().getNodeSuccessor().getId(), id5) != 0);
        await().atMost(10, TimeUnit.SECONDS).until(() -> Integer.compare(chordServer2.getNodeData().getNodeSuccessor().getId(), id1) != 0);
        await().atMost(10, TimeUnit.SECONDS).until(() -> Integer.compare(chordServer3.getNodeData().getNodeSuccessor().getId(), id4) != 0);
        await().atMost(10, TimeUnit.SECONDS).until(() -> Integer.compare(chordServer4.getNodeData().getNodeSuccessor().getId(), id2) != 0);
        await().atMost(10, TimeUnit.SECONDS).until(() -> Integer.compare(chordServer5.getNodeData().getNodeSuccessor().getId(), id3) != 0);
    }

}

