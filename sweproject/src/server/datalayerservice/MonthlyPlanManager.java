package server.datalayerservice;

import java.time.LocalDate;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import server.DateService;


public class MonthlyPlanManager implements Manager{
    ActivityManager activityManager; 
    DataLayer dataLayer;
    Gson gson;


    public MonthlyPlanManager(Gson gson){
        super();
        this.gson = gson;
        this.dataLayer = new JSONDataManager(gson);
        LocalDate today = (LocalDate) DateService.Service.GET_TODAY_DATE.start();
    }

    @Override
    public void add(JsonObject data) {

        String StringJO = new String();
        StringJO = gson.toJson(data);
        JsonObject JO = gson.fromJson(StringJO, JsonObject.class);

        DataContainer dataContainer = new DataContainer("JF/monthlyPlan.json", JO, "monthlyPlan");
        
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
