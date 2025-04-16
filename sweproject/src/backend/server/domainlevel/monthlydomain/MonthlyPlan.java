package backend.server.domainlevel.monthlydomain;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import backend.server.domainlevel.Activity;
import backend.server.genericservices.ReadWrite;
import backend.server.genericservices.datalayer.*;

public class MonthlyPlan extends ReadWrite {

    /*
     * popolato usando i config
     * 1- si crea la mappa dei piani giornalieri e si mette null nella mappa del giorno
     * relativo ad un giorno precliso
     * 2- per creare il monthly plan si controlla per ogni gionro se c'è gia null 
     * se è null si va avanti, altrimenti si crea un dailyu plan relativo a tale gionro
     */
    
    private LocalDate date;
    private Map<LocalDate,DailyPlan> monthlyPlan;
   
    public MonthlyPlan(Map<LocalDate, DailyPlan> montlyPlan, LocalDate date) {
        this.monthlyPlan = montlyPlan;
        this.date = date;
    }

    public MonthlyPlan(LocalDate date) {
        //write("Oggetto JSON caricato: " + dataLayer.get(new JSONDataContainer("JF/monthlyConfigs.json", "mc", "current", "type")), false);
        this.monthlyPlan = buildMonthlyMap();
        this.date = date;

    }



    private Map<LocalDate, DailyPlan> buildMonthlyMap() {
        DataLayer dataLayer = new JSONDataManager(this.getGson());
        HashMap<LocalDate, DailyPlan> monthlyMap = new HashMap<>();

        MonthlyConfig mc = JSONUtil.createObject(dataLayer.get(new JSONDataContainer("JF/monthlyConfigs.json", "mc", "current", "type")), MonthlyConfig.class);
        date = mc.getMonthAndYear();

        // Calcola il 16 del mese successivo
        LocalDate nextMonth16 = date.plusMonths(1).withDayOfMonth(16);

        // Cicla su ogni data dal giorno di oggi fino al 16 del mese successivo
        LocalDate currentDate = date.plusDays(1);
        while (!currentDate.isAfter(nextMonth16)) {
            if(mc.getPrecludeDates().contains(currentDate)) {
                monthlyMap.put(currentDate, null); 
            }else{
                monthlyMap.put(currentDate, new DailyPlan(currentDate)); 
            }
            
            currentDate = currentDate.plusDays(1);
        }

        return monthlyMap;
    }

    public Map<LocalDate, DailyPlan> getMonthlyPlan() {
        return monthlyPlan;
    }

    public void setMonthlyPlan(Map<LocalDate, DailyPlan> monthlyPlan) {
        this.monthlyPlan = monthlyPlan;
    }

    public void generateMonthlyPlan(List<Activity> activity){
        // per ogni giorno del mese
        // se il giorno è precluso non faccio nulla
        // altrimenti creo un daily plan e lo metto nella mappa
        for (LocalDate date : monthlyPlan.keySet()) {
            DailyPlan dp = monthlyPlan.get(date);
            if (!(dp == null)) {
                dp.generate(activity);
                monthlyPlan.put(date, dp);
            }
        }

    }

    private Gson getGson(){
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Gson gson = new GsonBuilder()
        .setPrettyPrinting()

        // LocalDate
        .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.format(dateFormatter)); // Format: dd-mm-yyyy
            }
        })
        .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return LocalDate.parse(json.getAsString(), dateFormatter);
            }
        })

        // LocalTime
        .registerTypeAdapter(LocalTime.class, new JsonSerializer<LocalTime>() {
            @Override
            public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.format(timeFormatter)); // Format: HH:mm
            }
        })
        .registerTypeAdapter(LocalTime.class, new JsonDeserializer<LocalTime>() {
            @Override
            public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return LocalTime.parse(json.getAsString(), timeFormatter);
            }
        })

        .create();

        return gson;
    }

    


}
