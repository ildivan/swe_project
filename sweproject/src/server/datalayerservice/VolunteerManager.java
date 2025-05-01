package server.datalayerservice;

import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import server.firstleveldomainservices.volunteerservice.VMIOUtil;
import server.firstleveldomainservices.volunteerservice.Volunteer;


public class VolunteerManager implements IBasicDLServices{
    private static final String PATH = "JF/volunteers.json";
    private static final String MEMBER_NAME = "volunteers";
    private static final String KEY_DESC = "name";
    DataLayer dataLayer;
    Gson gson;


    public VolunteerManager(Gson gson){
        super();
        this.gson = gson;
        this.dataLayer = new JSONDataManager(gson);
    }


    @Override
    public void add(JsonObject data) {
        dataLayer.add(new DataContainer(PATH, data, MEMBER_NAME));
        VMIOUtil.addNewVolunteerUserProfile(JSONService.createObject(data, Volunteer.class).getName());
    }

    @Override
    public void remove(JsonObject data, String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public void update(JsonObject data, String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public JsonObject get(String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public String getAll() {
       String out = "";
        List<JsonObject> volunteers = dataLayer.getAll(new DataContainer(PATH, MEMBER_NAME));
        for (JsonObject jo : volunteers){
            Volunteer a = JSONService.createObject(jo, Volunteer.class);
            out = out + a.toString();
        }

        return out;
    }

    @Override
    public boolean exists(String key) {
        if(dataLayer.get(new DataContainer(PATH, MEMBER_NAME, key, KEY_DESC))==null){
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean checkIfThereIsSomethingWithCondition() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkIfThereIsSomethingWithCondition'");
    }

    @Override
    public List<Object> getCustomList() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCustomList'");
    }



}
