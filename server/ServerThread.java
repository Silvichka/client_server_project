package server;

import java.io.*;
import java.net.Socket;

//!users !banned @for_specific \exclude_from_broadcasting /quit

public class ServerThread extends Thread{
    private Socket socket;
    private MyServer server;
    private String nickname;
    BufferedReader in;
    PrintWriter out;

    public ServerThread(Socket socket, MyServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Enter your nickname: ");
            nickname = in.readLine();
            out.println(server.getRules());
            System.out.println(nickname + " has joined the chat");
            server.joinChat(nickname + " has joined the chat", this);

            String message;
            while((message = in.readLine()) != null){
                String new_message = message;
                System.out.println(new_message);
                server.broadcast(new_message, this);
            }

        }catch(IOException e){
            System.exit(1);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getNickname(){
        return nickname;
    }
}
