package server.authservice;

import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
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
    //qua posso modificare il tipo di factory per polimorfismo
    private final IJsonLocInfoFactory locInfoFactory;
    private final IInputOutput ioService = new IOService();
    private final JsonDataLayer dataLayer;
    private final AuthenticationUtil authenticationUtil;


    public AuthenticationService(Socket socket, ConnectionType connectionType,
     IJsonLocInfoFactory locInfoFactory,
     JsonDataLayer dataLayer) {
        super(socket);
        this.dataLayer = dataLayer;
        this.connectionType = connectionType;
        this.locInfoFactory = locInfoFactory;
        this.authenticationUtil = new AuthenticationUtil(locInfoFactory,dataLayer);
    }

    @Override
    public User applyLogic() {
        User user;
        boolean temp;

        ioService.writeMessage(CLEAR, false);

        //se esiste ottengo l'utente
        JsonObject userJO = getUser();
        if (userJO == null) return null;

        if(userNotActive(userJO)){
            ioService.writeMessage("\nUtente non ancora attivo", false);
            return null;
        }

        String username = userJO.get("name").getAsString();
        String role = userJO.get("role").getAsString();

        if(!isTerminalCorrect(role)){
            return null;
        }

        if(authenticationUtil.checkIfTemp(username)){
            temp = true;
        }else{
            temp = false;
        }

        if(!obtainPassword(username, temp)){
            return null;
        }
       
        if (temp) {
            changePassword(username);
            ioService.writeMessage("\n\nPer favore fare nuovamente l'accesso\n\n", false);
            if(!obtainPassword(username, false)){
            return null;
            }
        }

        user = gson.fromJson(userJO, User.class);

        assert user != null : "User is null";
        return user;
    }

    /**
     * method to check if user is active
     * @param userJO
     * @return
     */
    private boolean userNotActive(JsonObject userJO) {
        return !userJO.get("active").getAsBoolean();
    }

    /**
     * method to obtain password, calls the verifyPassword method
     * @param username
     */
    private boolean obtainPassword(String username, boolean temp) {
        int i = 0;
        while(true) {
            String pass = ioService.readString("Inserisci password:");
            if (verifyPassword(username, pass, temp)) {
                break;
            }
            ioService.writeMessage(String.format("Password sbagliata riprovare, tentativi rimasti %d", 2 - i), false);
            if (i == 2) {
                ioService.writeMessage("Tentativi esauriti, connessione chiusa", false);
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return false;
            }
            i += 1;
        }
        return true;
    }

    private boolean isTerminalCorrect(String role) {
        boolean isCorrect = true;
        if((role.equals("configuratore") || role.equals("volontario"))&&connectionType==ConnectionType.External){
            ioService.writeMessage("Terminale non corretto per il ruolo configuratore o volontario", false);
            isCorrect = false;
        }
        if(role.equals("fruitore")&&connectionType==ConnectionType.Internal){
            ioService.writeMessage("Terminale non corretto per il ruolo fruitore", false);
            isCorrect = false;
        }
        return isCorrect;
    }

    private JsonObject getUser() {
        String username;
        String riprovare = ""; // n no y si
        do {
            username = ioService.readString("Inserisci username:");
            JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();
            
            locInfo.setKey(username);

            boolean exist = dataLayer.exists(locInfo);
            if(exist){
                JsonObject userJO = authenticationUtil.getUserJsonObject(username);
                assert userJO != null : "User is null";
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

        JsonObject userJO = authenticationUtil.getUserJsonObject(username);
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
        assert authenticationUtil.getUserJsonObject(username) != null : "cannot change password of invalid username";

        String newPassword = ioService.readString("\nPrimo accesso, cambio password necessario\n\nInserisci nuova password:");

        if (!newPassword.isEmpty()) {
            if (authenticationUtil.changePassword(username, newPassword)) {
                ioService.writeMessage("Password cambiata con successo", false);
            }

        } else {
            ioService.writeMessage("Errore nel cambiare la password", false);
        }
    }

    private boolean verifyPassword(String username, String password, boolean temp) {
    
        assert authenticationUtil.getUserJsonObject(username) != null : "cannot verify password of invalid username";
        assert !password.isEmpty() : "cannot verify password if empty";

        if (temp){
            return authenticationUtil.verifyClearPassword(username, password);
        }

        return authenticationUtil.verifyPassword(username, password);
       
    }

    

}
