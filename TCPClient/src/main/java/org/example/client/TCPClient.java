package org.example.client;

import java.io.InputStream;
import java.util.Properties;

public class TCPClient {

    public static void main(String[] args) {
        // 1) Load defaults from client-config.properties (optional fallback)
        loadDefaultsFromProperties();

        // 2) Required CLI format: java TCPClient <ServerIPAddress> <PortNumber>
        // Args must override properties.
        if (args.length >= 2) {
            ClientConfig.host = args[0].trim();
            try {
                ClientConfig.port = Integer.parseInt(args[1].trim());
            } catch (NumberFormatException ignored) {
                // keep default port
            }
        }

        // 3) Launch JavaFX app
        TCPClientApp.main(args);
    }

    private static void loadDefaultsFromProperties() {
        try (InputStream is = TCPClient.class.getResourceAsStream("/client-config.properties")) {
            if (is == null) return;

            Properties p = new Properties();
            p.load(is);

            String host = p.getProperty("server.host");
            String portStr = p.getProperty("server.port");

            if (host != null && !host.isBlank()) ClientConfig.host = host.trim();
            if (portStr != null && !portStr.isBlank()) {
                try {
                    ClientConfig.port = Integer.parseInt(portStr.trim());
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }
}