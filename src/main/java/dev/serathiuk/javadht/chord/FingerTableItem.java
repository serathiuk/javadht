package dev.serathiuk.javadht.chord;

import dev.serathiuk.javadht.chord.grpc.Node;
import dev.serathiuk.javadht.chord.grpc.NodeData;

import java.util.Objects;

public class FingerTableItem {

    private Node node;
    private Node nodeSuccessor;
    private Node nodePredecessor;

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public void setNodeSuccessor(Node nodeSuccessor) {
        this.nodeSuccessor = nodeSuccessor;
    }

    public Node getNodeSuccessor() {
        return nodeSuccessor;
    }

    public Node getNodePredecessor() {
        return nodePredecessor;
    }

    public void setNodePredecessor(Node nodePredecessor) {
        this.nodePredecessor = nodePredecessor;
    }

    public NodeData toNodeData() {
        return NodeData.newBuilder()
            .setNode(node)
            .setNodeSuccessor(nodeSuccessor)
            .setNodePredecessor(nodePredecessor)
            .build();
    }

    public static FingerTableItem fromNodeData(NodeData data) {
        FingerTableItem item = new FingerTableItem();
        item.setNode(data.getNode());
        item.setNodeSuccessor(data.getNodeSuccessor());
        item.setNodePredecessor(data.getNodePredecessor());
        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FingerTableItem that = (FingerTableItem) o;
        return Objects.equals(node.getId(), that.node.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(node.getId());
    }
}
