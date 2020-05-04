package ru.q1w2e3.chat.client;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    TextArea mainArea;

    @FXML
    TextField msgField, loginField;

    @FXML
    PasswordField passwordField;

    @FXML
    HBox loginBox;

    @FXML
    ListView<String> clientsList;

    private Network network;
    private boolean authenticated;
    private String nickname;
    private String login;
    private HistoryManager history;


    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        loginBox.setVisible(!authenticated);
        loginBox.setManaged(!authenticated);
        msgField.setVisible(authenticated);
        msgField.setManaged(authenticated);
        clientsList.setVisible(authenticated);
        clientsList.setManaged(authenticated);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthenticated(false);
        clientsList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() == 2) {
                    msgField.setText("/w " + clientsList.getSelectionModel() + " ");
                    msgField.requestFocus();
                    msgField.selectEnd();
                }
            }
        });
    }

    public void tryToConnect() {
        try {
            if (network !=null && network.isConnected()) {
                return;
            }
            setAuthenticated(false);
            network = new Network(8189);
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String msg = network.readMsg();
                        if (msg.startsWith("/auth_ok ")) {
                            nickname = msg.split(" ")[1];
                            login = msg.split(" ")[2];
                            mainArea.appendText("Вы зашли в чат под ником: " + nickname + "\n");
                            history = new HistoryManager(login);
                            mainArea.appendText(history.readLastLines());
                            setAuthenticated(true);
                            break;
                        }
                        mainArea.appendText(msg + "\n");
                        history.writeHistory(msg);
                    }
                    while (true) {
                        String msg = network.readMsg();
                        if (msg.startsWith("/")) {
                            if (msg.equals("/end_confirm")) {
                                mainArea.appendText("Вы покинули чат\n");
                                break;
                            }
                            if (msg.startsWith("/set_nick_to ")) {
                                nickname = msg.split(" ")[1];
                                mainArea.appendText("Ваш новый ник: " + nickname + "\n");
                                continue;
                            }
                            if (msg.startsWith("/clients_list ")) {
                                Platform.runLater(() ->{
                                    clientsList.getItems().clear();
                                    String[] tokens = msg.split(" ");
                                    for (int i = 1; i < tokens.length; i++) {
                                        if (!nickname.equals(tokens[i])) {
                                            clientsList.getItems().add(tokens[i]);
                                        }
                                    }
                                });
                            }
                        } else {
                            mainArea.appendText(msg + "\n");
                            history.writeHistory(msg);
                        }
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Соединение с сервером разорвано\n", ButtonType.OK);
                        alert.showAndWait();
                    });

                } finally {
                    network.close();
                    setAuthenticated(false);
                    nickname = null;
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Невозможно подключиться к серверу", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void sendMsg(ActionEvent actionEvent) {
        try {
            String msg = msgField.getText();
            network.sendMsg(msg);
            //history.writeHistory(msg);
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось отправить сообщение, проверьте сетевое подключение", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        try {
            tryToConnect();
            network.sendMsg("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось отправить сообщение, проверьте сетевое подключение", ButtonType.OK);
            alert.showAndWait();
        }
    }
}