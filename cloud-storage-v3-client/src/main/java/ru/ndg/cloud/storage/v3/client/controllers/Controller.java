package ru.ndg.cloud.storage.v3.client.controllers;

import io.netty.channel.ChannelFuture;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ndg.cloud.storage.v3.client.handlers.Callback;
import ru.ndg.cloud.storage.v3.client.network.Network;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class Controller implements Initializable {

    private static final Logger logger = LogManager.getLogger(Controller.class);
    private Network network;
    private String login;
    private String password;

    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    TextField hostField;
    @FXML
    TextField portField;
    @FXML
    Button btnConnection;
    @FXML
    HBox hBoxHostPort;
    @FXML
    HBox hBoxLoginPassword;
    @FXML
    Button btnSendFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Callback callback = (args) -> {
            // определить
        };
        this.network = new Network(callback);
        updateInterface();
    }

    public void connectionToServer(ActionEvent actionEvent) {
        String hostString = hostField.getText();
        String portString = portField.getText();
        int port = 0;
        if (hostString.isEmpty() || portString.isEmpty()) {
            String msg = "Укажите хост и порт для подключения";
            showAlert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
            return;
        }
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            logger.debug(e.getMessage());
            String msg = "Порт должен содержать только цифры";
            showAlert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
            return;
        }
        this.network.setHost(hostString);
        this.network.setPort(port);
        final CountDownLatch latch = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            Controller.this.network.run(latch);
        });
        t.setDaemon(true);
        t.start();
        try {
            latch.await();
            updateInterface();
        } catch (InterruptedException e) {
            logger.debug(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void updateInterface() {
        if (network != null && network.isOpen()) {
            hBoxHostPort.setVisible(false);
            hBoxHostPort.setManaged(false);
            hBoxLoginPassword.setVisible(true);
            hBoxLoginPassword.setManaged(true);
            btnSendFile.setVisible(true);
            btnSendFile.setManaged(true);
        } else {
            hBoxHostPort.setVisible(true);
            hBoxHostPort.setManaged(true);
            hBoxLoginPassword.setVisible(false);
            hBoxLoginPassword.setManaged(false);
            btnSendFile.setVisible(false);
            btnSendFile.setManaged(false);
        }

    }

    public void authenticationToServer(ActionEvent actionEvent) {
        if (!isCorrectLoginPassword()) return;
        this.network.sendAuthCommand(this.login + " " + this.password, null);
    }

    public void registrationToServer(ActionEvent actionEvent) {
        if (!isCorrectLoginPassword()) return;
        this.network.sendRegCommand(this.login + " " + this.password, null);
    }

    private boolean isCorrectLoginPassword() {
        this.login = loginField.getText();
        this.password = passwordField.getText();
        if (this.login.isEmpty() || this.password.isEmpty()) {
            String msg = "Укажите логин и пароль";
            showAlert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
            return false;
        }
        return true;
    }

    public void sendFile(ActionEvent actionEvent) {
        this.network.sendFile(this.login);
    }

    private void showAlert(Alert.AlertType alertType, String msg, ButtonType buttonType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType, msg, buttonType);
            alert.show();
        });
    }

    public void close(ActionEvent actionEvent) {
        if (network == null || !network.isOpen()) Platform.exit();
        ChannelFuture close = network.close();
        if (close.isSuccess()) {
            Platform.exit();
        } else {
            String msg = "What a fuck is this!!!!";
            showAlert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        }
    }
}
