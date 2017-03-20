package server;

import exceptions.AuthFailException;
import exceptions.NoLoginException;
import filters.ChatFilter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Server {
    private static volatile Server instance;

    private List<ClientHandler> clients;

    private List<ChatFilter> filters;

    private ServerSocket serverSocket = null;

    public void addFilter(ChatFilter filter) {
        filters.add(filter);
        System.out.println("Добавлен" + filter.getName());
    }

    public synchronized void addClient(ClientHandler clientHandler, String nick) throws AuthFailException {
        for(ClientHandler client : clients) {
            if (client.getClientName().equals(nick)) {
                System.out.println("Пользователь с ником " + nick + " уже в сети");
                throw new AuthFailException();
            }
        }
        clientHandler.setNick(nick);

        clients.add(clientHandler);
        System.out.println(clientHandler.getClientName() + " теперь онлайн");
    }

    public synchronized void deleteClient (ClientHandler clientHandler){

        clients.remove(clientHandler);

    }

    public synchronized ClientHandler getClientHandlerByNick(String nick){
        //return clients.get(clients.indexOf(nick));
        for(ClientHandler client : clients) {
            if (client.getClientName().equals(nick)) {
                return client;
            }
        }
        return null;
    }

    public Server(int serverPort, String dbName) {
        System.out.println("Запуск сервера");
        clients = new LinkedList<>();
        filters = new ArrayList<>();

        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Сокет открыт");

            SQLHandler.connect(dbName);
            System.out.println("База данных в норме");

            System.out.println("Сервер ожидает клиентов...");
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void waitForClient() {
        Socket socket = null;
        try {
            while(true) {
                    socket = serverSocket.accept();
                    System.out.println("Присоединился клиент");
                    ClientHandler client = new ClientHandler(socket, this);
                    new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
                System.out.println("Сервер остановлен.");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void newMessageFromClient(String message) {
        for (ChatFilter filter : filters) {
            message = filter.filter(message);
            if (message == null) return;
        }

        for(ClientHandler client : clients) {
            try {
                client.getOut().writeUTF(message);
                client.getOut().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            client.getOut().flush();
        }
    }

}