package org.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ServerModel server;

    private BufferedReader in;
    private PrintWriter out;

    private String username;       // null if not registered (read-only-ish)
    private boolean registered;    // true once username accepted

    public ClientHandler(Socket socket, ServerModel server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 1) Ask for username (client should respond with a single line)
            out.println("[SERVER] Enter your username:");

            String candidate = in.readLine(); // may be null if disconnect immediately
            if (candidate == null) return;

            if (server.registerUsername(candidate, this)) {
                username = candidate.trim();
                registered = true;
                out.println("[SERVER] Username accepted: " + username);
                server.broadcastChat("SERVER", username + " joined the chat.");
            } else {
                // Client is allowed to stay connected, but we treat it as read-only
                registered = false;
                out.println("[SERVER] Username invalid or taken. You are in READ-ONLY mode.");
            }

            // 2) Main read loop
            String line;
            while ((line = in.readLine()) != null) {
                String msg = line.trim();
                if (msg.isEmpty()) continue;

                // Commands
                if (msg.equals(ServerModel.CMD_ALL_USERS)) {
                    server.sendAllUsersTo(this);
                    continue;
                }
                if (msg.equalsIgnoreCase(ServerModel.CMD_BYE) || msg.equalsIgnoreCase(ServerModel.CMD_END)) {
                    out.println("[SERVER] Disconnecting. Bye!");
                    break;
                }

                // Enforce read-only if not registered
                if (!registered) {
                    out.println("[SERVER] READ-ONLY: You cannot send messages without a valid username.");
                    continue;
                }

                // Normal chat
                server.broadcastChat(username, msg);
            }

        } catch (IOException e) {
            // client connection issues
        } finally {
            // Cleanup always
            if (registered && username != null) {
                server.broadcastChat("SERVER", username + " left the chat.");
            }
            server.disconnect(this);

            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void send(String message) {
        if (out != null) out.println(message);
    }
}