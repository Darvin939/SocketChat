package ru.mashoshin.SocketChat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection {
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private final Socket socket;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    public Message recive() throws IOException, ClassNotFoundException {
        synchronized (inputStream) {
            return (Message) inputStream.readObject();
        }
    }

    public void send(Message message) throws IOException {
        synchronized (outputStream) {
            outputStream.writeObject(message);
        }
    }

    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
        socket.close();
    }
}
