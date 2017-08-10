package ru.mashoshin.SocketChat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerRunner {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(6000);

            while (true) {
                Socket socket = serverSocket.accept();
                new ServerHandler(socket).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
