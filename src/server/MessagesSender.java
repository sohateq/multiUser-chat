package server;

public class MessagesSender implements Runnable {
    private String message;
    private String clientName;
    private Server server;

    public MessagesSender(String message, String clientName, Server server) {
        this.message = message;
        this.clientName = clientName;
        this.server = server;
        if (clientName != null) this.message = clientName + ": " + message;
    }

    @Override
    public void run() {
        server.newMessageFromClient(this.message);
    }
}
