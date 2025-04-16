package backend.server.domainlevel.domainmanagers;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import backend.server.domainlevel.Activity;
import backend.server.domainlevel.Manager;
import backend.server.domainlevel.Place;
import backend.server.genericservices.datalayer.DataLayer;
import backend.server.genericservices.datalayer.JSONDataContainer;
import backend.server.genericservices.datalayer.JSONDataManager;
import backend.server.genericservices.datalayer.JSONUtil;

public class ActivityManager implements Manager{
    
    
    private static final String PATH = "JF/activities.json";
    private static final String MEMBER_NAME = "activities";
     DataLayer dataLayer;
    Gson gson;


    public ActivityManager(Gson gson){
        super();
        this.gson = gson;
        this.dataLayer = new JSONDataManager(gson);
    }

    @Override
    public void add(JsonObject data) {
        JsonObject toAdd = getActivity(JSONUtil.createObject(data, Place.class));
        dataLayer.add(new JSONDataContainer(PATH, toAdd, MEMBER_NAME));
    }

    private JsonObject getActivity(Place place){
        Activity activity = AMIOUtil.getActivity(place);
        String[] volunteers = AMIOUtil.addVolunteersToActivity();
        activity.setVolunteers(volunteers);
        return JSONUtil.createJson(activity);
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
        List<JsonObject> activities = dataLayer.getAll(new JSONDataContainer(PATH, MEMBER_NAME));
        for (JsonObject jo : activities){
            Activity a = JSONUtil.createObject(jo, Activity.class);
            out = out + a.toString();
        }

        return out;
    }

    public List<Activity> getAllAsActivities(){
        List<JsonObject> activities = dataLayer.getAll(new JSONDataContainer(PATH, MEMBER_NAME));
        List<Activity> out = new ArrayList<>();
        for (JsonObject jo : activities){
            Activity a = JSONUtil.createObject(jo, Activity.class);
            out.add(a);
        }
        return out;
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
    public List<Object> getCustomList() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCustomList'");
    }
    
}
