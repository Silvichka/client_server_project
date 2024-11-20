package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyServer {

    private ServerSocket ss;
    private String configPath;

    private String serverName;
    private int port;
    private String rules;
    private Set<String> banned;

    private Set<ServerThread> users = new HashSet<>();

    public MyServer(String configPath) {
        this.configPath = configPath;
        banned = new HashSet<>();
    }

    public void start(){
        try{
            loadConfig();
            ss = new ServerSocket(port);

            while(true){
                Socket socket = ss.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Enter your nickname: ");
                String nickname = "";
                boolean uniquie = false;
                while(!uniquie){
                    nickname = in.readLine();
                    try{
                        if(!getUsersNames().isEmpty() && getUsersNames().contains(nickname.toLowerCase())){
                            out.println(400);
                            System.out.println("This nick is taken");
                            out.println("This nickname is taken, enter different one: ");
                        }else {
                            out.println(200);
                            System.out.println("New user: " + nickname);
                            uniquie = true;
                            ServerThread serverThread = new ServerThread(socket, this, nickname);
                            new Thread(serverThread).start();
                            users.add(serverThread);
                        }
                    }catch (NullPointerException e){
                        System.exit(-1);
                    }
                }
            }

        }catch (IOException e){
            System.exit(1);
        }
    }

    public void broadcast(String message, ServerThread sender) throws IOException {
        if(Objects.equals(message, "!users")){
            String temp = "";
            for (ServerThread user : users){
                temp += user.getNickname() + " ";
            }
            sender.sendMessage("(server): " + temp);
        }else if(Objects.equals(message, "!banned")){
            sender.sendMessage(banned.toString());
        }else if(Objects.equals(message, "!close")){
            for(ServerThread user: users){
                if(user != sender){
                    user.sendMessage(sender.getNickname() + " has disconnected");
                }
            }
            System.out.println(sender.getNickname() + " has disconnected");
            users.remove(sender);
        }else if (message.charAt(0) == '@'){
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

        }else if(message.charAt(0) == '/') {
            Pattern pattern = Pattern.compile("/([^,\\s]+)(?:,\\s*|\\s*)*|(\\S.*)");
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
                if(!tagged.contains(user.getNickname()) && user != sender){
                    user.sendMessage("(" + sender.getNickname() + "): " + msg);
                }
            }
        }else{
            String[] words = message.split("\\W+");
            boolean ban = false;
            for (String w: words) {
                if(banned.contains(w)){
                    sender.sendMessage("(server): This word is not allowed!!!");
                    ban = true;
                    break;
                }
            }
            if(!ban){
                for (ServerThread user: users){
                    if(user != sender) {
                        user.sendMessage("(" + sender.getNickname() + "): " + message);
                    }
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

    private void loadConfig() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(configPath));
        String line;
        StringBuilder rulesBuilder = new StringBuilder();

        boolean inRules = false;
        boolean inBanned = false;

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (this.serverName == null && !line.isEmpty() && !line.matches("\\d+")) {
                this.serverName = line;
                continue;
            }

            if (this.port == 0 && line.matches("\\d+")) {
                this.port = Integer.parseInt(line);
                continue;
            }

            if (line.equalsIgnoreCase("rules:")) {
                inRules = true;
                continue;
            }
            if (inRules) {
                if (line.equalsIgnoreCase("end;")) {
                    inRules = false;
                    continue;
                }
                rulesBuilder.append(line).append("\n");
                continue;
            }

            if (line.equalsIgnoreCase("banned:")) {
                inBanned = true;
                continue;
            }
            if (inBanned) {
                if (line.equalsIgnoreCase("end;")) {
                    inBanned = false;
                    continue;
                }
                this.banned.add(line.replace(",", "").trim());
            }
        }
        reader.close();

        this.rules = rulesBuilder.toString().trim();
    }

    public Set<String> getBanned() {
        return banned;
    }

    public String getServerName() {
        return serverName;
    }

    public String getRules(){
        return rules;
    }

    public Set<ServerThread> getUsers(){
        return users;
    }

    public Set<String> getUsersNames(){
        Set<String> us = new HashSet<>();
        for(ServerThread user: users){
            us.add(user.getNickname().toLowerCase());
        }
        return us;
    }

    public static void main(String[] args) {
        MyServer server = new MyServer("/Users/silvia/JavaProjects/client_server_project/src/server/.configuration.txt");
        server.start();
    }

}
