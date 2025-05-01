package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import server.GsonFactoryService;
import server.datalayerservice.DataContainer;
import server.datalayerservice.DataLayer;
import server.datalayerservice.JSONDataManager;
import server.datalayerservice.JSONService;
import server.firstleveldomainservices.Activity;



public class MonthlyPlan{

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
        DataLayer dataLayer = new JSONDataManager((Gson) GsonFactoryService.Service.GET_GSON.start());
        HashMap<LocalDate, DailyPlan> monthlyMap = new LinkedHashMap<>();

        MonthlyConfig mc = JSONService.createObject(dataLayer.get(new DataContainer("JF/monthlyConfigs.json", "mc", "current", "type")), MonthlyConfig.class);
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

        incrementMonthOfPlan();
        clearPrecludedDates();
    }

    /**
     * clear the preclude dates in the config of the monthly plan
     */
    private void clearPrecludedDates() {
        DataLayer dataLayer = new JSONDataManager((Gson) GsonFactoryService.Service.GET_GSON.start());
        MonthlyConfig mc = JSONService.createObject(dataLayer.get(new DataContainer("JF/monthlyConfigs.json", "mc", "current", "type")), MonthlyConfig.class);
        mc.setPrecludeDates(new HashSet<>());

        dataLayer.modify(new DataContainer("JF/monthlyConfigs.json", JSONService.createJson(mc),"mc", "current", "type"));
    }

    /**
     * change month of plan into monthly configs
     */
    private void incrementMonthOfPlan() {
        DataLayer dataLayer = new JSONDataManager((Gson) GsonFactoryService.Service.GET_GSON.start());
        MonthlyConfig mc = JSONService.createObject(dataLayer.get(new DataContainer("JF/monthlyConfigs.json", "mc", "current", "type")), MonthlyConfig.class);
        LocalDate date = mc.getMonthAndYear();
        LocalDate newDate = date.plusMonths(1);
        mc.setMonthAndYear(newDate);

        dataLayer.modify(new DataContainer("JF/monthlyConfigs.json", JSONService.createJson(mc),"mc", "current", "type"));
    }

    


}
