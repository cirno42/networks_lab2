package ru.nsu.nikolotov.client;

import lombok.extern.java.Log;
import ru.nsu.nikolotov.shared.ResultOfSending;
import java.io.IOException;
import java.net.Socket;


@Log
public class Client {

    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_ADDRESS = "127.0.0.1";

    public static void main(String[] args) {
        String filename = null, address = null;
        int port = 0;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-p" -> port = Integer.parseInt(args[i + 1]);
                case "-f" -> filename = args[i + 1];
                case "-a" -> address = args[i + 1];
                case "-h" -> printHelp();
            }
        }

        if (filename == null) {
            System.out.println("No file name was found");
            printHelp();
            return;
        }
        if (address == null) {
            System.out.println("No address was found, starting with default address");
            address = DEFAULT_ADDRESS;
        }
        if ((port == 0) || (port < 0) || (port > 65536)) {
            System.out.println("No port was found or port was invalid, starting with default port");
            port = DEFAULT_PORT;
        }

        try (var clientSocket = new Socket(address, port)) {
            ResultOfSending response = FileSender.send(clientSocket, filename);
            switch (response) {
                case ERROR -> System.out.println("Unknown error has occurred");
                case NOT_FINISHED -> System.out.println("Downloading wasn't finished");
                case SUCCESSFUL -> System.out.println("File downloaded successfully");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static void printHelp() {
        System.out.println("Use flags -f for file name, -p for port, -a for address. -h for help" +
                "\n -p and -a are optional flags, you may not use it and start with default port " + DEFAULT_PORT +
                " and default address " + DEFAULT_ADDRESS);
    }
}
