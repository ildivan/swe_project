package server.authservice;

import java.io.*;
import java.net.Socket;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import server.objects.*;
import server.datalayerservice.*;
import server.ioservice.IOService;
import server.GsonFactoryService;

public class AuthenticationService extends Service<User> {
    private static final String CLEAR = "CLEAR";
    private static final String USERS_KEY_DESCRIPTION = "name";
    private static final String USERS_MEMBER_NAME = "users";
    private static final String USERS_PATH = "JF/users.json";

    private ConnectionType connectionType;
    private static final Gson gson = (Gson) GsonFactoryService.Service.GET_GSON.start();


    public AuthenticationService(Socket socket, ConnectionType connectionType) {
        super(socket);
        this.connectionType = connectionType;
    }

    @Override
    public User applyLogic() throws IOException {
        String username;
        User user = null;
        String riprovare = "n"; // n no y si
        
        IOService.Service.WRITE.start(CLEAR, false);
        
        do{
            username = (String) IOService.Service.READ_STRING.start("Inserisci username:");
            JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
            locInfo.setPath(USERS_PATH);
            locInfo.setKeyDesc(USERS_KEY_DESCRIPTION);
            locInfo.setMemberName(USERS_MEMBER_NAME);
            locInfo.setKey(username);
            

            if(!DataLayerDispatcherService.startWithResult(locInfo, layer -> layer.exists(locInfo))){
                //DEBUG write(String.valueOf(dataLayer.exists(dataContainer)), false);
                riprovare = (String) IOService.Service.READ_STRING.start("Utente inesistente, vuoi riprovare? (y/n):");
                if(riprovare.equalsIgnoreCase("n")){
                    IOService.Service.WRITE.start("\n\n\nCONNESSIONE CHIUSA\n\n\n", false);
                    return null;
                }
            }
        }while(riprovare.equals("y"));

        //se esiste ottengo l'utente
        JsonObject userJO = AuthenticationUtil.getUserJsonObject(username);
        
        if(AuthenticationUtil.checkIfTemp(username)){
            changePassword(username);
        }
            
        for(int i = 0; i < 3; i++){
            String pass = (String) IOService.Service.READ_STRING.start("Inserisci password:");
            if(verifyPassword(username, pass)){
                //write("DEBUG: calsse auth service line 42 password corretta", false);
                break;
            }
            IOService.Service.WRITE.start(String.format("Password sbagliata riprovare, tentativi rimasti %d",2-i), false);
            if(i == 2){
                IOService.Service.WRITE.start("Tentativi esauriti, connessione chiusa", false);
                return null;
            }
        }

        user = gson.fromJson(userJO, User.class);
        return user;
    }
            

    /**
     * metodo per ottenere la nuova password e cambiarla
     * @param username
     */
    private void changePassword(String username) {
        
        String newPassword = (String) IOService.Service.READ_STRING.start("Inserisci nuova password:", true);;
        
        if(!newPassword.equals("")){
            if(AuthenticationUtil.changePassword(username, newPassword)){
                IOService.Service.WRITE.start("Password cambiata con successo", false);}

        } else {
            IOService.Service.WRITE.start("Errore nel cambiare la password", false);
        }
    }

    private boolean verifyPassword(String username, String Password) {
            if(AuthenticationUtil.verifyPassword(username, Password)){
                return true;
            } else {
                IOService.Service.WRITE.start("Password errata", false);
                return false;
            }
    }

}
