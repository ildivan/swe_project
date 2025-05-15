package server.firstleveldomainservices.volunteerservice;

import server.authservice.User;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;


public class VMIOUtil{
     private static final String ROLE = "volontario";
 
     private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
     private ILocInfoFactory locInfoFactory = new JsonLocInfoFactory();
     private final IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();

     /**
     * method to add a new user profile to user database creating a new random password
     * @param name
     */
    public void addNewVolunteerUserProfile(String name) {
        IInputOutput ioService = new IOService();
        String tempPass = "temp_" + Math.random();
        ioService.writeMessage(String.format("Nuova password temporanea per volontario: %s\n%s", name, tempPass), false);
        User u = new User(name, tempPass, ROLE);

        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();
        
        dataLayer.add(jsonFactoryService.createJson(u), locInfo);

    }
}
