package server.authservice;

import com.google.gson.JsonObject;

import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;

import org.mindrot.jbcrypt.*;

public class AuthenticationUtil {

    private final IJsonLocInfoFactory locInfoFactory;
    private final int HASH_ROUNDS = 12;
    private final JsonDataLayer dataLayer;

    public AuthenticationUtil(IJsonLocInfoFactory locInfoFactory,
    JsonDataLayer dataLayer) {
        this.locInfoFactory = locInfoFactory;
        this.dataLayer = dataLayer;
    }

    public boolean checkIfTemp(String username) {
        JsonObject userJO = getUserJsonObject(username);
        assert userJO != null: "user not found";
        
        return userJO.get("password").getAsString().contains("temp");
    }

    public boolean changePassword(String username, String newPassword) {
        assert !newPassword.trim().isEmpty(): "password is empty";
        JsonObject userJO = getUserJsonObject(username);
        assert userJO != null: "user not found";
        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();

        locInfo.setKey(username);
        String newPasswordEncrypted = cryptPassword(newPassword);
        userJO.addProperty("password", newPasswordEncrypted);

        return dataLayer.modify(userJO, locInfo);
    }

    private String cryptPassword(String password) {
        assert !password.trim().isEmpty(): "password is empty";
        String salt = BCrypt.gensalt(HASH_ROUNDS);
        // Crea l'hash della password
        return BCrypt.hashpw(password, salt);
    }

    public boolean verifyPassword(String username, String password) {
        assert !password.trim().isEmpty(): "password is empty";
        JsonObject userJO = getUserJsonObject(username);
        assert userJO != null: "user not found";

        String hashedPassword = userJO.get("password").getAsString();
        return BCrypt.checkpw(password, hashedPassword);
    }

    public boolean verifyClearPassword(String username, String password) {
        assert !password.trim().isEmpty(): "password is empty";
        JsonObject userJO = getUserJsonObject(username);
        assert userJO != null: "user not found";

        String clearPassword = userJO.get("password").getAsString();
        return clearPassword.contentEquals(password);
    }

    public JsonObject getUserJsonObject(String username){
        assert !username.trim().isEmpty(): "username is empty";
        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();

        locInfo.setKey(username);
        JsonObject user =  dataLayer.get(locInfo);
        assert user != null: "user not found";
        return user;
    }

    
}
