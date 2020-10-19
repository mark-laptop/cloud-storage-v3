package ru.ndg.cloud.storage.v3.server.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ndg.cloud.storage.v3.server.network.Server;

import javax.security.auth.callback.Callback;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class Controller implements Initializable {

    private static final Logger logger = LogManager.getLogger(Controller.class);
    private Server server;
    private Callback callback;

    @FXML
    TextField portField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.server = new Server();
    }

    public void startServer(ActionEvent actionEvent) {
        String portString = this.portField.getText();
        int port = 0;
        if (portString.isEmpty()) {
            String msg = "Укажите порт для сервера";
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
        this.server.setPort(port);
        CountDownLatch latch = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            Controller.this.server.run(latch);
        });
        t.setDaemon(true);
        t.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            this.server.setPort(0);
            logger.debug(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void showAlert(Alert.AlertType alertType, String msg, ButtonType buttonType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType, msg, buttonType);
            alert.show();
        });
    }
}
