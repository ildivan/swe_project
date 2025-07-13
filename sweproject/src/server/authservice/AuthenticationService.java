package server.authservice;

import java.net.Socket;

import server.data.facade.FacadeHub;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.utils.*;

public class AuthenticationService extends MainService<User> {
    private static final String CLEAR = "CLEAR";

    private final ConnectionType connectionType;
    private final IInputOutput ioService = new IOService();
    private final AuthenticationUtil authenticationUtil;
    private final FacadeHub data;


    public AuthenticationService(Socket socket, ConnectionType connectionType, FacadeHub data) {
        super(socket);
        this.connectionType = connectionType;
        this.authenticationUtil = new AuthenticationUtil(data);
        this.data = data;
    }

    @Override
    public User applyLogic() {
        boolean temp;

        ioService.writeMessage(CLEAR, false);

        //se esiste ottengo l'utente
        User user = getUser();
        if (user == null) return null;

        if(userNotActive(user)){
            ioService.writeMessage("\nUtente non ancora attivo", false);
            return null;
        }

        String username = user.getName();
        String role = user.getRole();

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

        assert user != null : "User is null";
        return user;
    }

    /**
     * method to check if user is active
     * @param userJO
     * @return
     */
    private boolean userNotActive(User user) {
        return !user.isActive();
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

    private User getUser() {
        String username;
        String riprovare = ""; // n no y si
        do {
            username = ioService.readString("Inserisci username:");
            User user = data.getUsersFacade().getUser(username);
            if(user != null){
                return user;
            }

            riprovare = ioService.readString("Utente inesistente, vuoi riprovare? (y/n):");
            if (!riprovare.equalsIgnoreCase("y") && !riprovare.equalsIgnoreCase("n")){
                riprovare = ioService.readString("\nComando non valido inserire 'n' o 'y'");
            }
            if (riprovare.equalsIgnoreCase("n")) {
                ioService.writeMessage("\n\n\nCONNESSIONE CHIUSA\n\n\n", false);
                return null;
            }


            
        } while (riprovare.equalsIgnoreCase("y"));

        User user = data.getUsersFacade().getUser(username);
        assert user != null : "User is null";
        assert (
                (user.getRole().equals("configuratore") && connectionType == ConnectionType.Internal) ||
                (user.getRole().equals("volontario") && connectionType == ConnectionType.Internal) ||
                (user.getRole().equals("fruitore") && connectionType == ConnectionType.External)
        );
        return user;
    }


    /**
     * metodo per ottenere la nuova password e cambiarla
     *
     * @param username username
     */
    private void changePassword(String username) {
        assert data.getUsersFacade().doesUserExist(username): "cannot change password of invalid username";

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
    
        assert data.getUsersFacade().doesUserExist(username): "cannot verify password of invalid username";
        assert !password.isEmpty() : "cannot verify password if empty";

        if (temp){
            return authenticationUtil.verifyClearPassword(username, password);
        }

        return authenticationUtil.verifyPassword(username, password);
       
    }

    

}
