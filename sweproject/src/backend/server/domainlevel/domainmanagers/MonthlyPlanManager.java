package backend.server.domainlevel.domainmanagers;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import backend.server.domainlevel.Manager;
import backend.server.domainlevel.monthlydomain.MonthlyPlan;
import backend.server.genericservices.DateUtil;
import backend.server.genericservices.datalayer.DataLayer;
import backend.server.genericservices.datalayer.JSONDataContainer;
import backend.server.genericservices.datalayer.JSONDataManager;

public class MonthlyPlanManager implements Manager{
    private MonthlyPlan monthlyPlan;
    private DataLayer dataLayer = new JSONDataManager();
    private Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString()); // Format: "2025-04-01"
        }
    })
    .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDate.parse(json.getAsString());
        }
    })
    .create();



    public MonthlyPlanManager() {
        this.monthlyPlan = new MonthlyPlan(DateUtil.getTodayDate());

    }

     //TODO
    /*
     * 1. metodo per prendere la data di sistema (potrà essere avviato solo il 16 di ogni mese) e
     * popolare la map con le date di ogni giono
     * 
     * 2. metodo per settare i gionri preclusi
     * 
     * 3. metodo per creare il monthly plan (che chiama per ogni gionro non precluso un daily plan manager
     * che crea il piano di ogni giorno)
     */

    @Override
    public void add(JsonObject data) {
        //viene passato un jsonobject che contiene cosa? nulla non metto niente nel jsonobject 
        //ho gai tutto quello che mi serve nei json
        monthlyPlan.generateMonthlyPlan();
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
