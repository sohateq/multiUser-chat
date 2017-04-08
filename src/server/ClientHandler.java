package server;

import exceptions.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;

    private DataOutputStream out;
    private DataInputStream in;

    private static int clientsCount = 0;
    private String clientName;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;

            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            clientsCount++;
            clientName = "client" + clientsCount;

            System.out.println("Client \"" + clientName + "\" ready!");
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        if (!waitForAuth()) return;;

        Commands.showCommands(this);

        waitForMessage();
        server.deleteClient(this);

    }

    private void waitForMessage() {
        while (true) {
            String message = null;
            try {
                message = in.readUTF();

                Commands.ifCommand(message, this); //сработает только если в сообщении есть команда

            } catch (IOException e) {
                System.out.println("Соединение с клиентом " + clientName + " остановлено");
                break;
            }
            System.out.println(clientName + ": " + message);

                new Thread(new MessagesSender(message, clientName, server)).start();
        }
    }

    private boolean waitForAuth() {
        boolean auth = false;
        while (!auth) {
            String message = null;
            try {
                message = in.readUTF();
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println(clientName + " вышел без авторизации");
                break;
            }
            try {

                if (isAuthOk(message)) {
                    out.writeUTF("signIn_success");
                    auth = true;
                    break;
                } else {
                    out.writeUTF("signIn_fail");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (auth == false) return auth;

        System.out.println(clientName + " подключился к чату");
        new Thread(new MessagesSender(clientName + " подключился к чату", null, server)).start();
        return auth;
    }

    private boolean isAuthOk(String message) {
        System.out.println(clientName + "[НЕ АВТОРИЗОВАН]: " + message);

        if (message != null) {
            String[] parsedMessage = message.split("___");

                try {
                    registrationWithoutAuth(parsedMessage); //прикрутил это в самом конце, чтобы не залогиненный мог зарегистрироваться
                    processAuthMessage(parsedMessage);
                    return true;
                } catch (AuthFailException e) {
                    return false;
                }

        }
        return false;
    }

    private void registrationWithoutAuth(String[] cm) {
        if (!cm[0].equals("reg")) return;
        try {
            String nick = cm[1];
            String login = cm[2];
            String password = cm[3];
            SQLHandler.registration(nick, login, password);
            //время костылей. Это нужно чтобы после регистрации сразу залогиниться
            cm[0] = "auth";
            cm[1] = login;
            cm[2] = password;
        } catch (RegistrationFailException e){
            e.printStackTrace();
        }
    }

    private void processAuthMessage(String[] parsedMessage) throws AuthFailException   {
        if (parsedMessage[0].equals("auth")) {
            System.out.println("Попытка авторизации от " + clientName);
            String login = parsedMessage[1];
            String password = parsedMessage[2];

            String nick = null;
            try {
                nick = SQLHandler.authorise(login, password);
            }  catch ( NoLoginException e){
                System.out.println("Нет такого логина");
            } catch (NoPassException e){
                System.out.println("Нет такого пароля");
            } catch (NoNickException e){
                System.out.println("Нет такого пользователя");
            } catch (NoConnectionToDataBase e){
                System.out.println("Нет связи с базой банных");
            }

            if(nick != null) {
                server.addClient(this, nick);
                return;
            }

            throw new AuthFailException();
        }
    }


    public DataOutputStream getOut() {
        return out;
    }
    public String getClientName() {
        return clientName;
    }
    public ClientHandler getClientHandler(){
        return this;
    }
    public Server getServer() {
        return server;
    }



    public void setNick(String nick) {
        this.clientName = nick;
    }







}
