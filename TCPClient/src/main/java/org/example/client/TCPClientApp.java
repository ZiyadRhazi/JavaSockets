package org.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TCPClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientView.fxml"));
        Scene scene = new Scene(loader.load(), 600, 400);
        
        primaryStage.setTitle("TCP Client");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            ClientController controller = loader.getController();
            controller.shutdown();
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
