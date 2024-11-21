package client;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

public class MyClient {

    private String serverName;
    private int serverPort;
    private String userName;

    public MyClient(String userName, int serverPort) {
        this.serverPort = serverPort;
        this.userName = userName;
    }

    public void start(){
        try(Socket socket = new Socket(serverName, serverPort)){
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            Scanner sc = new Scanner(System.in);
            String message;
            while (true){
                System.out.println(in.readLine());
                userName = sc.nextLine();
                out.println(userName);
                if(Integer.parseInt(in.readLine()) == 200){
                    System.out.println("Enjoy");
                    break;
                }
            }

            System.out.println(in.readLine());

            Thread readThread = new Thread(() -> {
                try {
                    String input;
                    while ((input = in.readLine()) != null) {
                        System.out.println(input);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed by the server.");
                }
            });
            readThread.start();

            while (true) {
                message = sc.nextLine();
                if(Objects.equals(message, "!close")){
                    out.println(message);
                    exitChat();
                }
                if(!message.isEmpty()) out.println(message);
            }

        }catch (IOException e){
            System.out.println("cannot establish connection");
            System.exit(1);
        }
    }

    public void exitChat(){
        System.exit(-1);
    }

    public static void main(String[] args) {
        MyClient client = new MyClient("localhost", 4892);
        client.start();
    }
}
