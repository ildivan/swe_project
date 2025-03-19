package backend.server.genericservices.auth;
import backend.server.ConnectionType;
import backend.server.domainlevel.User;
import backend.server.domainlevel.domainservices.Service;
import backend.server.genericservices.Message;
import backend.server.genericservices.DataLayer.DataContainer;
import backend.server.genericservices.DataLayer.DataLayer;
import backend.server.genericservices.DataLayer.JSONDataManager;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

public class AuthenticationService extends Service<User> {

    private final ConnectionType connectionType;
    private static DataLayer dataLayer = new JSONDataManager();

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

        DataContainer dataContainer = new DataContainer("sweproject/JsonFIles/users.json", "users", "name", username);
        if(!dataLayer.exists(dataContainer)){
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
        DataContainer dataContainer1 = new DataContainer("sweproject/JsonFIles/users.json", "users", "name", username);
        user = gson.fromJson(dataLayer.get(dataContainer1), User.class);
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
