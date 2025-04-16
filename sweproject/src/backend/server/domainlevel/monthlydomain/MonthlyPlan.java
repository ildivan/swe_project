package backend.server.domainlevel.monthlydomain;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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

    private DataLayer dataLayer = new JSONDataManager();
    private MonthlyConfig mc;

    public MonthlyPlan(Map<LocalDate, DailyPlan> montlyPlan, LocalDate date) {
        this.monthlyPlan = montlyPlan;
        this.date = date;
        this.mc = JSONUtil.createObject(dataLayer.get(new JSONDataContainer("JF/monthlyConfigs.json", "mc", "current", "type")), MonthlyConfig.class);
    }

    public MonthlyPlan(LocalDate date) {
        write("Oggetto JSON caricato: " + dataLayer.get(new JSONDataContainer("JF/monthlyConfigs.json", "mc", "current", "type")), false);

       this.mc = JSONUtil.createObject(dataLayer.get(new JSONDataContainer("JF/monthlyConfigs.json", "mc", "current", "type")), MonthlyConfig.class);
        
        this.monthlyPlan = buildMonthlyMap(mc.getMonthAndYear());
        this.date = date;

    }



    private Map<LocalDate, DailyPlan> buildMonthlyMap(LocalDate date) {
        HashMap<LocalDate, DailyPlan> monthlyMap = new HashMap<>();

        // Calcola il 16 del mese successivo
        LocalDate nextMonth16 = date.plusMonths(1).withDayOfMonth(16);

        // Cicla su ogni data dal giorno di oggi fino al 16 del mese successivo
        LocalDate currentDate = date;
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

    public void generateMonthlyPlan(){
        // per ogni giorno del mese
        // se il giorno è precluso non faccio nulla
        // altrimenti creo un daily plan e lo metto nella mappa
        for (LocalDate date : monthlyPlan.keySet()) {
            DailyPlan dp = monthlyPlan.get(date);
            if (!(dp == null)) {
                dp.generate();
                monthlyPlan.put(date, dp);
            }
        }

    }

    


}
