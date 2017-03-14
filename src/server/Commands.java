package server;

import exceptions.ChangeNickException;

/**
 * Created by Анатолий on 13.03.2017.
 */
public class Commands {

    public static void ifCommand(String command, ClientHandler clientHandler){
        if (command.contains("/changenick")){
            changeNick(command, clientHandler);
        }

    }

    public static void changeNick(String message, ClientHandler clientHandler) {

        //String command = ifCommand(message);
        String[] cm = message.split(" ");
        String newNick = "";
        if (cm[0].equals("/changenick")) {
            for (int i = 1; i < cm.length; i++) {
                newNick = newNick + cm[i] + " ";
            }
        }

        if (newNick != "") {
            try {
                SQLHandler.changeNick(clientHandler.getClientName(), newNick);
            } catch (ChangeNickException e) {
                return;
            }
            String oldNick = clientHandler.getClientName();
            clientHandler.setNick(newNick);
            //out.writeUTF("Пользователь " + oldNick + " сменил ник на " + newNick);
            new Thread(new MessagesSender("Пользователь " + oldNick + " сменил ник на " + newNick, clientHandler.getClientName(), clientHandler.getServer())).start();
        }
    }


}
