package org.example.server;

/**
 * Simple CLI entry point (useful for quick testing without JavaFX).
 */
public class TCPServer {
    public static void main(String[] args) {
        int port = 5555;

        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        ServerModel server = new ServerModel(port);
        server.start(); // blocking accept-loop
    }
}