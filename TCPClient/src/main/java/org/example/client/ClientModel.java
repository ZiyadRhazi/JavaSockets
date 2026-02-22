package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientModel {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private Thread receiverThread;
    private volatile boolean running;

    private Listener listener;

    public interface Listener {
        void onServerLine(String line);
        void onStatus(String statusLine);
        void onDisconnected(String reason);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void connect(String host, int port, String usernameToSend) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        running = true;

        if (listener != null) {
            listener.onStatus("Connected to " + host + ":" + port);
        }

        // Start receiver loop
        receiverThread = new Thread(this::receiveLoop, "client-receiver");
        receiverThread.setDaemon(true);
        receiverThread.start();

        // Server asks for username first; we respond immediately (even if blank)
        sendRaw(usernameToSend == null ? "" : usernameToSend);
    }

    private void receiveLoop() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                if (listener != null) listener.onServerLine(line);
            }
            if (listener != null) listener.onDisconnected("Server closed the connection.");
        } catch (IOException e) {
            if (listener != null) listener.onDisconnected("Disconnected: " + e.getMessage());
        } finally {
            running = false;
            closeSilently();
        }
    }

    public void sendMessage(String text) {
        // Normal chat messages and commands are both just "send a line"
        sendRaw(text);
    }

    private void sendRaw(String line) {
        if (out != null) out.println(line);
    }

    public void disconnect() {
        running = false;
        closeSilently();
    }

    private void closeSilently() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
}