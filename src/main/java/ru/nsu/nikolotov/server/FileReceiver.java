package ru.nsu.nikolotov.server;

import ru.nsu.nikolotov.shared.ResultOfSending;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.extern.java.Log;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log
public class FileReceiver implements Runnable {
    private static final int BUF_SIZE = 4096;
    private final Socket clientSocket;
    private final String downloadDirectory;
    private Path filePath;
    private long totalBytesUploaded = 0;
    private long instantBytesUploaded = 0;
    private long fileLengthInBytes;
    private int speedometerCalls = 0;
    private final DataInputStream dataInputStream;

    public FileReceiver(Socket clientSocket, String downloadDirectory) throws IOException {
        this.clientSocket = clientSocket;
        log.info("New client " + clientSocket.getInetAddress().getHostAddress());
        this.dataInputStream = new DataInputStream(clientSocket.getInputStream());
        this.downloadDirectory = downloadDirectory;
    }

    @Override
    public void run() {
        var scheduledExecutorService = Executors.newScheduledThreadPool(1);
        try {
            receiveHeader();

            scheduledExecutorService.scheduleAtFixedRate(this::speedometer, 2, 3, TimeUnit.SECONDS);
            receiveFile();

            ResultOfSending res = checkResult();
            log.info("Client: " + clientSocket.getInetAddress().getHostAddress() +
                    "; Result of transferring " + res.toString());

            sendResponse(res);
            scheduledExecutorService.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            scheduledExecutorService.shutdown();
        }

    }

    private void speedometer() {
        speedometerCalls++;
        log.info("Client: " + clientSocket.getInetAddress().getHostAddress()
                + "; Avg. speed = " + totalBytesUploaded / 1024
                / 3 / speedometerCalls + " KB/s");
        log.info("Client: " + clientSocket.getInetAddress().getHostAddress()
                + "; Inst. speed from  = " + instantBytesUploaded / 1024 / 3 + " KB/s");
        instantBytesUploaded = 0;
    }

    private void receiveFile() {
        byte[] buf = new byte[BUF_SIZE];
        try {
            fileLengthInBytes = dataInputStream.readLong();
            OutputStream os = Files.newOutputStream(filePath);

            int bytesReceived;
            while ((bytesReceived = dataInputStream.read(buf, 0, BUF_SIZE)) > 0) {
                os.write(buf, 0, bytesReceived);
                totalBytesUploaded += bytesReceived;
                instantBytesUploaded += bytesReceived;
            }

            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.warning("Client: " + clientSocket.getInetAddress().getHostAddress() +
                    "; Error has occurred while receiving file: " + e.getMessage());
        }

    }

    private void receiveHeader() {
        byte[] filenameBuf;
        int filenameLength;
        try {
            filenameLength = dataInputStream.readInt();
            log.info("Client: " + clientSocket.getInetAddress().getHostAddress() +
                    "; Received length of file name " + filenameLength);

            filenameBuf = new byte[filenameLength];
            dataInputStream.read(filenameBuf, 0, filenameLength);
            String receivedFilename = Path.of(new String(filenameBuf)).getFileName().toString();

            String filenameOnServer = generateNewFilename(receivedFilename);
            log.info("Client: " + clientSocket.getInetAddress().getHostAddress() +
                    "; Received length of file name " + filenameLength);

            filePath = Path.of(filenameOnServer);
        } catch (IOException e) {
            e.printStackTrace();
            log.warning("Client: " + clientSocket.getInetAddress().getHostAddress() +
                    "Error has occurred while receiving header: " + e.getMessage());
        }
    }

    private void sendResponse(ResultOfSending status) {
        try {
            DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());
            os.writeInt(status.getCode());
        } catch (IOException e) {
            e.printStackTrace();
            log.warning("Client: " + clientSocket.getInetAddress().getHostAddress() +
                    "; Error has occurred while sending response: " + e.getMessage());
        }
    }

    private ResultOfSending checkResult() {
        if (totalBytesUploaded == fileLengthInBytes) {
            return ResultOfSending.SUCCESSFUL;
        } else if (totalBytesUploaded < fileLengthInBytes) {
            return ResultOfSending.NOT_FINISHED;
        } else {
            return ResultOfSending.ERROR;
        }
    }

    private String generateNewFilename(String filename) {
        return downloadDirectory + UUID.randomUUID().toString() + filename;
    }
}
