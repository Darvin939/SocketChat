package ru.mashoshin.SocketChat.client;

import ru.mashoshin.SocketChat.Connection;
import ru.mashoshin.SocketChat.Console;
import ru.mashoshin.SocketChat.Message;
import ru.mashoshin.SocketChat.MessageType;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Connection connection;
    private volatile boolean clientConnected = false;

    ClientHandler() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            Console.writeMessage("Wait error");
            return;
        }

        if (clientConnected)
            Console.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        else Console.writeMessage("Произошла ошибка во время работы клиента.");

        while (clientConnected) {
            String line = Console.readString();

            if (shouldSendTextFromConsole())
                sendTextMessage(line);

            if (line.equals("exit"))
                break;
        }
    }

    private String getServerAddress() {
        return Console.readString();
    }

    private int getServerPort() {
        return Console.readInt();
    }

    private String getUserName() {
        return Console.readString();
    }

    private boolean shouldSendTextFromConsole() {
        return true;
    }

    private SocketThread getSocketThread() {
        return new SocketThread();
    }

    private void sendTextMessage(String text) {
        try {
            connection.send(new Message(text, MessageType.TEXT));
        } catch (IOException e) {
            e.printStackTrace();
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread {
        void processIncomingMessage(String message) {
            Console.writeMessage(message);
        }

        void informAboutAddingNewUser(String userName) {
            Console.writeMessage("Пользователь " + userName + " подключился к чату");
        }

        void informAboutDeletingNewUser(String userName) {
            Console.writeMessage("Пользователь " + userName + " покинул чат");
        }

        void notifyConnectionStatusChanged(boolean clientConnected) {
            ClientHandler.this.clientConnected = clientConnected;
            synchronized (ClientHandler.this) {
                ClientHandler.this.notify();
            }
        }

        void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(getUserName(), MessageType.USER_NAME));
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else
                    throw new IOException("Unexpected MessageType");
            }
        }


        void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getMessage());
                } else if (message.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getMessage());
                } else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getMessage());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(getServerAddress(), getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (Exception e) {
                notifyConnectionStatusChanged(false);
                e.printStackTrace();
            }
        }
    }
}
