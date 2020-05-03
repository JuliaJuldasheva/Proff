package ru.q1w2e3.chat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {

    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;

    public Network(int port) throws IOException {
        clientSocket = new Socket("localhost", port);
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());
    }

    public boolean isConnected() {
        if (clientSocket == null) {
            return false;
        }
        if (clientSocket.isClosed()) {
            return false;
        }
        return true;
    }

    public void sendMsg (String msg) throws IOException {
        out.writeUTF(msg);
    }

    public String readMsg() throws IOException {
        return in.readUTF();
    }
    public void close() {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}

