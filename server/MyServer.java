package server;

import client.ServerThread;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyServer {

    private ServerSocket ss;
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
        if(Objects.equals(message, "!users")){
            String temp = "";
            for (ServerThread user : users){
                temp += user.getNickname() + " ";
            }
            sender.sendMessage(temp);
        }else if(message.charAt(0) == '@'){
            Pattern pattern = Pattern.compile("@([^,\\s]+)(?:,\\s*|\\s*)*|(\\S.*)");
            Matcher matcher = pattern.matcher(message);

            Set<String> tagged = new HashSet<>();
            String msg = "";

            while (matcher.find()) {
                if (matcher.group(1) != null) {
                    tagged.add(matcher.group(1));
                } else if (matcher.group(2) != null) {
                    msg = matcher.group(2);
                }
            }

            for(ServerThread user: users){
                if(tagged.contains(user.getNickname())){
                    user.sendMessage("(" + sender.getNickname() + "): " + msg);
                }
            }

        }else{
            for (ServerThread user: users){
                if(user != sender) {
                    user.sendMessage("(" + sender.getNickname() + "): " + message);
                }
            }
        }
    }

    public void joinChat(String message, ServerThread sender){
        for (ServerThread user: users){
            if(user != sender) {
                user.sendMessage(message);
            }
        }
    }

    public static void main(String[] args) {
        MyServer server = new MyServer();
        server.start();
    }

}
