package org.example.client;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ClientModel {
    private final StringProperty messageProperty;
    private final StringProperty logProperty;
    private StringBuilder messageBuilder;
    private StringBuilder logBuilder;

    public ClientModel() {
        this.messageProperty = new SimpleStringProperty("");
        this.logProperty = new SimpleStringProperty("");
        this.messageBuilder = new StringBuilder();
        this.logBuilder = new StringBuilder();
    }

    public void addMessage(String message) {
        Platform.runLater(() -> {
            messageBuilder.append(message).append("\n");
            messageProperty.set(messageBuilder.toString());
        });
    }

    public void addLog(String log) {
        Platform.runLater(() -> {
            logBuilder.append(log).append("\n");
            logProperty.set(logBuilder.toString());
        });
    }

    public StringProperty messageProperty() {
        return messageProperty;
    }

    public StringProperty logProperty() {
        return logProperty;
    }
}
