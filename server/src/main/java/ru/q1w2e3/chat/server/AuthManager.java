package ru.q1w2e3.chat.server;

public interface AuthManager {
    String getNicknameByLoginPassword(String login, String password);
    boolean changeNickname(String oldNickname, String newNickname);
    void start();
    void stop();
}
