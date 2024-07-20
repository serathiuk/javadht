package dev.serathiuk.javadht.chord;

import dev.serathiuk.javadht.chord.grpc.Node;
import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;

public class ChordClient {

    private String host;
    private int port;

    public ChordClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void init() {
        System.out.printf("Initializing Chord Client %s:%d", host, port);

        System.out.println("------------------------------------");



        var sc = new Scanner(System.in);
        String opt = null;
        while((opt = sc.next()) != null) {
            System.out.println("1 -> Get Key / 2 - Put Key");

            switch (opt) {
                case "1" -> get(sc);
                case "2" -> put(sc);
                default -> System.out.println("Invalid option");
            }
        }
    }

    private void get(Scanner sc) {
        System.out.println("Key: ");
        String key = sc.next();
        if (StringUtils.isEmpty(key)) {
            System.out.println("Invalid key");
            return;
        }


    }

    private void put(Scanner sc) {

    }

}
