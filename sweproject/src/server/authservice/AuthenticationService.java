package server.authservice;
import java.io.*;
import java.net.Socket;

import com.google.gson.Gson;

import server.objects.*;
import server.datalayerservice.*;
import server.ioservice.IOServiceWithCommandLine;
import server.GsonFactoryService;

public class AuthenticationService extends Service<User> {
    private static final String CLEAR = "CLEAR";
    private ConnectionType connectionType;
    private static final Gson gson = (Gson) GsonFactoryService.Service.GET_GSON.start();
    private static DataLayer dataLayer = new JSONDataManager(gson);

    public AuthenticationService(Socket socket, ConnectionType connectionType) {
        super(socket);
        this.connectionType = connectionType;
    }

    @Override
    public User applyLogic() throws IOException {
        String username;
        User user = null;
        String riprovare = "n"; // n no y si
        
        IOServiceWithCommandLine.Service.WRITE.start(CLEAR, false);
        
        do{
            username = (String) IOServiceWithCommandLine.Service.READ_STRING.start("Inserisci username:");

            DataContainer dataContainer = new DataContainer("JF/users.json", "users", username,"name");
            if(!dataLayer.exists(dataContainer)){
                //DEBUG write(String.valueOf(dataLayer.exists(dataContainer)), false);
                riprovare = (String) IOServiceWithCommandLine.Service.READ_STRING.start("Utente inesistente, vuoi riprovare? (y/n):");
                if(riprovare.equalsIgnoreCase("n")){
                    IOServiceWithCommandLine.Service.WRITE.start("\n\n\nCONNESSIONE CHIUSA\n\n\n", false);
                    return null;
                }
            }
        }while(riprovare.equals("y"));
        
        
        if(AuthenticationUtil.checkIfTemp(username)){
            changePassword(username);
        }
            
        for(int i = 0; i < 3; i++){
            String pass = (String) IOServiceWithCommandLine.Service.READ_STRING.start("Inserisci password:");
            if(verifyPassword(username, pass)){
                //write("DEBUG: calsse auth service line 42 password corretta", false);
                break;
            }
            IOServiceWithCommandLine.Service.WRITE.start(String.format("Password sbagliata riprovare, tentativi rimasti %d",2-i), false);
            if(i == 2){
                IOServiceWithCommandLine.Service.WRITE.start("Tentativi esauriti, connessione chiusa", false);
                return null;
            }
        }
        DataContainer dataContainer1 = new DataContainer("JF/users.json", "users", username, "name");
        user = gson.fromJson(dataLayer.get(dataContainer1), User.class);
        return user;
    }
            

    /**
     * metodo per ottenere la nuova password e cambiarla
     * @param username
     */
    private void changePassword(String username) {
        
        String newPassword = (String) IOServiceWithCommandLine.Service.READ_STRING.start("Inserisci nuova password:", true);;
        
        if(!newPassword.equals("")){
            if(AuthenticationUtil.changePassword(username, newPassword)){
                IOServiceWithCommandLine.Service.WRITE.start("Password cambiata con successo", false);}

        } else {
            IOServiceWithCommandLine.Service.WRITE.start("Errore nel cambiare la password", false);
        }
    }

    private boolean verifyPassword(String username, String Password) {
            if(AuthenticationUtil.verifyPassword(username, Password)){
                return true;
            } else {
                IOServiceWithCommandLine.Service.WRITE.start("Password errata", false);
                return false;
            }
    }

}
