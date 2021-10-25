package ru.nsu.nikolotov.server;

import lombok.extern.java.Log;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log
public class Server {
    private static final String DEFAULT_DIR = "./uploads/";
    private static final int DEFAULT_PORT = 8080;

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        int port = 0;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-p" -> port = Integer.parseInt(args[i + 1]);
                case "-h" -> printHelp();
            }
        }

        if ((port <= 0) || (port >= 65536)) {
            log.info("No port was found or port was invalid, starting on default port");
            port = DEFAULT_PORT;
        }


        try (ServerSocket serverSocket = new ServerSocket(port)) {

            log.info("Server was started on " + port + " port\n" +
                    "Directory for files: " + DEFAULT_DIR);

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new FileReceiver(clientSocket, DEFAULT_DIR));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printHelp() {
        log.info("Use flag -p for port, -h for help" +
                "\n -p is an optional flag, you may not use it and start with default port + " + DEFAULT_PORT);
    }
}

