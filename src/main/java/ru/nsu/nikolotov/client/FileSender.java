package ru.nsu.nikolotov.client;

import ru.nsu.nikolotov.shared.ResultOfSending;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSender {
    private static final int BUF_SIZE = 4096;

    public static ResultOfSending send(Socket clientSocket, String filePath) throws IOException {
        OutputStream os = clientSocket.getOutputStream();
        InputStream is = clientSocket.getInputStream();

        Path path = Paths.get(filePath);

        sendHeader(os, path);
        sendData(os, path);
        clientSocket.shutdownOutput();

        return receiveResponse(is);
    }

    private static void sendHeader(OutputStream os, Path path) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(os);

        dataOutputStream.writeInt(path.getFileName().toString().length());
        dataOutputStream.write(path.getFileName().toString().getBytes(StandardCharsets.UTF_8));
        dataOutputStream.writeLong(Files.size(path));
    }

    private static void sendData(OutputStream os, Path path) throws IOException {
        BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(path));
        byte[] buf = new byte[BUF_SIZE];
        int bytesCount;
        while ((bytesCount = inputStream.read(buf, 0, BUF_SIZE)) != -1) {
            os.write(buf, 0, bytesCount);
        }
        inputStream.close();
    }

    private static ResultOfSending receiveResponse(InputStream is) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(is);
        return ResultOfSending.getByCode(dataInputStream.readInt());
    }
}
