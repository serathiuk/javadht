package dev.serathiuk.javadht.chord;

import dev.serathiuk.javadht.chord.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class ChordGrpcImpl extends ChordGrpc.ChordImplBase {

    private static final Logger logger = Logger.getLogger(ChordServer.class.getName());

    private FingerTable fingerTable;
    private final Map<String, String> nodeData = new HashMap<>();
    private Node successor;
    private Node predecessor;
    private Node node;
    private boolean isStarted = false;

    public void join(Node newNode, String joinHost, int joinPort) {
        fingerTable = new FingerTable(newNode);

        if(joinPort > 0) {
            searchSuccessor(newNode, joinHost, joinPort);
        }

        node = newNode;
        isStarted = true;

        notify();
    }

    private void searchSuccessor(Node newNode, String joinHost, int joinPort) {
        logger.info("Searching successor for "+newNode.getId()+" in " + joinHost + ":" + joinPort);
        try (var stub = new ChordGrpcStub(joinHost, joinPort)) {
            var successorData = stub.getStub().findSuccessor(newNode);
            if (successorData != null && successorData.getNode().getPort() > 0) {
                logger.info("Successor found: "+successorData.getNode().getId());

                successor = successorData.getNode();
                fingerTable.save(FingerTableItem.fromNodeData(successorData));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stabilize() {
        logger.info(node.getId()+" - Start stabilization. ");
        if(successor == null) return;

        logger.info(node.getId()+" - Stabilization successor. "+successor.getId());
        try (var stub = new ChordGrpcStub(successor.getHost(), successor.getPort())) {
            var successorData = stub.getStub().getNodeData(Empty.newBuilder().build());
            logger.info(node.getId() + " - Sucessor data: " + getNodeInformation(successorData));
            var predecessor = successorData.getNodePredecessor();
            if(Key.isBetween(predecessor, node, successorData.getNode())) {
                logger.info(node.getId()+" - Sucessor is between.");
                successor = predecessor;

                var predecessorFT = new FingerTableItem();
                predecessorFT.setNodePredecessor(node);
                predecessorFT.setNode(predecessor);
                predecessorFT.setNodeSuccessor(successorData.getNode());
                fingerTable.save(predecessorFT);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (var stub = new ChordGrpcStub(successor.getHost(), successor.getPort())) {
            logger.info(node.getId()+" - Notify "+ successor.getId());
            stub.getStub().notify(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getNodeInformation(NodeData successorData) {
        return successorData.getNodePredecessor().getId() + "> " + successorData.getNode().getId() + " -> " + successorData.getNodeSuccessor().getId();
    }

    public void fixFingers() {
        logger.info(node.getId()+" - Start fix fingers. "+node.getId());
        var fingerTableItems = new ArrayList<>(fingerTable.getFingerTableItems());
        if(fingerTableItems.isEmpty()) {
            logger.warning(node.getId()+" - Finger table empty");
            return;
        }

        var random = new Random();
        int item = random.nextInt(fingerTableItems.size() - 1);
        var fingerTableItem = fingerTableItems.get(item);
        var node = fingerTableItem.getNode();

        var newNode = findSuccessorIntern(node.getId());
        if(newNode != null) {
            logger.info(node.getId()+" - Saving new node:"+getNodeInformation(newNode));
            fingerTable.save(FingerTableItem.fromNodeData(newNode));
        }
            
    }

    @Override
    public synchronized void findSuccessor(Node nodeRequest, StreamObserver<NodeData> responseObserver) {
        logger.info(node.getId()+" - Finding successor for "+ nodeRequest.getId());
        responseObserver.onNext(findSuccessorIntern(nodeRequest.getId()));
        responseObserver.onCompleted();
    }

    private NodeData findSuccessorIntern(int nodeId) {
        if(successor != null && Key.isBetween(nodeId, node.getId(), successor.getId())) {
            logger.info(node.getId()+" - Searching Node Data");
            try (var stub = new ChordGrpcStub(successor.getHost(), successor.getPort())) {
                return stub.getStub().getNodeData(Empty.newBuilder().build());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        logger.info(node.getId()+" - Searching closest preceding");
        var closest = closestPrecedingFinger(node);
        if(closest.getId() == node.getId()) {
            logger.info(node.getId()+" - IT's the same :)");
            return getNodeData();
        }

        try (var stub = new ChordGrpcStub(closest.getHost(), closest.getPort())) {
            logger.info(node.getId()+" - Find the successor of the closest.");
            return stub.getStub().findSuccessor(closest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public NodeData getNodeData() {
        var builder = NodeData.newBuilder().setNode(node);

        if(successor != null) {
            builder.setNodeSuccessor(successor);
        }

        if(predecessor != null) {
           builder.setNodePredecessor(predecessor);
        }

        return builder.build();
    }

    @Override
    public void getNodeData(Empty request, StreamObserver<NodeData> responseObserver) {
        responseObserver.onNext(getNodeData());
        responseObserver.onCompleted();
    }

    public Node closestPrecedingFinger(Node nodeRequest) {
        var fingerTableItems = fingerTable.getFingerTableItems()
                .stream()
                .sorted((o1, o2) -> Integer.compare(o2.getNode().getId(), o1.getNode().getId()))
                .toList();

        for(var item : fingerTableItems) {
            if(Key.isBetween(item.getNode().getId(), node.getId(), nodeRequest.getId())) {
                return item.getNode();
            }
        }

        return node;
    }

    @Override
    public synchronized void notify(Node nodeRequest, StreamObserver<Empty> responseObserver) {
        if(predecessor == null || Key.isBetween(nodeRequest.getId(), predecessor.getId(), node.getId())) {
            predecessor = nodeRequest;
        }

        if(successor == null || Key.isBetween(nodeRequest.getId(), node.getId(), successor.getId())) {
            successor = nodeRequest;
        }

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
        var hashKey = Key.hash(request.getKey());

        if(Key.isBetween(hashKey, node.getId(), successor.getId())) {
            responseObserver.onNext(GetResponse.newBuilder()
                    .setKey(request.getKey())
                    .setValue(nodeData.get(request.getKey()))
                    .setNodeId(node.getId())
                    .build());
            responseObserver.onCompleted();
            return;
        }

        var node = findSuccessorIntern(hashKey);
        if(node == null) {
            responseObserver.onError(new IllegalStateException("Node found a node."));
            responseObserver.onCompleted();
            return;
        }

        try (var stub = new ChordGrpcStub(node.getNode().getHost(), node.getNode().getPort())) {
            responseObserver.onNext(stub.getStub().get(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void put(Entry request, StreamObserver<PutResponse> responseObserver) {
        var hashKey = Key.hash(request.getKey());

        if(Key.isBetween(hashKey, node.getId(), successor.getId())) {
            nodeData.put(request.getKey(), request.getValue());

            responseObserver.onNext(PutResponse.newBuilder()
                    .setNodeId(node.getId())
                    .build());
            responseObserver.onCompleted();
            return;
        }

        var node = findSuccessorIntern(hashKey);
        if(node == null) {
            responseObserver.onError(new IllegalStateException("Node found a node."));
            responseObserver.onCompleted();
            return;
        }

        try (var stub = new ChordGrpcStub(node.getNode().getHost(), node.getNode().getPort())) {
            responseObserver.onNext(stub.getStub().put(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isStarted() {
        return isStarted;
    }
    public NodeData getSuccessor() {
        return fingerTable.getSuccessor()
                .map(FingerTableItem::toNodeData)
                .orElse(null);
    }

    public Node getNode() {
        return node;
    }

}
