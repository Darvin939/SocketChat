package ru.mashoshin.SocketChat.server;

import ru.mashoshin.SocketChat.Connection;
import ru.mashoshin.SocketChat.Console;
import ru.mashoshin.SocketChat.Message;
import ru.mashoshin.SocketChat.MessageType;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler extends Thread {

    private Socket socket;
    private Map<String, Connection> userConnectionMap = new ConcurrentHashMap<String, Connection>();

    ServerHandler(Socket serverSocket) {
        this.socket = serverSocket;
    }

    private void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> entry : userConnectionMap.entrySet()) {
            try {
                entry.getValue().send(message);
            } catch (IOException e) {
                Console.writeMessage("Не смогли отправить сообщение пользователю " + entry.getKey());
            }
        }
    }

    private String getHandshake(Connection connection) throws IOException, ClassNotFoundException {
        String user_name = "";
        while (true) {
            connection.send(new Message(MessageType.NAME_REQUEST));
            Message message = connection.receive();
            if (message.getType() == MessageType.USER_NAME) {
                user_name = message.getMessage();
                if (!user_name.isEmpty() && !userConnectionMap.containsKey(user_name)) {
                    userConnectionMap.put(user_name, connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    break;
                }
            }
        }
        return user_name;
    }

    private void sendListOfUsers(Connection connection, String userName) throws IOException {
        for (String key : userConnectionMap.keySet()) {
            if (!key.equals(userName)) {
                Message message = new Message(key, MessageType.USER_ADDED);
                connection.send(message);
            }
        }
    }

    private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
        while (true) {
            Message message = connection.receive();
            if (message.getType() == MessageType.TEXT) {
                if (message.getMessage().equals("exit"))
                    break;
                Message newMess = new Message(String.format("%s: %s", userName, message.getMessage()), MessageType.TEXT);
                sendBroadcastMessage(newMess);
            } else Console.writeMessage("Не верный тип сообщения!");
        }
    }

    @Override
    public void run() {
        try (Connection connection = new Connection(socket)) {
            String user_name = getHandshake(connection);
            sendBroadcastMessage(new Message(user_name, MessageType.USER_ADDED));
            sendListOfUsers(connection, user_name);
            serverMainLoop(connection, user_name);
            if (user_name != null && !user_name.isEmpty()) {
                userConnectionMap.remove(user_name);
                sendBroadcastMessage(new Message(user_name, MessageType.USER_REMOVED));
            }
            Console.writeMessage("Соединение с удаленным адресом " + socket.getRemoteSocketAddress() + " закрыто.");
        } catch (Exception e) {
            Console.writeMessage("Произошла ошибка при обмене данными с удаленным адресом " + socket.getRemoteSocketAddress());
        }
    }
}
