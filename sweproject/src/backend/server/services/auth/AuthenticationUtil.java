package backend.server.services.auth;

import com.google.gson.JsonObject;

import org.mindrot.jbcrypt.*;

import backend.server.json.JSONDataManager;
import backend.server.objects.User;

public class AuthenticationUtil {

    private static final int HASH_ROUNDS = 12;

    public static boolean checkIfTemp(String username) {
        JsonObject userJO = JSONDataManager.get(username, "name", "sweproject/JsonFIles/users.json", "users");
        return userJO.get("password").getAsString().contains("temp");
    }

    public static boolean changePassword(String username, String newPassword) {
        JsonObject userJO = JSONDataManager.get(username, "name", "sweproject/JsonFIles/users.json", "users");
        String newPasswordCrypted = cryptPassword(newPassword);
        userJO.addProperty("password", newPasswordCrypted);
        return JSONDataManager.modify(username, "name", userJO, "sweproject/JsonFIles/users.json", "users");
    }

    private static String cryptPassword(String password) {
        String salt = BCrypt.gensalt(HASH_ROUNDS);
        // Crea l'hash della password
        return BCrypt.hashpw(password, salt);
    }

    public static boolean verifyPassword(String username, String password) {
        JsonObject userJO = JSONDataManager.get(username, "name", "sweproject/JsonFIles/users.json", "users");
        String hashedPassword = userJO.get("password").getAsString();
        return BCrypt.checkpw(password, hashedPassword);
    }
}
