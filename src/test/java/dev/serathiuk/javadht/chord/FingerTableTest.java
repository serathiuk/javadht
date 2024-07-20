package dev.serathiuk.javadht.chord;

import dev.serathiuk.javadht.chord.grpc.Node;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FingerTableTest {

    private FingerTable fingerTable;

    @BeforeEach
    public void beforeEach() {
        fingerTable = new FingerTable(createNode(0));
    }

    @Test
    public void testAddFirstItem() {
        var item = new FingerTableItem();
        item.setNode(createNode(1));
        item.setNodeSuccessor(createNode(2));
        fingerTable.save(item);

        assertEquals(1, fingerTable.getFingerTableSize());

        Assertions.assertThat(fingerTable.getFingerTableItems())
                .usingRecursiveComparison()
                .isEqualTo(Set.of(item));
    }

    @Test
    public void testAddAndReplaceItem() {
        var item = new FingerTableItem();
        item.setNode(createNode(1));
        item.setNodeSuccessor(createNode(2));
        fingerTable.save(item);

        item = new FingerTableItem();
        item.setNode(createNode(1));
        item.setNodeSuccessor(createNode(3));
        fingerTable.save(item);

        assertEquals(1, fingerTable.getFingerTableSize());

        Assertions.assertThat(fingerTable.getFingerTableItems())
                .usingRecursiveComparison()
                .isEqualTo(Collections.unmodifiableSet(Set.of(item)));
    }

    @Test
    public void testRemoveItem() {
        var item = new FingerTableItem();
        item.setNode(createNode(1));
        item.setNodeSuccessor(createNode(2));
        fingerTable.save(item);

        fingerTable.remove(2);
        assertEquals(1, fingerTable.getFingerTableSize());

        fingerTable.remove(1);
        assertEquals(0, fingerTable.getFingerTableSize());
    }

    @Test
    public void testSuccessor() {
        var item1 = new FingerTableItem();
        item1.setNode(createNode(3));
        fingerTable.save(item1);

        Assertions.assertThat(fingerTable.getSuccessor().get())
                .usingRecursiveComparison()
                .isEqualTo(item1);

        var item2 = new FingerTableItem();
        item2.setNode(createNode(5));
        fingerTable.save(item2);

        Assertions.assertThat(fingerTable.getSuccessor().get())
                .usingRecursiveComparison()
                .isEqualTo(item1);

        var item3 = new FingerTableItem();
        item3.setNode(createNode(1));
        fingerTable.save(item3);

        Assertions.assertThat(fingerTable.getSuccessor().get())
                .usingRecursiveComparison()
                .isEqualTo(item3);
    }

    private Node createNode(int id) {
        return Node.newBuilder()
                .setId(id)
                .setHost("127.0.0.1")
                .setPort(8000+id)
                .build();
    }

}