package backend.server.domainlevel.domainmanagers;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import backend.server.domainlevel.Activity;
import backend.server.domainlevel.Manager;
import backend.server.domainlevel.User;
import backend.server.domainlevel.Volunteer;
import backend.server.genericservices.datalayer.DataLayer;
import backend.server.genericservices.datalayer.JSONDataContainer;
import backend.server.genericservices.datalayer.JSONDataManager;
import backend.server.genericservices.datalayer.JSONUtil;

public class VolunteerManager implements Manager{
    private static final String PATH = "JF/volunteers.json";
    private static final String MEMBER_NAME = "volunteers";
    private static final String KEY_DESC = "name";
    DataLayer dataLayer = new JSONDataManager();
    Gson gson = new Gson();


    @Override
    public void add(JsonObject data) {
        dataLayer.add(new JSONDataContainer(PATH, data, MEMBER_NAME));
        VMIOUtil.addNewVolunteerUserProfile(JSONUtil.createObject(data, Volunteer.class).getName());
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
        List<JsonObject> volunteers = dataLayer.getAll(new JSONDataContainer(PATH, MEMBER_NAME));
        for (JsonObject jo : volunteers){
            Volunteer a = JSONUtil.createObject(jo, Volunteer.class);
            out = out + a.toString();
        }

        return out;
    }

    @Override
    public boolean exists(String key) {
        if(dataLayer.get(new JSONDataContainer(PATH, MEMBER_NAME, key, KEY_DESC))==null){
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
