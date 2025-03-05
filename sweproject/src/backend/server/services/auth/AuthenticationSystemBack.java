package backend.server.services.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputFilter.Config;

import backend.server.Server;
import backend.server.services.ConfigService;

public class AuthenticationSystemBack implements Runnable {

    public AuthenticationSystemBack() {
        super();
    }
    public void run () {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {

            Server.output("Inserisci username:");
            String username = reader.readLine();

            Server.output("Inserisci password:");
            String pass = reader.readLine();

            System.out.println(username);
            System.out.println(pass);

            //solo per test chiamo un metodo di configservice
            ConfigService.applyLogic();
            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
}