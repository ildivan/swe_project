package server.authservice;
import com.google.gson.JsonObject;

import server.GsonFactoryService;
import server.datalayerservice.DataContainer;
import server.datalayerservice.DataLayer;
import server.datalayerservice.JSONDataManager;

import org.mindrot.jbcrypt.*;

public class AuthenticationUtil {

    private static final int HASH_ROUNDS = 12;
    private static DataLayer dataLayer = new JSONDataManager(GsonFactoryService.getGson());
    
    public static boolean checkIfTemp(String username) {
        DataContainer dataContainer = new DataContainer("JF/users.json", "users", username, "name");
        JsonObject userJO = dataLayer.get(dataContainer);
    return userJO.get("password").getAsString().contains("temp");
    }

    public static boolean changePassword(String username, String newPassword) {
        DataContainer dataContainer1 = new DataContainer("JF/users.json", "users", username, "name");
        JsonObject userJO = dataLayer.get(dataContainer1);
        String newPasswordCrypted = cryptPassword(newPassword);
        userJO.addProperty("password", newPasswordCrypted);
        DataContainer dataContainer2 = new DataContainer("JF/users.json", userJO, "users", username, "name");
        return dataLayer.modify(dataContainer2);
    }

    private static String cryptPassword(String password) {
        String salt = BCrypt.gensalt(HASH_ROUNDS);
        // Crea l'hash della password
        return BCrypt.hashpw(password, salt);
    }

    public static boolean verifyPassword(String username, String password) {
        DataContainer dataContainer = new DataContainer("JF/users.json", "users", username, "name");
        JsonObject userJO = dataLayer.get(dataContainer);
        String hashedPassword = userJO.get("password").getAsString();
        return BCrypt.checkpw(password, hashedPassword);
    }

    
}
