package server.authservice;

import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import server.datalayerservice.*;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.gsonfactoryservice.GsonFactoryService;
import server.gsonfactoryservice.IGsonFactory;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.utils.*;

public class AuthenticationService extends MainService<User> {
    private static final String CLEAR = "CLEAR";

    private final ConnectionType connectionType;
    private final IGsonFactory gsonFactoryService = new GsonFactoryService();
    private final Gson gson = gsonFactoryService.getGson();
    private final ILocInfoFactory locInfoFactory= new JsonLocInfoFactory();
    private final IInputOutput ioService = new IOService();


    public AuthenticationService(Socket socket, ConnectionType connectionType) {
        super(socket);
        this.connectionType = connectionType;
    }

    @Override
    public User applyLogic() {
        User user;
        

        ioService.writeMessage(CLEAR, false);

        //se esiste ottengo l'utente
        JsonObject userJO = getUser();
        if (userJO == null) return null;

        String username = userJO.get("name").getAsString();

        if (AuthenticationUtil.checkIfTemp(username)) {
            changePassword(username);
        }

        int i = 0;
        while(true) {
            String pass = ioService.readString("Inserisci password:");
            if (verifyPassword(username, pass)) {
                //write("DEBUG: calsse auth service line 42 password corretta", false);
                break;
            }
            ioService.writeMessage(String.format("Password sbagliata riprovare, tentativi rimasti %d", 2 - i), false);
            if (i == 2) {
                ioService.writeMessage("Tentativi esauriti, connessione chiusa", false);
                return null;
            }
            i += 1;
        }

        user = gson.fromJson(userJO, User.class);

        assert user != null : "User is null";
        return user;
    }

    private JsonObject getUser() {
        String username;
        String riprovare = ""; // n no y si
        do {
            username = ioService.readString("Inserisci username:");
            JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();
            
            locInfo.setKey(username);

            boolean exist = DataLayerDispatcherService.startWithResult(locInfo, layer -> layer.exists(locInfo));
            if(exist){
                JsonObject userJO = AuthenticationUtil.getUserJsonObject(username);
                assert userJO != null : "User is null";
                 assert (
                    (userJO.get("role").getAsString().equals("configuratore") && connectionType == ConnectionType.Internal) ||
                    (userJO.get("role").getAsString().equals("volontario") && connectionType == ConnectionType.Internal) ||
                    (userJO.get("role").getAsString().equals("fruitore") && connectionType == ConnectionType.External)
                 );
                return userJO;
                
            }
                //DEBUG write(String.valueOf(dataLayer.exists(dataContainer)), false);
                riprovare = ioService.readString("Utente inesistente, vuoi riprovare? (y/n):");
                if (!riprovare.equalsIgnoreCase("y") && !riprovare.equalsIgnoreCase("n")){
                    riprovare = ioService.readString("\nComando non valido inserire 'n' o 'y'");
                }
                if (riprovare.equalsIgnoreCase("n")) {
                    ioService.writeMessage("\n\n\nCONNESSIONE CHIUSA\n\n\n", false);
                    return null;
                }


            
        } while (riprovare.equalsIgnoreCase("y"));

        JsonObject userJO = AuthenticationUtil.getUserJsonObject(username);
        assert userJO != null : "User is null";
        assert (
                (userJO.get("role").getAsString().equals("configuratore") && connectionType == ConnectionType.Internal) ||
                (userJO.get("role").getAsString().equals("volontario") && connectionType == ConnectionType.Internal) ||
                (userJO.get("role").getAsString().equals("fruitore") && connectionType == ConnectionType.External)
        );
        return userJO;
    }


    /**
     * metodo per ottenere la nuova password e cambiarla
     *
     * @param username username
     */
    private void changePassword(String username) {
        assert AuthenticationUtil.getUserJsonObject(username) != null : "cannot change password of invalid username";

        String newPassword = ioService.readString("Inserisci nuova password:");

        if (!newPassword.isEmpty()) {
            if (AuthenticationUtil.changePassword(username, newPassword)) {
                ioService.writeMessage("Password cambiata con successo", false);
            }

        } else {
            ioService.writeMessage("Errore nel cambiare la password", false);
        }
    }

    private boolean verifyPassword(String username, String password) {
        assert AuthenticationUtil.getUserJsonObject(username) != null : "cannot verify password of invalid username";
        assert !password.isEmpty() : "cannot verify password if empty";

        if (AuthenticationUtil.verifyPassword(username, password)) {
            return true;
        } else {
            ioService.writeMessage("Password errata", false);
            return false;
        }
    }

}
