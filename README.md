# TCP Server-Client Application

A JavaFX-based TCP Server and Client application implementing the Model-View-Controller (MVC) architecture pattern.

## Project Structure

```
ParadigmP1/
├── TCPServer/          # Server application
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/org/example/server/
│       │   │   ├── TCPServerApp.java       # Main application entry point
│       │   │   ├── TCPServer.java          # Server socket management
│       │   │   ├── ServerModel.java        # Data model
│       │   │   ├── ServerController.java   # UI controller
│       │   │   └── ClientHandler.java      # Individual client connection handler
│       │   └── resources/
│       │       ├── ServerView.fxml         # UI layout
│       │       └── server-config.properties # Configuration
│       └── test/java/
│
├── TCPClient/          # Client application
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/org/example/client/
│       │   │   ├── TCPClientApp.java       # Main application entry point
│       │   │   ├── TCPClient.java          # Client socket management
│       │   │   ├── ClientModel.java        # Data model
│       │   │   └── ClientController.java   # UI controller
│       │   └── resources/
│       │       ├── ClientView.fxml         # UI layout
│       │       └── client-config.properties # Configuration
│       └── test/java/
│
└── README.md
```

## Features

### Server
- Start/stop TCP server on configurable port
- Handle multiple client connections simultaneously
- Broadcast messages to all connected clients
- Real-time logging of server activities
- Thread-safe client management

### Client
- Connect to TCP server via host and port
- Send and receive messages
- Real-time chat display
- Connection status logging
- Graceful disconnect handling

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- JavaFX 21

## Building the Projects

### Build Server
```bash
cd TCPServer
mvn clean install
```

### Build Client
```bash
cd TCPClient
mvn clean install
```

## Running the Applications

### Run Server
```bash
cd TCPServer
mvn javafx:run
```

### Run Client
```bash
cd TCPClient
mvn javafx:run
```

## Configuration

### Server Configuration (`server-config.properties`)
- `server.port`: Port number for the server (default: 8080)
- `server.max.connections`: Maximum concurrent connections (default: 10)

### Client Configuration (`client-config.properties`)
- `client.host`: Server hostname (default: localhost)
- `client.port`: Server port (default: 8080)

## Architecture

Both applications follow the **MVC (Model-View-Controller)** pattern:

- **Model**: Manages application data and business logic
  - `ServerModel` / `ClientModel`: Handle data and state management
  
- **View**: FXML files defining the user interface
  - `ServerView.fxml` / `ClientView.fxml`: UI layouts
  
- **Controller**: Handles user interactions and updates the model/view
  - `ServerController` / `ClientController`: Bridge between model and view

## Usage

1. Start the server application first
2. Click "Start Server" with desired port (default: 8080)
3. Launch one or more client applications
4. Enter server host and port in each client
5. Click "Connect" to establish connection
6. Send messages from any client - they will be broadcast to all other clients

## Notes

- The server must be running before clients can connect
- Multiple clients can connect to a single server simultaneously
- Messages are broadcast to all connected clients except the sender
- Both applications handle graceful shutdown and cleanup of resources
