package server.datalayerservice;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
        dataLayer.add(new DataContainer(PATH, data, MEMBER_NAME));
        //UNIMPLEMENTED
    }

    @Override
    public void remove(JsonObject data, String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public void update(JsonObject data, String key) {
        dataLayer.modify(new DataContainer(PATH, data, MEMBER_NAME,key, KEY_DESC));
    }

    @Override
    public JsonObject get(String key) {
       return dataLayer.get(new DataContainer(PATH, MEMBER_NAME, key, KEY_DESC));
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
