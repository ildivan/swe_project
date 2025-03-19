package backend.server.genericservices.auth;

import com.google.gson.JsonObject;

import org.mindrot.jbcrypt.*;

import backend.server.domainlevel.User;
import backend.server.genericservices.DataLayer.DataContainer;
import backend.server.genericservices.DataLayer.DataLayer;
import backend.server.genericservices.DataLayer.JSONDataManager;

public class AuthenticationUtil {

    private static final int HASH_ROUNDS = 12;
    private static DataLayer dataLayer = new JSONDataManager();
    
    public static boolean checkIfTemp(String username) {
        DataContainer dataContainer = new DataContainer("sweproject/JsonFIles/users.json", "users", "name", username);
        JsonObject userJO = dataLayer.get(dataContainer);
    return userJO.get("password").getAsString().contains("temp");
    }

    public static boolean changePassword(String username, String newPassword) {
        DataContainer dataContainer1 = new DataContainer("sweproject/JsonFIles/users.json", "users", "name", username);
        JsonObject userJO = dataLayer.get(dataContainer1);
        String newPasswordCrypted = cryptPassword(newPassword);
        userJO.addProperty("password", newPasswordCrypted);
        DataContainer dataContainer2 = new DataContainer("sweproject/JsonFIles/users.json", userJO, "users", "name", username);
        return dataLayer.modify(dataContainer2);
    }

    private static String cryptPassword(String password) {
        String salt = BCrypt.gensalt(HASH_ROUNDS);
        // Crea l'hash della password
        return BCrypt.hashpw(password, salt);
    }

    public static boolean verifyPassword(String username, String password) {
        DataContainer dataContainer = new DataContainer("sweproject/JsonFIles/users.json", "users", "name", username);
        JsonObject userJO = dataLayer.get(dataContainer);
        String hashedPassword = userJO.get("password").getAsString();
        return BCrypt.checkpw(password, hashedPassword);
    }
}
