package backend.server.services.auth;
import backend.server.ConnectionType;
import backend.server.json.JSONDataManager;
import backend.server.json.objects.Message;
import backend.server.json.objects.User;
import backend.server.services.Service;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

public class AuthenticationService extends Service<User> {

    private final ConnectionType connectionType;

    public AuthenticationService(Socket socket, ConnectionType connectionType) {
        super(socket);
        this.connectionType = connectionType;
    }

    @Override
    public User applyLogic() throws IOException {
        String username;
        User user = null;
        
        write("Inserisci username:", true);
        username = read();
        
        if(!JSONDataManager.exists(username, "name", "sweproject/JsonFIles/users.json", "users")){
            write("Utente inesistente, connessione chiusa", false);
            return null;
        }
        
        if(AuthenticationUtil.checkIfTemp(username)){
            changePassword(username);
        }
            
        for(int i = 0; i < 3; i++){
            write("Inserisci password:", true);
            String pass = read();
            if(verifyPassword(username, pass)){
                write("DEBUG: calsse auth service line 42 password corretta", false);
                break;
            }
            write(String.format("Password sbagliata riprovare, tentativi rimasti %d",2-i), false);
            if(i == 2){
                write("Tentativi esauriti, connessione chiusa", false);
                return null;
            }
        }

        user = gson.fromJson(JSONDataManager.get(username, "name", "sweproject/JsonFIles/users.json", "users"), User.class);
        return user;
    }
            

    /**
     * metodo per ottenere la nuova password e cambiarla
     * @param username
     */
    private void changePassword(String username) {
        write("Inserisci nuova password:", true);
        String newPassword = "";
        try {
            newPassword = read();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(!newPassword.equals("")){
            if(AuthenticationUtil.changePassword(username, newPassword)){
            write("Password cambiata con successo", false);}

        } else {
            write("Errore nel cambiare la password", false);
        }
    }

    private boolean verifyPassword(String username, String Password) {
            if(AuthenticationUtil.verifyPassword(username, Password)){
                return true;
            } else {
                write("Password errata", false);
                return false;
            }
    }
}
