package backend.server.domainlevel.domainmanagers;

import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import backend.server.domainlevel.Activity;
import backend.server.domainlevel.Manager;
import backend.server.domainlevel.monthlydomain.MonthlyPlan;
import backend.server.genericservices.datalayer.DataLayer;
import backend.server.genericservices.datalayer.JSONDataContainer;
import backend.server.genericservices.datalayer.JSONDataManager;
import backend.server.utils.DateUtil;

public class MonthlyPlanManager implements Manager{
    private MonthlyPlan monthlyPlan;
    ActivityManager activityManager; 
    DataLayer dataLayer;
    Gson gson;


    public MonthlyPlanManager(Gson gson){
        super();
        this.gson = gson;
        this.dataLayer = new JSONDataManager(gson);
        this.activityManager = new ActivityManager(gson);
        this.monthlyPlan = new MonthlyPlan(DateUtil.getTodayDate());
    }

    public MonthlyPlanManager() {
        this.monthlyPlan = new MonthlyPlan(DateUtil.getTodayDate());

    }

    @Override
    public void add(JsonObject data) {
        //viene passato un jsonobject che contiene cosa? nulla non metto niente nel jsonobject 
        //ho gai tutto quello che mi serve nei json

        //ottengo la lista delle attività

        List<Activity> activity = activityManager.getAllAsActivities();
        monthlyPlan.generateMonthlyPlan(activity);
        String StringJO = new String();
        StringJO = gson.toJson(monthlyPlan);
        JsonObject JO = gson.fromJson(StringJO, JsonObject.class);

        JSONDataContainer dataContainer = new JSONDataContainer("JF/monthlyPlan.json", JO, "monthlyPlan");
        
        dataLayer.add(dataContainer);

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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
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
