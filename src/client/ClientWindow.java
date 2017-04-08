package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class ClientWindow extends JFrame {

    public static final int WINDOW_WIDTH = 650;
    public static final int WINDOW_HEIGHT = 600;

    JPanel authPanel;
    private JTextField clientMsgElement;
    private JTextArea serverMsgElement;

    final String serverHost;
    final int serverPort;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    public ClientWindow(String host, int port) {
        serverHost = host;
        serverPort = port;

        initConnection();
        initGUI();
        initServerListener();
    }

    private void initConnection() {
        try {
            socket = new Socket(serverHost, serverPort);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initGUI() {
        setBounds(600, 300, WINDOW_WIDTH, WINDOW_HEIGHT);
        setTitle("Клиент");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Font font1 = new Font("Arial", 0, 18);

        //authentication
        authPanel = new JPanel(new GridLayout(2, 3));
        //authPanel.setMinimumSize(new Dimension(WINDOW_WIDTH, (int)(0.1 * WINDOW_HEIGHT)));//не работает
        //authPanel.setSize(new Dimension(WINDOW_WIDTH, (int)(0.25 * WINDOW_HEIGHT)));
        authPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, (int) (0.15 * WINDOW_HEIGHT)));
        JTextField loginField = new JTextField();
        loginField.setToolTipText("Введите логин уже");

        loginField.setFont(font1);
        JLabel label1 = new JLabel("Логин: ");
        label1.setFont(font1);
        //label1.setAlignmentX(CENTER_ALIGNMENT); //это неработает
        label1.setHorizontalAlignment(SwingConstants.CENTER); //а это работает!!)
        authPanel.add(label1);

        authPanel.add(loginField);

        JButton signInButton = new JButton("Войти в чат");
        signInButton.setFont(font1);

        authPanel.add(signInButton);


        JTextField passwordField = new JTextField();
        passwordField.setToolTipText("Пароль сюда на!");

        passwordField.setFont(font1);
        JLabel label2 = new JLabel("Пароль: ");
        label2.setFont(font1);
        label2.setHorizontalAlignment(SwingConstants.CENTER);

        authPanel.add(label2);
        authPanel.add(passwordField);
        add(authPanel, BorderLayout.NORTH);

        JButton registerButton = new JButton("Зарегистрироваться");
        registerButton.setFont(font1);

        authPanel.add(registerButton);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createRegistrationWindow(getX(), getY(), getWidth()/2, getHeight()/2);
            }
        });

        signInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendAuthCommand(loginField.getText(), passwordField.getText());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });


        //многострочный элемент для всех сообщений
        serverMsgElement = new JTextArea();
        serverMsgElement.setEditable(false);
        serverMsgElement.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(serverMsgElement);
        serverMsgElement.setFont(new Font("Arial", 1, 16));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, (int) (0.07 * WINDOW_HEIGHT)));
        add(bottomPanel, BorderLayout.SOUTH);

        //кнопка отправки сообщения
        JButton sendButton = new JButton("Отправить");
        sendButton.setFont(font1);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        clientMsgElement = new JTextField();
        clientMsgElement.setFont(font1);
        bottomPanel.add(clientMsgElement, BorderLayout.CENTER);

        //отправка по кнопке
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = clientMsgElement.getText();
                try {
                    sendMessage(message);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                clientMsgElement.grabFocus();
            }
        });

        //отправка по Enter
        clientMsgElement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = clientMsgElement.getText();
                try {
                    sendMessage(message);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    out.writeUTF("/end");
                    out.flush();
                    socket.close();
                    out.close();
                    in.close();
                } catch (IOException exc) {
                } finally {
                    System.exit(0);
                }
            }
        });

        setVisible(true);
    }

    private void sendAuthCommand(String login, String password) throws IOException {
        String command = "auth___" + login + "___" + password;
        out.writeUTF(command);
        out.flush();
    }

    private void initServerListener() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String message = in.readUTF();
                        if (message.equalsIgnoreCase("end session")) {
                            try {
                                out.writeUTF("/end");
                                out.flush();
                                socket.close();
                                out.close();
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        } else if (message.equalsIgnoreCase("signIn_success")) {
                            JOptionPane.showMessageDialog(null, "SingIn ok!");
                            authPanel.setVisible(false);
                        } else if (message.equalsIgnoreCase("signIn_fail")) {
                            JOptionPane.showMessageDialog(null, "SingIn fail! Try one more time");
                        } else
                            serverMsgElement.append(message + "\n");
                    }
                } catch (Exception e) {
                }
            }
        }).start();
    }

    public void sendMessage(String message) throws IOException {
        if (!message.trim().isEmpty()) {
            out.writeUTF(message);
            out.flush();
            clientMsgElement.setText("");
        }
    }

    private void createRegistrationWindow(int x, int y, int width, int height) {
        JFrame frame = new JFrame();

        Font font1 = new Font("Arial", 0, 18);

        frame.setBounds(x, y, width, height);
        frame.setLayout(new GridLayout(5, 2));

        JLabel nickLabel = new JLabel("Ник:");
        nickLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nickLabel.setFont(font1);
        frame.add(nickLabel);

        JTextField nickField = new JTextField();
        frame.add(nickField);

        JLabel loginLabel = new JLabel("Логин:");
        frame.add(loginLabel);

        JTextField loginField = new JTextField();
        frame.add(loginField);

        JLabel passLabel = new JLabel("Пароль:");
        frame.add(passLabel);

        JTextField passField = new JTextField();
        frame.add(passField);

        JLabel passAgainLabel = new JLabel("Пароль еще раз:");
        frame.add(passAgainLabel);

        JTextField passAgainField = new JTextField();
        frame.add(passAgainField);

        frame.setVisible(true);
    }
}
