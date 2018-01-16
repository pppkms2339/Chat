package com.company.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private final int PORT = 8189;
    private Vector<ClientHandler> clients;

    public Server() {
        ServerSocket server = null;
        Socket socket = null;
        clients = new Vector<>();
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server is running, waiting for clients");
            while (true) {
                socket = server.accept();
                subscribeMe(new ClientHandler(socket, this));
                System.out.println("Client is connected");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to start sever");
        } finally {
            try {
                socket.close();
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void subscribeMe(ClientHandler c) {
        clients.add(c);
    }

    public void unsubscribeMe(ClientHandler c) {
        clients.remove(c);
        broadcastUserList();
        broadcastMsg(c.getName() + " is disconnected");
        System.out.println("Client is disconnected");
    }

    public void broadcastMsg(String msg) {
        for (ClientHandler c : clients) {
            c.sendMessage(msg);
        }
    }

    public boolean isNickRegister(String nick) {
        for (ClientHandler c : clients) {
            if (c.getName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void sendToUser(ClientHandler from, String to, String msg) {
        for (ClientHandler c : clients) {
            if (c.getName().equals(to)) {
                c.sendMessage("Private from " + from.getName() + ": " + msg);
            }
        }
    }

    public void broadcastUserList() {
        StringBuffer sb = new StringBuffer("/userlist");
        for (ClientHandler c : clients) {
            sb.append(" " + c.getName());
        }
        broadcastMsg(sb.toString());
    }
}
