package ru.q1w2e3.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainApp {
    public static void main(String[] args) {
       new Server(8189);
    }
}
