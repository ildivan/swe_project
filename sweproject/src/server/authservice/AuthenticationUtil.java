package server.authservice;

import server.data.facade.FacadeHub;
import java.util.Optional;

import org.mindrot.jbcrypt.*;

public class AuthenticationUtil {

    private final FacadeHub data;
    private final int HASH_ROUNDS = 12;

    public AuthenticationUtil(FacadeHub data) {
        this.data = data;
    }

    public boolean checkIfTemp(String username) {
        User user = data.getUsersFacade().getUser(username);
        assert user != null: "user not found";
        
        return user.getPassword().contains("temp");
    }

    public boolean changePassword(String username, String newPassword) {
        assert !newPassword.trim().isEmpty(): "password is empty";
        assert data.getUsersFacade().doesUserExist(username): "user not found";
        
        String newPasswordEncrypted = cryptPassword(newPassword);
        return data.getUsersFacade().modifyUser(
            username,
            Optional.empty(),
            Optional.of(newPasswordEncrypted),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
    }

    private String cryptPassword(String password) {
        assert !password.trim().isEmpty(): "password is empty";
        String salt = BCrypt.gensalt(HASH_ROUNDS);
        // Crea l'hash della password
        return BCrypt.hashpw(password, salt);
    }

    public boolean verifyPassword(String username, String password) {
        assert !password.trim().isEmpty(): "password is empty";
        User user = data.getUsersFacade().getUser(username);
        assert user != null: "user not found";

        String hashedPassword = user.getPassword();
        return BCrypt.checkpw(password, hashedPassword);
    }

    public boolean verifyClearPassword(String username, String password) {
        assert !password.trim().isEmpty(): "password is empty";
        User user = data.getUsersFacade().getUser(username);
        assert user != null: "user not found";

        String clearPassword = user.getPassword();
        return clearPassword.contentEquals(password);
    }
}
