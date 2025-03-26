package backend.server.domainlevel.domainmanagers;

import backend.server.domainlevel.User;
import backend.server.genericservices.IOUtil;
import backend.server.genericservices.DataLayer.DataLayer;
import backend.server.genericservices.DataLayer.JSONDataContainer;
import backend.server.genericservices.DataLayer.JSONDataManager;
import backend.server.genericservices.DataLayer.JSONUtil;

public class VMIOUtil extends IOUtil{

    private static DataLayer dataLayer = new JSONDataManager();


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
