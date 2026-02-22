package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ClientModel model;
    private boolean isConnected;

    public TCPClient(ClientModel model) {
        this.model = model;
    }

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        isConnected = true;
        
        model.addLog("Connected to server at " + host + ":" + port);

        // Start listening for incoming messages
        new Thread(() -> {
            try {
                String message;
                while (isConnected && (message = in.readLine()) != null) {
                    model.addMessage("Server: " + message);
                }
            } catch (IOException e) {
                if (isConnected) {
                    model.addLog("Error reading from server: " + e.getMessage());
                }
            }
        }).start();
    }

    public void sendMessage(String message) {
        if (out != null && isConnected) {
            out.println(message);
            model.addMessage("You: " + message);
        }
    }

    public void disconnect() {
        isConnected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            model.addLog("Disconnected from server");
        } catch (IOException e) {
            model.addLog("Error disconnecting: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }
}
