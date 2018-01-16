package com.company.server;

import com.company.database.SQLHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private String name = "";

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            try {
                //Авторизация
                SQLHandler.connect();
                while (true) {
                    String str = in.readUTF();
                    if (str.startsWith("/auth")) {
                        String[] elements = str.split(" ");
                        if (elements.length == 3) {
                            if (SQLHandler.isLoginExist(elements[1])) {
                                if (!SQLHandler.isWrongPassword(elements[1], elements[2])) {
                                    //Логин и пароль правильные
                                    String name = SQLHandler.getNickByLoginPassword(elements[1], elements[2]);
                                    if (!server.isNickRegister(name)) {
                                        //Такого пользователя еще нет в чате
                                        this.name = name;
                                        sendMessage("/authok " + this.name);
                                        server.broadcastMsg(this.name + " is connected");
                                        server.broadcastUserList();
                                        break;
                                    } else {
                                        sendMessage("You already have registered");
                                    }
                                } else {
                                    sendMessage("Uncorrect password");
                                }
                            } else {
                                //Регистрация нового пользователя (требуется ввести ник)
                                sendMessage("/inputnick");
                            }
                        } else {
                            sendMessage("You need to login");
                        }
                    } else if (str.startsWith("/nick")) {
                        //Регистрация нового пользователя (ник введен, заносим нового юзера в БД, подключаем к чату)
                        String[] elements = str.split(" ");
                        SQLHandler.addUser(elements[3], elements[1], elements[2]);
                        sendMessage("/authok " + this.name);
                        name = elements[3];
                        server.broadcastMsg(this.name + " is connected");
                        server.broadcastUserList();
                        break;
                    } else {
                        sendMessage("You need to login");
                    }
                }
                SQLHandler.disconnect();
                while (true) {
                    String str = in.readUTF();
                    if (str.equalsIgnoreCase("/end")) {
                        break;
                    }
                    if (str.startsWith("/w")) {
                        //Частное сообщение конкретному пользователю AB(/w AB message)
                        String[] elements = str.split(" ");
                        String msg = str.substring(6);
                        server.sendToUser(this, elements[1], msg);
                        sendMessage("Private to " + elements[1] + ": " + msg);
                    } else {
                        //Все другие (обычные) сообщения
                        server.broadcastMsg(name + ": " + str);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                server.unsubscribeMe(this);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }
}
