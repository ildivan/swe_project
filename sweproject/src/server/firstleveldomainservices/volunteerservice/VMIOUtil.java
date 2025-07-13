package server.firstleveldomainservices.volunteerservice;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonObject;

import server.authservice.User;
import server.data.facade.FacadeHub;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;


public class VMIOUtil{
    private static final String ROLE = "volontario";

    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private IJsonLocInfoFactory locInfoFactory;
    private final JsonDataLayer dataLayer;
    private final FacadeHub data;

    public VMIOUtil(IJsonLocInfoFactory locInfoFactory,
    JsonDataLayer dataLayer, FacadeHub data) {
        this.locInfoFactory = locInfoFactory;
        this.dataLayer = dataLayer;
        this.data = data;
    }

    public boolean checkVolunteerExistance(String name){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        locInfo.setKey(name);

        return dataLayer.exists(locInfo);
    }

    /**
     * method to add a volunteer
     * @param name
     * @return
     */
    public void addVolunteer(String name){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
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

        data.getUsersFacade().addUser(u);
    }

    /**
     * method to delete volunteer from the database
     * @param name
     */
    public void deleteVolunteer(String name){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        locInfo.setKey(name);

        dataLayer.delete(locInfo);

    }

    /**
     * method used to deactivate volunteer
     * @param name
     */
    public void deactivateVolunteer(String name) {

       User user = data.getUsersFacade().getUser(name);
       assert user != null : "User not found";

       user.setActive(false);
       user.setIsDeleted(true);

       boolean modified = data.getUsersFacade().modifyUser(
            user.getName(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(false),
            Optional.of(true)
        );
        assert modified;
    }

    /**
     * method to add disponibility date
     * @param firstPlanConfigured
     * @param name
     */
    public void addDisponibilityDate(boolean firstPlanConfigured, String name, String formattedDate){

        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        locInfo.setKey(name);
        JsonObject volunteerJO = dataLayer.get(locInfo);
        Volunteer volunteer = jsonFactoryService.createObject(volunteerJO, Volunteer.class);
        Set<String> disponibilityDays;

        disponibilityDays = volunteer.getDisponibilityDaysCurrent();

        if(disponibilityDays == null){
            disponibilityDays = new LinkedHashSet<String>();
        }
        
        disponibilityDays.add(formattedDate);

        volunteer.setDisponibilityDaysCurrent(disponibilityDays);

        saveVolunteer(volunteer, locInfo);

    }

    private void saveVolunteer(Volunteer volunteer, JsonDataLocalizationInformation locInfo){
        JsonObject newVolunteerJO = jsonFactoryService.createJson(volunteer);

        dataLayer.modify(newVolunteerJO, locInfo);
    }
}
