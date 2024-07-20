package dev.serathiuk.javadht.chord;

import dev.serathiuk.javadht.chord.grpc.Node;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Key {

    public static int hash(String host, int port) {
        var key = host + ":" + port;
        return hash(key);
    }

    public static int hash(String key) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var hashBytes = md.digest(key.getBytes());
            var hash = new BigInteger(1, hashBytes);
            var maxIdentifier = BigInteger.valueOf(FingerTable.SIZE_OF_RING);
            return hash.mod(maxIdentifier).intValue() + 1; // Adiciona 1 para garantir que esteja no intervalo de 1 a 360
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isBetween(int n, int start, int end) {
        if(start > end) {
            return  isBetween(n, start, FingerTable.SIZE_OF_RING - 1) ||
                    isBetween(n, 0, end);
        }

        return n >= start && n < end;
    }

    public static boolean isBetween(Node n, Node start, Node end) {
        if(n == null || start == null || end == null) return false;
        return isBetween(n.getId(), start.getId(), end.getId());
    }

}
