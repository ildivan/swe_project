package backend.server.services.auth;
import backend.server.ConnectionType;
import backend.server.json.JSONDataManager;
import backend.server.objects.Message;
import backend.server.objects.User;
import backend.server.services.Service;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

public class AuthenticationService extends Service<User> {

    private final ConnectionType connectionType;

    public AuthenticationService(Socket socket, Gson gson, ConnectionType connectionType) {
        super(socket, gson);
        this.connectionType = connectionType;
    }

    @Override
    public User applyLogic() throws IOException {
        String username;
        boolean exists = false;
        User user;
        
        write("Inserisci username:", true);
        username = read();
        
        if(!JSONDataManager.exists(username, "name", "sweproject/JsonFIles/users.json", "users")){
            write("Utente inesistente, connessione chiusa", false);
            return null;
        }else{
            checkIfTemp(username);
            
        }
            
       
        
        
        
        write("Inserisci password:", true);
        String pass = read();
        //CHECK PASSWORD
        write("GRAZIE", false);
        return "";
    }

    
}
