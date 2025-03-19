package backend.server;// Server.java

import backend.server.domainlevel.PlanManager;
import backend.server.domainlevel.User;
import backend.server.domainlevel.domainservices.ConfigService;
import backend.server.domainlevel.domainservices.Service;
import backend.server.genericservices.auth.AuthenticationService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private final int ClientPort;
    private final int ServerTerminalPort;
    private final Gson gson;
    private PlanManager activityManager;

    public Server(int ClientPort, int ServerTerminalPort){
        this.ClientPort = ClientPort;
        this.ServerTerminalPort = ServerTerminalPort;
        this.gson = new Gson();
        this.activityManager = new PlanManager();
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
                        Thread serviceThread = new Thread(() -> {
                            try {
                                // Ottieni il servizio associato all'utente e alla connessione
                                Service<?> s = obtainService(u, socket);
                                s.run();  // Esegui il servizio
                            } finally {
                                try {
                                    socket.close();  // Assicurati di chiudere il socket alla fine
                                } catch (IOException e) {
                                    System.out.println("Error closing socket: " + e.getMessage());
                                }
                            }
                        });
            
                        // Avvia il thread che gestisce il servizio
                        serviceThread.start();
                        
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
                return new ConfigService(socket,gson,activityManager);
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
        String currentDir = System.getProperty("user.dir");
        System.out.println("La directory di lavoro corrente è: " + currentDir);
        // try (Reader reader = Files.newBufferedReader(Paths.get("/Users/riccardomodina/Documents/GitHub/swe_project/sweproject/JF/users.json"))) {
        //     Gson gson = new Gson();
        //     JsonObject json = gson.fromJson(reader, JsonObject.class);
        //     JsonArray objectArray = json.getAsJsonArray("users");
            
        //     if(objectArray == null){
        //         System.out.println("Errore");
        //     }

        //     List<JsonObject> list = new ArrayList<>();
        //     for (JsonElement elem : objectArray) {
        //         list.add(elem.getAsJsonObject());
        //     }
        //     System.out.println(list);
        // } catch (IOException e) {
        //     e.printStackTrace();
            
        // }

        Server s = new Server(5001,6001);
        s.startServer();
    }

}



