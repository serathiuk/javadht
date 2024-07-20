package dev.serathiuk.javadht.chord;

import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ChordMain {

    private static ChordClient chordClient;

    private static final Logger logger = Logger.getLogger(ChordMain.class.getName());

    public static void main(String[] args) throws InterruptedException {
        var localhost = "127.0.0.1";
        System.out.println(Key.hash(localhost, 8001));
        System.out.println(Key.hash(localhost, 8002));
        System.out.println(Key.hash(localhost, 8003));
        System.out.println(Key.hash(localhost, 8004));
        System.out.println(Key.hash(localhost, 8005));
    }

}

