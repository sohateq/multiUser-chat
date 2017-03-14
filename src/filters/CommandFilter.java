package filters;

import server.ClientHandler;
import server.SQLHandler;

import java.io.IOException;

/**
 * Created by Анатолий on 05.03.2017.
 */
public class CommandFilter implements ChatFilter {
    @Override
    public String filter(String message) {
        if (message.contains("/changenick")) {
            return null;
        }
        if (message.equals("end")) return "Пользователь покинул чат";

        if (message.contains("auth___")) { //на всякий случай, чтобы не засветить пароль если в программе есть ошибка
            return null;
        }
        //return message;

//        if (message.equals("123")){
//            System.out.println("Я просто тестирую новый планшет:)");
//            return "Сережа лолик";
//        }
        return message;
    }

    @Override
    public String getName() {
        return " фильтр консольных команд ";
    }
}
