package org.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerModel {

    public static final String CMD_ALL_USERS = "allUsers";
    public static final String CMD_BYE = "bye";
    public static final String CMD_END = "end";

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final int port;
    private volatile boolean running;
    private ServerSocket serverSocket;

    // All connected handlers (including those not registered with a username)
    private final Set<ClientHandler> connections =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    // username -> handler (only registered users)
    private final Map<String, ClientHandler> users = new ConcurrentHashMap<>();

    // username -> random color hex (for server GUI list)
    private final Map<String, String> userColors = new ConcurrentHashMap<>();

    private final Random random = new Random();

    private Listener listener;

    public ServerModel(int port) {
        this.port = port;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void start() {
        running = true;

        try (ServerSocket ss = new ServerSocket(port)) {
            serverSocket = ss;

            log("[SERVER] Listening on port " + port);
            notifyLog("[SERVER] Listening on port " + port);

            while (running) {
                Socket socket = ss.accept();
                log("[SERVER] Connected: " + socket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(socket, this);
                connections.add(handler);

                Thread t = new Thread(handler, "client-" + socket.getPort());
                t.start();
            }

        } catch (IOException e) {
            log("[SERVER] Stopped/failed: " + e.getMessage());
            notifyLog("[SERVER] Stopped/failed: " + e.getMessage());
        } finally {
            running = false;
        }
    }

    public void stop() {
        running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close(); // breaks accept()
            } catch (IOException ignored) {
            }
        }
        notifyLog("[SERVER] Stop requested.");
    }

    // Called by ClientHandler after reading a username
    public boolean registerUsername(String username, ClientHandler handler) {
        String u = (username == null) ? "" : username.trim();
        if (u.isEmpty()) return false;

        ClientHandler existing = users.putIfAbsent(u, handler);
        if (existing != null) return false;

        userColors.put(u, randomColorHex());
        notifyUserListChanged();
        notifyLog("[SERVER] User joined: " + u);

        return true;
    }

    public void disconnect(ClientHandler handler) {
        connections.remove(handler);

        String username = getUsernameOf(handler);
        if (username != null) {
            users.remove(username);
            userColors.remove(username);
            notifyUserListChanged();
            notifyLog("[SERVER] User left: " + username);
        } else {
            notifyLog("[SERVER] Client left (no username).");
        }
    }

    public void broadcastChat(String fromUsername, String text) {
        String safeUser = (fromUsername == null || fromUsername.isBlank()) ? "UNKNOWN" : fromUsername.trim();
        String safeText = (text == null) ? "" : text;

        String payload = "[" + TS.format(LocalDateTime.now()) + "] " + safeUser + ": " + safeText;

        for (ClientHandler h : connections) {
            h.send(payload);
        }

        log(payload);
        notifyLog(payload);
    }

    public void sendAllUsersTo(ClientHandler requester) {
        Set<String> list = new LinkedHashSet<>(users.keySet());
        String payload = "[" + TS.format(LocalDateTime.now()) + "] [SERVER] Users online: " + String.join(", ", list);
        requester.send(payload);

        log("[SERVER->ONE] " + payload);
        notifyLog(payload);
    }

    public Set<String> getUsernamesSnapshot() {
        return new LinkedHashSet<>(users.keySet());
    }

    public String getUserColor(String username) {
        if (username == null) return "#FFFFFF";
        return userColors.getOrDefault(username, "#FFFFFF");
    }

    public int getPort() {
        return port;
    }

    private String getUsernameOf(ClientHandler handler) {
        for (Map.Entry<String, ClientHandler> e : users.entrySet()) {
            if (e.getValue() == handler) return e.getKey();
        }
        return null;
    }

    private String randomColorHex() {
        int r = 80 + random.nextInt(176);
        int g = 80 + random.nextInt(176);
        int b = 80 + random.nextInt(176);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private void notifyUserListChanged() {
        if (listener != null) {
            listener.onUserListChanged(getUsernamesSnapshot());
        }
    }

    private void notifyLog(String line) {
        if (listener != null) {
            listener.onLogLine(line);
        }
    }

    private void log(String msg) {
        System.out.println(msg);
    }

    public interface Listener {
        void onUserListChanged(Set<String> usernames);
        void onLogLine(String line);
    }
}