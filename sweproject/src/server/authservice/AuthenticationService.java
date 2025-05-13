package server.authservice;

import java.io.*;
import java.net.Socket;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import server.datalayerservice.*;
import server.gsonfactoryservice.GsonFactoryService;
import server.gsonfactoryservice.IGsonFactory;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.utils.*;

public class AuthenticationService extends MainService<User> {
    private static final String CLEAR = "CLEAR";
    private static final String USERS_KEY_DESCRIPTION = "name";
    private static final String USERS_MEMBER_NAME = "users";
    private static final String USERS_PATH = "JF/users.json";

    private ConnectionType connectionType;
    private IGsonFactory gsonFactoryService = new GsonFactoryService();
    private final Gson gson = gsonFactoryService.getGson();

    private IInputOutput ioService = new IOService();


    public AuthenticationService(Socket socket, ConnectionType connectionType) {
        super(socket);
        this.connectionType = connectionType;
    }

    @Override
    public User applyLogic() throws IOException {
        String username;
        User user = null;
        String riprovare = "n"; // n no y si
        
        ioService.writeMessage(CLEAR, false);
        
        do{
            username = ioService.readString("Inserisci username:");
            JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
            locInfo.setPath(USERS_PATH);
            locInfo.setKeyDesc(USERS_KEY_DESCRIPTION);
            locInfo.setMemberName(USERS_MEMBER_NAME);
            locInfo.setKey(username);
            

            if(!DataLayerDispatcherService.startWithResult(locInfo, layer -> layer.exists(locInfo))){
                //DEBUG write(String.valueOf(dataLayer.exists(dataContainer)), false);
                riprovare = ioService.readString("Utente inesistente, vuoi riprovare? (y/n):");
                if(riprovare.equalsIgnoreCase("n")){
                    ioService.writeMessage("\n\n\nCONNESSIONE CHIUSA\n\n\n", false);
                    return null;
                }else{
                    riprovare = ioService.readString("\nComando non valido inserire 'n o 'y'");
                }
            }
        }while(riprovare.equals("y"));

        //se esiste ottengo l'utente
        JsonObject userJO = AuthenticationUtil.getUserJsonObject(username);
        
        if(AuthenticationUtil.checkIfTemp(username)){
            changePassword(username);
        }
            
        for(int i = 0; i < 3; i++){
            String pass = ioService.readString("Inserisci password:");
            if(verifyPassword(username, pass)){
                //write("DEBUG: calsse auth service line 42 password corretta", false);
                break;
            }
            ioService.writeMessage(String.format("Password sbagliata riprovare, tentativi rimasti %d",2-i), false);
            if(i == 2){
                ioService.writeMessage("Tentativi esauriti, connessione chiusa", false);
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
        
        String newPassword = ioService.readString("Inserisci nuova password:");
        
        if(!newPassword.equals("")){
            if(AuthenticationUtil.changePassword(username, newPassword)){
                ioService.writeMessage("Password cambiata con successo", false);}

        } else {
            ioService.writeMessage("Errore nel cambiare la password", false);
        }
    }

    private boolean verifyPassword(String username, String Password) {
            if(AuthenticationUtil.verifyPassword(username, Password)){
                return true;
            } else {
                ioService.writeMessage("Password errata", false);
                return false;
            }
    }

}
