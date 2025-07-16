package server.firstleveldomainservices.volunteerservice;

import server.authservice.User;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;


public class VMIOUtil{
     private static final String ROLE = "volontario";
 
     private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
     private ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory;
     private final IDataLayer<JsonDataLocalizationInformation> dataLayer;

    public VMIOUtil(ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory,
    IDataLayer<JsonDataLocalizationInformation> dataLayer) {
        this.locInfoFactory = locInfoFactory;
        this.dataLayer = dataLayer;
    }

    public boolean checkVolunteerExistance(String name){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedVolunteersLocInfo();
        locInfo.setKey(name);

        return dataLayer.exists(locInfo);
    }

    /**
     * method to add a volunteer
     * @param name
     * @return
     */
    public void addVolunteer(String name){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedVolunteersLocInfo();
        locInfo.setKey(name);

        dataLayer.add(jsonFactoryService.createJson(new Volunteer(name)), locInfo);

        addNewVolunteerUserProfile(name);
    }

     /**
     * method to add a new user profile to user database creating a new random password
     * @param name
     */
    private void addNewVolunteerUserProfile(String name) {
        IInputOutput ioService = new IOService();
        String tempPass = "temp_" + Math.random();
        ioService.writeMessage(String.format("Nuova password temporanea per volontario: %s\n%s", name, tempPass), false);
        User u = new User(name, tempPass, ROLE, false);

        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();
        
        dataLayer.add(jsonFactoryService.createJson(u), locInfo);
    }

    /**
     * method to delete volunteer
     * @param name
     */
    public void deleteVolunteer(String name){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedVolunteersLocInfo();
        locInfo.setKey(name);

        dataLayer.delete(locInfo);

        deleteVolunteerUser(name);
    }

    /**
     * method to delete user of the deleted volunteer
     * @param name
     */
    private void deleteVolunteerUser(String name) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();
        locInfo.setKey(name);

        User user = jsonFactoryService.createObject(dataLayer.get(locInfo), User.class);

        user.setIsDeleted(true);

        dataLayer.modify(jsonFactoryService.createJson(user),locInfo);
    }
}
