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

                System.out.println(in.readLine());
                Scanner sc = new Scanner(System.in);
                userName = sc.nextLine();
                out.println(userName);

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

                String message;
                while (true) {
                    message = sc.nextLine();
                    out.println(message);
                }

            }catch (IOException e){
                System.out.println("cannot establish connection");
                System.exit(1);
            }
        }

        public static void main(String[] args) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter server's address: ");
            String name = sc.nextLine();
            System.out.println("Enter server's port: ");
            int port = sc.nextInt();
            sc.nextLine();

            MyClient client = new MyClient(name, port);
            client.start();
        }
    }
