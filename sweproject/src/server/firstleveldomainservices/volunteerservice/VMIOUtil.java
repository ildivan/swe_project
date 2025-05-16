package server.firstleveldomainservices.volunteerservice;
import com.google.gson.Gson;

import server.GsonFactoryService;
import server.authservice.User;
import server.datalayerservice.DataContainer;
import server.datalayerservice.DataLayer;
import server.datalayerservice.JSONDataManager;
import server.datalayerservice.JSONUtil;
import server.ioservice.IOService;


public class VMIOUtil{
    private static final Gson gson = GsonFactoryService.getGson();
    private static DataLayer dataLayer = new JSONDataManager(gson);

     /**
     * method to add a new user profile to user database creating a new random password
     * @param name
     */
    public static void addNewVolunteerUserProfile(String name) {
        String tempPass = "temp_" + Math.random();
        IOService.Service.WRITE.start(String.format("Nuova password temporanea per volontario: %s\n%s", name, tempPass), false);
        User u = new User(name, tempPass, "volontario");
        dataLayer.add(new DataContainer("JF/users.json", JSONUtil.createJson(u), "users"));
    }
}
