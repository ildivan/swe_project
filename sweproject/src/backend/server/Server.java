package backend.server;// Server.java

import backend.server.services.ConfigService;
import backend.server.services.auth.AuthenticationSystem;

import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        int port = 5001;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                AuthenticationSystem login = new AuthenticationSystem(socket);
                login.start();
                try{
                    login.join();
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
                System.out.println("LOGIN COMPLETATO");
                //IF CONFIGURATORE RUN CONFIG THREAD
                //IF VOLONTARIO RUN VOLUNTEER THREAD
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}



