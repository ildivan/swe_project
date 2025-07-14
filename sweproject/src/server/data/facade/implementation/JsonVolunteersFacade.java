package server.data.facade.implementation;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonObject;

import server.data.Volunteer;
import server.data.facade.interfaces.IVolunteersFacade;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;
import server.jsonfactoryservice.JsonFactoryService;

public class JsonVolunteersFacade implements IVolunteersFacade {

    private final JsonDataLayer datalayer;
    private final IJsonLocInfoFactory locInfoFactory;
    private final JsonFactoryService jsonFactoryService = new JsonFactoryService();

    public JsonVolunteersFacade(JsonReadWrite jsonReadWrite, IJsonLocInfoFactory locInfoFactory) {
        this.datalayer = new JsonDataLayer(jsonReadWrite);
        this.locInfoFactory = locInfoFactory;
    }

    @Override
    public List<Volunteer> getVolunteers(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        List<JsonObject> volunteerJOList = datalayer.getAll(locInfo);

        return jsonFactoryService.createObjectList(volunteerJOList, Volunteer.class);
    }

    @Override
    public Volunteer getVolunteer(String name) {
        assert name != null && !name.trim().isEmpty() : "Nome volontario non valido";
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        locInfo.setKey(name);
        
        if (!datalayer.exists(locInfo)) {
            return null; // Volunteer does not exist
        }

        JsonObject volunteerJO = datalayer.get(locInfo);
        return jsonFactoryService.createObject(volunteerJO, Volunteer.class);
    }

    @Override
    public boolean modifyVolunteer(
        String name, 
        Optional<String> newName,
        Optional<Set<String>> disponibilityDaysCurrent,
        Optional<Set<String>> disponibilityDaysOld) {
        assert name != null && !name.trim().isEmpty() : "Nome volontario non valido";
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        locInfo.setKey(name);
        Volunteer volunteer = getVolunteer(name);

        if (volunteer == null) {
            return false; 
        }

        if (newName.isPresent()) {
            volunteer.setName(newName.get());
        }
        if (disponibilityDaysCurrent.isPresent()) {
            volunteer.setDisponibilityDaysCurrent(disponibilityDaysCurrent.get());
        }
        if (disponibilityDaysOld.isPresent()) {
            volunteer.setDisponibilityDaysOld(disponibilityDaysOld.get());
        }

        return datalayer.modify(jsonFactoryService.createJson(volunteer), locInfo);
    }

    @Override
    public boolean doesVolunteerExist(String name) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        locInfo.setKey(name);

        return datalayer.exists(locInfo);
    }

    @Override
    public boolean addVolunteer(String name){
        assert name != null && !name.trim().isEmpty() : "Nome volontario non valido";
        if (doesVolunteerExist(name)) {
            return false; // Volunteer already exists
        }
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        locInfo.setKey(name);
        datalayer.add(jsonFactoryService.createJson(new Volunteer(name)), locInfo);
        return datalayer.exists(locInfo);
    }

    @Override
    public boolean deleteVolunteer(String name){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        locInfo.setKey(name);
        datalayer.delete(locInfo);
        return !datalayer.exists(locInfo);
    }
}
