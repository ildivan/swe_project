package backend.server.services;

import java.io.*;
import java.net.Socket;

import backend.server.Server;
import com.google.gson.Gson;

public class ConfigService extends Service<Void>{

    public ConfigService(Socket socket, Gson gson){
        super(socket);
    }

    public Void applyLogic() throws IOException {

        startMenu();

        return null;
    }
                
    private void startMenu() {
        write("CONFIGURATORE",false);
    }
}
