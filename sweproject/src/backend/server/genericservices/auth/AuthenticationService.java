package backend.server.genericservices.auth;
import backend.server.ConnectionType;
import backend.server.domainlevel.User;
import backend.server.genericservices.Service;
import backend.server.genericservices.DataLayer.JSONDataContainer;
import backend.server.genericservices.DataLayer.DataLayer;
import backend.server.genericservices.DataLayer.JSONDataManager;


import java.io.*;
import java.net.Socket;

public class AuthenticationService extends Service<User> {
    private ConnectionType connectionType;
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

        JSONDataContainer dataContainer = new JSONDataContainer("JF/users.json", "users", username,"name");
        if(!dataLayer.exists(dataContainer)){
            write(String.valueOf(dataLayer.exists(dataContainer)), false);
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
        JSONDataContainer dataContainer1 = new JSONDataContainer("JF/users.json", "users", username, "name");
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
