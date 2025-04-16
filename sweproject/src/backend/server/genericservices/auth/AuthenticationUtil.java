package backend.server.genericservices.auth;
import com.google.gson.JsonObject;
import backend.server.genericservices.datalayer.DataLayer;
import backend.server.genericservices.datalayer.JSONDataContainer;
import backend.server.genericservices.datalayer.JSONDataManager;
import backend.server.genericservices.gson.GsonFactory;
import org.mindrot.jbcrypt.*;

public class AuthenticationUtil {

    private static final int HASH_ROUNDS = 12;
    private static DataLayer dataLayer = new JSONDataManager(GsonFactory.getGson());
    
    public static boolean checkIfTemp(String username) {
        JSONDataContainer dataContainer = new JSONDataContainer("JF/users.json", "users", username, "name");
        JsonObject userJO = dataLayer.get(dataContainer);
    return userJO.get("password").getAsString().contains("temp");
    }

    public static boolean changePassword(String username, String newPassword) {
        JSONDataContainer dataContainer1 = new JSONDataContainer("JF/users.json", "users", username, "name");
        JsonObject userJO = dataLayer.get(dataContainer1);
        String newPasswordCrypted = cryptPassword(newPassword);
        userJO.addProperty("password", newPasswordCrypted);
        JSONDataContainer dataContainer2 = new JSONDataContainer("JF/users.json", userJO, "users", username, "name");
        return dataLayer.modify(dataContainer2);
    }

    private static String cryptPassword(String password) {
        String salt = BCrypt.gensalt(HASH_ROUNDS);
        // Crea l'hash della password
        return BCrypt.hashpw(password, salt);
    }

    public static boolean verifyPassword(String username, String password) {
        JSONDataContainer dataContainer = new JSONDataContainer("JF/users.json", "users", username, "name");
        JsonObject userJO = dataLayer.get(dataContainer);
        String hashedPassword = userJO.get("password").getAsString();
        return BCrypt.checkpw(password, hashedPassword);
    }

    
}
