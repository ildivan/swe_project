package server.firstleveldomainservices.volunteerservice;

import server.authservice.User;
import server.datalayerservice.DataLayerDispatcherService;
import server.datalayerservice.JsonDataLocalizationInformation;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;


public class VMIOUtil{
     private static final String ROLE = "volontario";
     private static final String USERS_MEMBER_NAME = "users";
     private static final String USERS_PATH = "JF/users.json";
     private IJsonFactoryService jsonFactoryService = new JsonFactoryService();

     /**
     * method to add a new user profile to user database creating a new random password
     * @param name
     */
    public void addNewVolunteerUserProfile(String name) {
        IInputOutput ioService = new IOService();
        String tempPass = "temp_" + Math.random();
        ioService.writeMessage(String.format("Nuova password temporanea per volontario: %s\n%s", name, tempPass), false);
        User u = new User(name, tempPass, ROLE);

        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(USERS_PATH);
        locInfo.setMemberName(USERS_MEMBER_NAME);
        
        DataLayerDispatcherService.start(locInfo, layer -> layer.add(jsonFactoryService.createJson(u), locInfo));

    }
}
