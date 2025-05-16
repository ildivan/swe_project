package server.authservice;
import java.io.*;
import java.net.Socket;

import server.objects.*;
import server.datalayerservice.*;
import server.GsonFactoryService;
import server.IOService;

public class AuthenticationService extends Service<User> {
    private static final String CLEAR = "CLEAR";
    private ConnectionType connectionType;
    private static DataLayer dataLayer = new JSONDataManager(GsonFactoryService.getGson());

    public AuthenticationService(Socket socket, ConnectionType connectionType) {
        super(socket);
        this.connectionType = connectionType;
    }

    @Override
    public User applyLogic() throws IOException {
        String username;
        User user = null;
        String riprovare = "n"; // n no y si
        
        write(CLEAR, false);
        
        do{
            username = IOService.readString("Inserisci username:");

            DataContainer dataContainer = new DataContainer("JF/users.json", "users", username,"name");
            if(!dataLayer.exists(dataContainer)){
                //DEBUG write(String.valueOf(dataLayer.exists(dataContainer)), false);
                riprovare = IOService.readString("Utente inesistente, vuoi riprovare? (y/n):");
                if(riprovare.equalsIgnoreCase("n")){
                    write("\n\n\nCONNESSIONE CHIUSA\n\n\n", false);
                    return null;
                }
            }
        }while(riprovare.equals("y"));
        
        
        if(AuthenticationUtil.checkIfTemp(username)){
            changePassword(username);
        }
            
        for(int i = 0; i < 3; i++){
            write("Inserisci password:", true);
            String pass = read();
            if(verifyPassword(username, pass)){
                //write("DEBUG: calsse auth service line 42 password corretta", false);
                break;
            }
            write(String.format("Password sbagliata riprovare, tentativi rimasti %d",2-i), false);
            if(i == 2){
                write("Tentativi esauriti, connessione chiusa", false);
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
