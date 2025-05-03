package server.firstleveldomainservices.volunteerservice;

import server.authservice.User;
import server.datalayerservice.DataLayerDispatcherService;
import server.datalayerservice.JsonDataLocalizationInformation;
import server.ioservice.IOService;
import server.jsonfactoryservice.JsonFactoryService;


public class VMIOUtil{
     private static final String ROLE = "volontario";
     private static final String USERS_MEMBER_NAME = "users";
     private static final String USERS_PATH = "JF/users.json";

     /**
     * method to add a new user profile to user database creating a new random password
     * @param name
     */
    public static void addNewVolunteerUserProfile(String name) {
        String tempPass = "temp_" + Math.random();
        IOService.Service.WRITE.start(String.format("Nuova password temporanea per volontario: %s\n%s", name, tempPass), false);
        User u = new User(name, tempPass, ROLE);

        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(USERS_PATH);
        locInfo.setMemberName(USERS_MEMBER_NAME);
        
        DataLayerDispatcherService.start(locInfo, layer -> layer.add(JsonFactoryService.createJson(u), locInfo));

    }
}
