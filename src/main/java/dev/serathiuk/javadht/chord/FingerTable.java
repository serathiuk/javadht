package dev.serathiuk.javadht.chord;

import dev.serathiuk.javadht.chord.grpc.Node;

import java.util.*;

public class FingerTable {
    public static final int SIZE_OF_RING = 360;
    public static final int FINGER_TABLE_MAX_SIZE = 9; // it's the "m" on the paper, and the number of bits in SIZE_OF_RING :)

    private TreeSet<FingerTableItem> fingerTableItems;
    private Node node;

    public FingerTable(Node node) {
        fingerTableItems = new TreeSet<>(this::compare);
        this.node = node;
    }

    private int compare(FingerTableItem o1, FingerTableItem o2) {
        var id1 = o1.getNode().getId();
        if(id1 < node.getId()) {
            id1 += SIZE_OF_RING;
        }

        var id2 = o2.getNode().getId();
        if(id2 < node.getId()) {
            id1 += SIZE_OF_RING;
        }

        return Integer.compare(id1, id2);
    }

    public Optional<FingerTableItem> getSuccessor() {
        return fingerTableItems.stream().findFirst();
    }

    public void save(FingerTableItem item) {
        remove(item.getNode().getId());
        fingerTableItems.add(item);
    }

    public void remove(int id) {
        fingerTableItems.stream()
            .filter(f -> f.getNode().getId() == id)
            .findFirst()
            .ifPresent(f -> fingerTableItems.remove(f));
    }

    public int getFingerTableSize() {
        return fingerTableItems.size();
    }

    public Collection<FingerTableItem> getFingerTableItems() {
        return Collections.unmodifiableSet(fingerTableItems);
    }

}
