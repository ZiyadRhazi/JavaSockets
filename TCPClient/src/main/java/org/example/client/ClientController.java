package org.example.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientController implements ClientModel.Listener {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML private TextArea chatLog;
    @FXML private TextField messageField;
    @FXML private Button sendButton;

    private final ClientModel model = new ClientModel();

    private String username = "";
    private boolean readOnly = false;

    @FXML
    public void initialize() {
        chatLog.setEditable(false);

        model.setListener(this);

        // 1) Prompt username
        Platform.runLater(this::promptUsernameAndConnect);

        // 2) Send button handler
        sendButton.setOnAction(e -> onSend());

        // 3) Enter key sends too
        messageField.setOnAction(e -> onSend());
    }

    private void promptUsernameAndConnect() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Username");
        dialog.setHeaderText("Enter your username");
        dialog.setContentText("Username:");

        username = dialog.showAndWait().orElse("").trim();

        if (username.isEmpty()) {
            readOnly = true;
            appendLocal("[CLIENT] READ-ONLY MODE: blank username.");
            sendButton.setDisable(true);
            messageField.setDisable(true);
        } else {
            readOnly = false;
            sendButton.setDisable(false);
            messageField.setDisable(false);
        }

        // 2) Connect to server using config resolved from CLI/properties
        try {
            model.connect(ClientConfig.host, ClientConfig.port, username);
            appendLocal("[CLIENT] Connected. Type messages. Commands: allUsers, bye, end");
        } catch (Exception ex) {
            appendLocal("[CLIENT] Failed to connect: " + ex.getMessage());
            sendButton.setDisable(true);
            messageField.setDisable(true);
        }
    }

    private void onSend() {
        String text = messageField.getText();
        if (text == null) return;

        text = text.trim();
        if (text.isEmpty()) return;

        if (readOnly) {
            appendLocal("[CLIENT] READ-ONLY: you cannot send messages.");
            messageField.clear();
            return;
        }

        model.sendMessage(text);
        messageField.clear();

        // If user typed bye/end, also disconnect locally
        if (text.equalsIgnoreCase(ServerWords.BYE) || text.equalsIgnoreCase(ServerWords.END)) {
            appendLocal("[CLIENT] Disconnecting...");
            model.disconnect();
            sendButton.setDisable(true);
            messageField.setDisable(true);
        }
    }

    // Listener callbacks (non-FX thread) -> hop to FX thread
    @Override
    public void onServerLine(String line) {
        Platform.runLater(() -> chatLog.appendText(line + "\n"));
    }

    @Override
    public void onStatus(String statusLine) {
        Platform.runLater(() -> appendLocal("[CLIENT] " + statusLine));
    }

    @Override
    public void onDisconnected(String reason) {
        Platform.runLater(() -> {
            appendLocal("[CLIENT] " + reason);
            sendButton.setDisable(true);
            messageField.setDisable(true);
        });
    }

    private void appendLocal(String msg) {
        String line = "[" + TS.format(LocalDateTime.now()) + "] " + msg;
        chatLog.appendText(line + "\n");
    }

    /**
     * Keep command words in one place (avoids typos).
     * This matches what your server expects: allUsers, bye, end.
     */
    private static final class ServerWords {
        private static final String BYE = "bye";
        private static final String END = "end";
        private ServerWords() {}
    }
}