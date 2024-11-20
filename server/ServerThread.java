package server;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread{
    private Socket socket;
    private MyServer server;
    private String nickname;
    private int port;
    private BufferedReader in;
    private PrintWriter out;

    public ServerThread(Socket socket, MyServer server, String nickname) {
        this.socket = socket;
        this.server = server;
        this.nickname = nickname;
        port = socket.getPort();
    }

    @Override
    public void run() {
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(server.getRules());
            server.joinChat(nickname + " has joined the chat", this);

            String message;
            while((message = in.readLine()) != null){
                String new_message = message;
                System.out.println("(" + nickname + "):" + new_message);
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
