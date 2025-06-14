package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonObject;

import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.monthlyconfig.MonthlyConfig;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;



public class MonthlyPlan{

    private static final String MONTHLY_CONFIG_CURRENT_KEY = "current";

    private LocalDate date;
    private Map<LocalDate,DailyPlan> monthlyPlan;

    //questo non deve essere serializzato -> inserisco transient per risolvere il problema
    private transient IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private transient ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory = new JsonLocInfoFactory();
    private transient IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();
   
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
        
        HashMap<LocalDate, DailyPlan> monthlyMap = new LinkedHashMap<>();

        MonthlyConfig mc = geMonthlyConfig();
 
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


    private MonthlyConfig geMonthlyConfig(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyConfigLocInfo();
        
        locInfo.setKey(MONTHLY_CONFIG_CURRENT_KEY);

        JsonObject mcJO = dataLayer.get(locInfo);
        return jsonFactoryService.createObject(mcJO, MonthlyConfig.class);
    }

    public DailyPlan getDailyPlan(LocalDate date) {
        return monthlyPlan.get(date);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Map<LocalDate, DailyPlan> getMonthlyPlanMap() {
        return monthlyPlan;
    }
    


}
