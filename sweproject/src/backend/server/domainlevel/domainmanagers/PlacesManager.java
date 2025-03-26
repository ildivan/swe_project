package backend.server.domainlevel.domainmanagers;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import backend.server.domainlevel.Activity;
import backend.server.domainlevel.Manager;
import backend.server.domainlevel.Place;
import backend.server.genericservices.DataLayer.DataLayer;
import backend.server.genericservices.DataLayer.JSONDataContainer;
import backend.server.genericservices.DataLayer.JSONDataManager;
import backend.server.genericservices.DataLayer.JSONUtil;

public class PlacesManager implements Manager {
    private static final String PATH = "JF/places.json";
    private static final String MEMBER_NAME = "places";
    private static final String KEY_DESC = "name";
    DataLayer dataLayer = new JSONDataManager();
    Gson gson = new Gson();

    @Override
    public void add(JsonObject data) {
        dataLayer.add(new JSONDataContainer(PATH, data, MEMBER_NAME));
    }

    @Override
    public void remove(JsonObject data) {
        
    }

    @Override
    public void update(JsonObject data) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public JsonObject get(String key) {
        return dataLayer.get(new JSONDataContainer(PATH, MEMBER_NAME, key, KEY_DESC));
        
    }

    @Override
    public String getAll() {
        String out = "";
        List<JsonObject> places = dataLayer.getAll(new JSONDataContainer(PATH, MEMBER_NAME));
        for (JsonObject jo : places){
            Place a = JSONUtil.createObject(jo, Place.class);
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

    /**
     * la condizione è che ci sia almeno luogo senza attivita associata se non c'è ritorna vero
     * @return
     */
    public boolean checkIfThereIsSomethingWithCondition(){
        if(dataLayer.get(new JSONDataContainer(PATH, MEMBER_NAME, "false", "atLeastOneActivityRelated"))==null){
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<Place> getCustomList(){
        List<JsonObject> pJO = dataLayer.getList(new JSONDataContainer(PATH, MEMBER_NAME, "false", "atLeastOneActivityRelated"));
        List<Place> places = new ArrayList<Place>();
        
        for(JsonObject jo : pJO){
            Place p = gson.fromJson(jo, Place.class);
            if(!p.getAtLeastOneActivityRelated()){
                places.add(p);
            }
        }
        return places;
    }

}
