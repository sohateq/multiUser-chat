package server;

import exceptions.ChangeNickException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Анатолий on 13.03.2017.
 */
public class Commands {
    private static ArrayList<String> commandsInfo = makeList();
    private static final String PASSWORD = "adminPass123";

    public static void ifCommand(String command, ClientHandler clientHandler){
        if (command.contains("/changenick")){
            changeNick(command, clientHandler);
        }
        if (command.contains("/kill")){
            killClient(clientHandler, command);

        }

    }
    //public static void changeNick(){}
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

    public static ArrayList<String> makeList(){
       ArrayList<String> commandList = new ArrayList<>();
       commandList.add("---Вам доступны следующие комманды---");
       commandList.add("/changenick New_Nick - позволяет сменить имя учетной записи");
       commandList.add("/kil Server_Password Nick - удаление пользователя с именем Nick из чата");
       return commandList;
    }
    public static void showCommands(ClientHandler clientHandler){
//       try{
//           clientHandler.getOut().writeUTF(commandsInfo.toString());
//       } catch(IOException e){
//           e.printStackTrace();
//           System.out.println("Не получается показать команды");
//       }
        for(String com : commandsInfo){
            try{
                clientHandler.getOut().writeUTF(com);
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void killClient(ClientHandler clientHandler, String command) {
        String[] cm = command.split(" ");
        if (!cm[1].equals(PASSWORD)) {
            try {
                clientHandler.getOut().writeUTF("Неверный пароль"); //надо проверить, не рассылается ли оно всем.
                //new Thread(new MessagesSender("Неверный пароль", clientHandler.getClientName(), clientHandler.getServer())).start(); //и тогда заменить на это
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String nick = "";
        for (int i = 2; i < cm.length; i++) {
            nick = nick + cm[i] + " ";
        }
        ClientHandler temp;
        if ((temp =  clientHandler.getServer().getClientHandlerByNick(nick)) != null) {
            try {
                temp.getOut().writeUTF("Администратор выгнал вас из чата");
                temp.getOut().writeUTF("end session");
            } catch (IOException e) {
                e.printStackTrace();

            }
        }


        //        try {
//            clientHandler.getOut().writeUTF("end session");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}