package backend.server.domainlevel.domainmanagers;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import backend.server.Configs;
import backend.server.domainlevel.Manager;
import backend.server.domainlevel.Volunteer;
import backend.server.genericservices.datalayer.DataLayer;
import backend.server.genericservices.datalayer.JSONDataContainer;
import backend.server.genericservices.datalayer.JSONDataManager;
import backend.server.genericservices.datalayer.JSONUtil;

public class ConfigManager implements Manager{
    private static final String PATH = "JF/configs.json";
    private static final String MEMBER_NAME = "configs";
    private static final String KEY_DESC = "configType";
    DataLayer dataLayer;
    Gson gson;


    public ConfigManager(Gson gson){
        super();
        this.gson = gson;
        this.dataLayer = new JSONDataManager(gson);
    }
    @Override
    public void add(JsonObject data) {
        dataLayer.add(new JSONDataContainer(PATH, data, MEMBER_NAME));
        //UNIMPLEMENTED
    }

    @Override
    public void remove(JsonObject data, String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public void update(JsonObject data, String key) {
        dataLayer.modify(new JSONDataContainer(PATH, data, MEMBER_NAME,key, KEY_DESC));
    }

    @Override
    public JsonObject get(String key) {
       return dataLayer.get(new JSONDataContainer(PATH, MEMBER_NAME, key, KEY_DESC));
    }

    @Override
    public boolean exists(String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'exists'");
    }

    @Override
    public boolean checkIfThereIsSomethingWithCondition() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkIfThereIsSomethingWithCondition'");
    }

    @Override
    public List<?> getCustomList() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCustomList'");
    }

    @Override
    public String getAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }
    
}
