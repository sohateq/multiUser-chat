package server;

import exceptions.*;

import java.sql.*;

public class SQLHandler {
    private static Connection connection;
    //private static PreparedStatement statement;



    public static void connect(String dbName) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
    }

    public static String authorise(String login, String password) throws AuthFailException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new NoConnectionToDataBase();
        }
        String nick = null;
//         try{
//            ResultSet rs = statement.executeQuery(
//                     "select nick from \"users_table\" where login = \"" + login
//                             + "\" and password = \"" + password + "\"");
//             while (rs.next()) {
//                 nick = rs.getString(1);
//             }
//         } catch(SQLException e){
//
//         }
        //return nick;

        String loginLookingFor = null;
        String passLookingFor = null;

        try {
            ResultSet rs = statement.executeQuery("select login from " +
                    "\"users_table\" where password = \"" + password + "\"");
            while (rs.next()) {
                loginLookingFor = rs.getString(1);
            }
            try {
                if (!loginLookingFor.equals(login)) {
                    throw new NoLoginException();
                }
            } catch (NullPointerException e) {
                //throw new NoPassException();
            }
        } catch (SQLException e) {
            throw new AuthFailException();
        }


        try {
            ResultSet rs = statement.executeQuery("select password from " +
                    "\"users_table\" where login = \"" + login + "\"");
            while (rs.next()) {
                passLookingFor = rs.getString(1);
            }
            try {
                if (!passLookingFor.equals(password)) {
                    throw new NoPassException();
                }
            } catch (NullPointerException e) {

            }

        } catch (SQLException e) {

            throw new AuthFailException();
        }

        try {
            ResultSet rs = statement.executeQuery(
                    "select nick from \"users_table\" where login = \"" + loginLookingFor
                            + "\" and password = \"" + passLookingFor + "\"");
            while (rs.next()) {
                nick = rs.getString(1);
            }
            if (nick == null) {
                throw new NoNickException();
            }

        } catch (SQLException e) {

            throw new AuthFailException();
        }

        return nick;
    }

    public static void changeNick(String oldNick, String newNick) throws ChangeNickException {
        //Statement statement = null;
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("update \"users_table\" set nick = \"" + newNick +
                    "\" where nick = \"" + oldNick + "\"");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Не удалось сменить ник");
            throw new ChangeNickException();
        }
    }

    public static void registration (String nick, String login, String pass){
        try{
            Statement statement = connection.createStatement();
            statement.executeUpdate("insert into \"users_table\" "
                    + "values ('" + nick + "', '" + login + "', '" + pass + "')");
            System.out.println("Зарегистирировался новый пользователь с ником " + nick);
        } catch (SQLException e){
            e.printStackTrace();
        }

    }

}
