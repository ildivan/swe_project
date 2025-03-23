package backend.server.domainlevel.domainmanagers;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import backend.server.domainlevel.Manager;
import backend.server.genericservices.DataLayer.DataLayer;
import backend.server.genericservices.DataLayer.JSONDataContainer;
import backend.server.genericservices.DataLayer.JSONDataManager;

public class VolunteerManager implements Manager{
    private static final String PATH = "JF/volunteers.json";
    private static final String MEMBER_NAME = "volunteers";
    private static final String KEY_DESC = "name";
    DataLayer dataLayer = new JSONDataManager();
    Gson gson = new Gson();


    @Override
    public void add(JsonObject data) {
        dataLayer.add(new JSONDataContainer(PATH, data, MEMBER_NAME));
    }

    @Override
    public void remove(JsonObject data) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public void update(JsonObject data) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public JsonObject get(String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public void getAll(JsonObject data) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
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
