package backend.server;// Server.java

import backend.server.services.ConfigService;
import backend.server.services.auth.AuthenticationService;
import com.google.gson.Gson;
import backend.server.json.objects.User;

import java.io.*;
import java.net.*;

public class Server {

    private final int ClientPort;
    private final int ServerTerminalPort;
    private final Gson gson;

    public Server(int ClientPort, int ServerTerminalPort){
        this.ClientPort = ClientPort;
        this.ServerTerminalPort = ServerTerminalPort;
        this.gson = new Gson();
    }

    public void startServer(){
        try (ServerSocket clientSS = new ServerSocket(ClientPort);
             ServerSocket serverTerminalSS = new ServerSocket(ServerTerminalPort)) {

            System.out.println("Server is listening on port " + ClientPort);
            System.out.println("Server is listening on port " + ServerTerminalPort);

            Thread internalConnectionThread = new Thread(() -> {
                try {
                    while(true) {
                        Socket socket = serverTerminalSS.accept();
                        System.out.println("Internal Connection");
                        User u = authenticate(socket,ConnectionType.Internal);
                        if(u == null){
                            socket.close();
                            continue;
                        }
                        switch (u.getRole()){
                            case "configuratore":
                                System.out.println("Configuratore");
                                new ConfigService(socket,gson).run();
                                break;
                            case "volontario":
                                System.out.println("Volontario");
                                break;
                        }
                        socket.close();
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println(e.getMessage());
                    throw new RuntimeException(e);
                }
            });
            internalConnectionThread.start();

            while (true) {
                Socket socket = clientSS.accept();
                System.out.println("External Connection"); //è sicuramente un fruitore da implementare
                authenticate(socket,ConnectionType.External);
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        } catch(InterruptedException e) {
            System.out.println("Server interrupted: " + e.getMessage());
        }
    }

    private User authenticate(Socket socket, ConnectionType connectionType)
            throws InterruptedException, IOException {
        AuthenticationService login = new AuthenticationService(socket, connectionType);
        return login.run();
    }

    public static void output(String message){
        System.out.println(message);
    }

    public static void main(String[] args) {
        Server s = new Server(5001,6001);
        s.startServer();
    }

}



