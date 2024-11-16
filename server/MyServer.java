package server;

import client.ServerThread;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class MyServer {

    private ServerSocket ss;
    private String userName;
    private Set<ServerThread> users = new HashSet<>();

    public void start(){
        try{
            ss = new ServerSocket(1111);

            while(true){
                Socket socket = ss.accept();
//                System.out.println("New user: " + socket);//DEBUG
                ServerThread serverThread = new ServerThread(socket, this);
                users.add(serverThread);
                new Thread(serverThread).start();
            }

        }catch (IOException e){
            System.exit(1);
        }
    }

    public void broadcast(String message, ServerThread sender) {
        for (ServerThread user : users) {
            if(user != sender) user.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        MyServer server = new MyServer();
        server.start();
    }

}
