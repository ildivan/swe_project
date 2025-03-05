package backend.server;// Server.java

import backend.server.services.auth.AuthenticationSystemFront;

import java.io.*;
import java.net.*;

public class Server {

    private final int ClientPort;
    private final int ServerTerminalPort;

    public Server(int ClientPort, int ServerTerminalPort){
        this.ClientPort = ClientPort;
        this.ServerTerminalPort = ServerTerminalPort;
    }

    public void startServer(){
        try (ServerSocket clientSS = new ServerSocket(ClientPort);
             ServerSocket serverTerminalSS = new ServerSocket(ServerTerminalPort)) {

            System.out.println("Server is listening on port " + ClientPort);

            Thread backInt = new Thread(() -> {
                try {
                    while(true) {
                        Socket socket = serverTerminalSS.accept();
                        System.out.println("Internal Connection");
                        authenticate(socket,ConnectionType.Internal);
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println(e.getMessage());
                    throw new RuntimeException(e);
                }
            });
            backInt.start();

            while (true) {
                Socket socket = clientSS.accept();
                System.out.println("External Connection");
                authenticate(socket,ConnectionType.External);
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        } catch(InterruptedException e) {
            System.out.println("Server interrupted: " + e.getMessage());
        }
    }

    private static void authenticate(Socket socket, ConnectionType connectionType)
            throws InterruptedException, IOException {
        AuthenticationSystemFront login = new AuthenticationSystemFront(socket, ConnectionType.Internal);
        login.start();
        login.join();
    }

    public static void output(String message){
        System.out.println(message);
    }

    public static void main(String[] args) {
        Server s = new Server(5001,6001);
        s.startServer();
    }

}



