package backend.server.services;

import java.io.*;
import java.net.Socket;

import backend.server.Server;

public class ConfigService {

    public ConfigService(){
        super();
    }

    public static void applyLogic() throws IOException {
        
        //metodo test
        startMenu();
                
    }
                
    private static void startMenu() {
        Server.output("configuratore");
    }
}
