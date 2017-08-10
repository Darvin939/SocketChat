package ru.mashoshin.SocketChat.server;

import ru.mashoshin.SocketChat.Connection;

import java.io.IOException;
import java.net.Socket;

public class ServerHandler extends Thread {

    private Socket socket;

    public ServerHandler(Socket serverSocket) {
        this.socket = serverSocket;
    }

    @Override
    public void run() {
        try {
            Connection connection = new Connection(socket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
