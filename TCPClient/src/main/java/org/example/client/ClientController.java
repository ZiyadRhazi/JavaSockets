package org.example.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Properties;

public class ClientController {
    @FXML
    private TextField hostField;
    
    @FXML
    private TextField portField;
    
    @FXML
    private Button connectButton;
    
    @FXML
    private Button disconnectButton;
    
    @FXML
    private TextField messageField;
    
    @FXML
    private Button sendButton;
    
    @FXML
    private TextArea chatArea;
    
    @FXML
    private TextArea logArea;

    private TCPClient client;
    private ClientModel model;

    @FXML
    public void initialize() {
        model = new ClientModel();
        client = new TCPClient(model);
        
        chatArea.textProperty().bind(model.messageProperty());
        logArea.textProperty().bind(model.logProperty());
        
        disconnectButton.setDisable(true);
        sendButton.setDisable(true);
        
        loadConfiguration();
    }

    private void loadConfiguration() {
        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("/client-config.properties"));
            hostField.setText(props.getProperty("client.host", "localhost"));
            portField.setText(props.getProperty("client.port", "8080"));
        } catch (Exception e) {
            hostField.setText("localhost");
            portField.setText("8080");
            model.addLog("Could not load configuration, using defaults");
        }
    }

    @FXML
    private void handleConnect() {
        try {
            String host = hostField.getText();
            int port = Integer.parseInt(portField.getText());
            client.connect(host, port);
            
            connectButton.setDisable(true);
            disconnectButton.setDisable(false);
            sendButton.setDisable(false);
            hostField.setDisable(true);
            portField.setDisable(true);
        } catch (NumberFormatException e) {
            model.addLog("Invalid port number");
        } catch (IOException e) {
            model.addLog("Failed to connect: " + e.getMessage());
        }
    }

    @FXML
    private void handleDisconnect() {
        client.disconnect();
        
        connectButton.setDisable(false);
        disconnectButton.setDisable(true);
        sendButton.setDisable(true);
        hostField.setDisable(false);
        portField.setDisable(false);
    }

    @FXML
    private void handleSend() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            messageField.clear();
        }
    }

    public void shutdown() {
        if (client.isConnected()) {
            client.disconnect();
        }
    }
}
