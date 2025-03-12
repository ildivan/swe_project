package backend.server;// Server.java

import backend.server.services.ConfigService;
import backend.server.services.Service;
import backend.server.services.UserService;
import backend.server.services.VolunteerService;
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

                        //pattern solid delle inteerfacce per generalizzare e non dover riscrivere 
                        //il codice se aggiungo unnuovo tipo di utente 
                        Service<?> s = obtainService(u, socket);
                        s.run();
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

    private Service<?> obtainService(User u, Socket socket){
        switch (u.getRole()){
            case "configuratore":
                return new ConfigService(socket,gson);
            case "volontario":
                //return new VolunteerService(socket,gson);
            case "fruitore":
                //return new UserService(socket,gson);
            default:
                return null;
        }
    }
    public static void output(String message){
        System.out.println(message);
    }

    public static void main(String[] args) {
        Server s = new Server(5001,6001);
        s.startServer();
    }

}



