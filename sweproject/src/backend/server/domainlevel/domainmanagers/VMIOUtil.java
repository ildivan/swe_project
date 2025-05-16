package backend.server.domainlevel.domainmanagers;
import backend.server.domainlevel.User;
import backend.server.genericservices.datalayer.DataLayer;
import backend.server.genericservices.datalayer.JSONDataContainer;
import backend.server.genericservices.datalayer.JSONDataManager;
import backend.server.genericservices.datalayer.JSONUtil;
import backend.server.genericservices.gson.GsonFactory;
import backend.server.utils.IOUtil;

import com.google.gson.Gson;

public class VMIOUtil extends IOUtil{
    private static final Gson gson = GsonFactory.getGson();
    private static DataLayer dataLayer = new JSONDataManager(gson);

     /**
     * method to add a new user profile to user database creating a new random password
     * @param name
     */
    public static void addNewVolunteerUserProfile(String name) {
        String tempPass = "temp_" + Math.random();
        write(String.format("Nuova password temporanea per volontario: %s\n%s", name, tempPass), false);
        User u = new User(name, tempPass, "volontario");
        dataLayer.add(new JSONDataContainer("JF/users.json", JSONUtil.createJson(u), "users"));
    }
}
