import client.ClientWindow;

public class TestClients {

    public static final String SERVER_HOST = "localhost"; //127.0.0.1
    public static final int SERVER_PORT = 9933;

    public static void main(String[] args) {
        new ClientWindow(SERVER_HOST, SERVER_PORT);
        new ClientWindow(SERVER_HOST, SERVER_PORT);
//        new ClientWindow(SERVER_HOST, SERVER_PORT);
    }
}