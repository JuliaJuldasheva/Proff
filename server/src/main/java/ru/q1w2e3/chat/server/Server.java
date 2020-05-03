package ru.q1w2e3.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private AuthManager authManager;
    private List<ClientHandler> clients;
    private final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuthManager getAuthManager() {
        return authManager;
    }

    public Server(int port)  {
        clients = new ArrayList<>();
        authManager = new DbAuthManager();
        authManager.start();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен. Ожидаем подключения клиентов...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            authManager.stop();
        }
    }

    public void broadCastMsg(String msg, boolean withDateTime) {
        if(withDateTime) {
            msg = String.format("[%s] %s", LocalDateTime.now().format(DTF), msg);
        }
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }

    public boolean isNickBusy(String nickname) {
        for (ClientHandler o : clients) {
            if(o.getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public void privateMsg(ClientHandler sender, String receiver, String msg) {
        if(sender.getNickname().equals(receiver)) {
            sender.sendMsg("Нельзя отправить личное сообщение самому себе");
        }
        for (ClientHandler o : clients) {
            if(o.getNickname().equals(receiver)) {
                o.sendMsg("from " + sender.getNickname() +" : " + msg);
                sender.sendMsg("to " + receiver + ": " + msg);
                return;
            }
        }
        sender.sendMsg(receiver + " не в сети");
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder("/clients_list ");
        for (ClientHandler o : clients) {
            sb.append(o.getNickname()).append(" ");
        }
        sb.setLength(sb.length() - 1);
        String out = sb.toString();
        broadCastMsg(out, false);
    }

    public synchronized void subscribe (ClientHandler clientHandler) {
        broadCastMsg(clientHandler.getNickname() + " вошел в чат", false);
        clients.add(clientHandler);
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadCastMsg(clientHandler.getNickname() + " покинул чат", false);
        broadcastClientsList();
    }
}
