package server.authservice;

import com.google.gson.JsonObject;
import server.datalayerservice.DataLayerDispatcherService;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import org.mindrot.jbcrypt.*;

public class AuthenticationUtil {

    private static final ILocInfoFactory locInfoFactory= new JsonLocInfoFactory();
    private static final int HASH_ROUNDS = 12;
    
    
    public static boolean checkIfTemp(String username) {
        JsonObject userJO = getUserJsonObject(username);
        assert userJO != null: "user not found";
        
        return userJO.get("password").getAsString().contains("temp");
    }

    public static boolean changePassword(String username, String newPassword) {
        assert !newPassword.trim().isEmpty(): "password is empty";
        JsonObject userJO = getUserJsonObject(username);
        assert userJO != null: "user not found";
        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();

        locInfo.setKey(username);
        String newPasswordEncrypted = cryptPassword(newPassword);
        userJO.addProperty("password", newPasswordEncrypted);

        return DataLayerDispatcherService.startWithResult(locInfo, layer -> layer.modify(userJO, locInfo));
    }

    private static String cryptPassword(String password) {
        assert !password.trim().isEmpty(): "password is empty";
        String salt = BCrypt.gensalt(HASH_ROUNDS);
        // Crea l'hash della password
        return BCrypt.hashpw(password, salt);
    }

    public static boolean verifyPassword(String username, String password) {
        assert !password.trim().isEmpty(): "password is empty";
        JsonObject userJO = getUserJsonObject(username);
        assert userJO != null: "user not found";

        String hashedPassword = userJO.get("password").getAsString();
        return BCrypt.checkpw(password, hashedPassword);
    }

    public static JsonObject getUserJsonObject(String username){
        assert !username.trim().isEmpty(): "username is empty";
        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();

        locInfo.setKey(username);
        JsonObject user =  DataLayerDispatcherService.startWithResult(locInfo, layer -> layer.get(locInfo));
        assert user != null: "user not found";
        return user;
    }

    
}
