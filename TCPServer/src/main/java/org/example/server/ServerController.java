package org.example.server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

public class ServerController implements ServerModel.Listener {

    @FXML private TextArea chatLog;
    @FXML private ListView<String> usersListView;
    @FXML private TextField portField;
    @FXML private Button startButton;
    @FXML private Button stopButton;

    private ServerModel server;
    private Thread serverThread;

    @FXML
    public void initialize() {
        chatLog.setEditable(false);
        stopButton.setDisable(true);

        // Load default port from properties, if present
        int port = 5555;
        try (InputStream is = getClass().getResourceAsStream("/server-config.properties")) {
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                port = Integer.parseInt(p.getProperty("server.port", "5555").trim());
            }
        } catch (Exception ignored) {
        }

        portField.setText(String.valueOf(port));

        // Color each username row based on server-assigned color
        usersListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setBackground(null);
                    return;
                }

                setText(user);
                if (server != null) {
                    String hex = server.getUserColor(user);
                    Color c = Color.web(hex);
                    setBackground(new Background(new BackgroundFill(c, null, null)));
                } else {
                    setBackground(null);
                }
            }
        });
    }

    @FXML
    private void onStart() {
        if (serverThread != null && serverThread.isAlive()) return;

        int port = 5555;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (Exception ignored) {
        }

        server = new ServerModel(port);
        server.setListener(this);

        serverThread = new Thread(server::start, "server-main");
        serverThread.setDaemon(true);
        serverThread.start();

        startButton.setDisable(true);
        stopButton.setDisable(false);
        portField.setDisable(true);
    }

    @FXML
    private void onStop() {
        if (server != null) server.stop();

        startButton.setDisable(false);
        stopButton.setDisable(true);
        portField.setDisable(false);
        onLogLine("[SERVER] Stop requested from UI.");
    }

    // Listener callbacks (from ServerModel thread) -> must hop to FX thread
    @Override
    public void onUserListChanged(Set<String> usernames) {
        Platform.runLater(() -> {
            usersListView.getItems().setAll(usernames);
            usersListView.refresh();
        });
    }

    @Override
    public void onLogLine(String line) {
        Platform.runLater(() -> chatLog.appendText(line + "\n"));
    }
}