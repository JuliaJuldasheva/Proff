package ru.q1w2e3.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket =socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                while (true) { //цикл аутентификации
                    String msg = in.readUTF();
                    System.out.print("Клиент: " + msg + "\n");
                    if (msg.startsWith("/auth ")) {
                        String[] tokens = msg.split(" ", 3);
                        String nickFromAuthManager = server.getAuthManager().getNicknameByLoginPassword(tokens[1], tokens[2]);
                        if (nickFromAuthManager != null) {
                            if (server.isNickBusy(nickFromAuthManager)) {
                                sendMsg("Такой пользователь уже \n зарегистрирован в чате");
                                continue;
                            }
                            nickname = nickFromAuthManager;
                            sendMsg("/auth_ok " + nickname);
                            server.subscribe(this);
                            break;
                        } else {
                            sendMsg("Неверный логин/пароль");
                        }
                    }
                }
                while (true) { //цикл общения с сервером
                    String msg = in.readUTF();
                    System.out.print("Клиент: " + msg + "\n");
                    if (msg.startsWith("/")) {

                        if (msg.equals("/end")) {
                            sendMsg("/end_confirm");
                            break;
                        }

                        if (msg.startsWith("/changenick ")) { // /changenick newNickname
                            String[] tokens = msg.split(" ", 2);
                            String newNickname = tokens[1];
                            if(server.getAuthManager().changeNickname(nickname, newNickname)) {
                                nickname = newNickname;
                                sendMsg("/set_nick_to " + newNickname);
                            } else {
                                sendMsg("Сервер: не удалось сменить ник, ник уже занят");
                            }
                            continue;
                        }

                        if (msg.startsWith("/w ")) { // /w user1 hello
                            String[] tokens = msg.split(" ", 3);
                            server.privateMsg(this, tokens[1], tokens[2]);
                            continue;
                        }
                    } else {
                        server.broadCastMsg(nickname + ": " + msg, true);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }).start();
    }

    public void sendMsg (String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        server.unsubscribe(this);
        nickname = null;
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
