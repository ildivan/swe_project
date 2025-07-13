package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import server.data.facade.FacadeHub;
import server.data.facade.interfaces.IPrecludeDateFacade;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
public class MonthlyPlan{

    private LocalDate date;
    private Map<LocalDate,DailyPlan> monthlyPlan;

    //questo non deve essere serializzato -> inserisco transient per risolvere il problema
    private transient IPrecludeDateFacade precludeDateService;
    private transient FacadeHub data;

    public MonthlyPlan(Map<LocalDate, DailyPlan> montlyPlan, LocalDate date,
    IPrecludeDateFacade precludeDateService,
    FacadeHub data) {

        this.monthlyPlan = montlyPlan;
        this.date = date;
        this.precludeDateService = precludeDateService;
        this.data = data;
    }

    public MonthlyPlan(LocalDate date,
    IPrecludeDateFacade precludeDateService,
    FacadeHub data) {
        
        this.date = date;
        this.precludeDateService = precludeDateService;
        this.monthlyPlan = buildMonthlyMap();
        this.data = data;

    }



    private Map<LocalDate, DailyPlan> buildMonthlyMap() {

        HashMap<LocalDate, DailyPlan> monthlyMap = new LinkedHashMap<>();

        MonthlyConfig mc = data.getMonthlyConfigFacade().getMonthlyConfig();

        //Data piano
        LocalDate date;
        
        date = mc.getMonthAndYear();
        
        

        // Primo giorno del mese successivo
        LocalDate startDate = date.plusMonths(1).withDayOfMonth(1);

        // Ultimo giorno del mese successivo
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // Ciclo dal primo all'ultimo giorno del mese successivo
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (isPrecludeDate(currentDate, date)) {
                monthlyMap.put(currentDate, null);
            } else {
                monthlyMap.put(currentDate, new DailyPlan(currentDate));
            }

            currentDate = currentDate.plusDays(1);
        }

        return monthlyMap;
    }

    /**
     * method to check if is a preclude date for the current plan
     * @param currentDate
     * @return
     */
    private boolean isPrecludeDate(LocalDate currentDate, LocalDate dateOfPlanGeneration) {
        return precludeDateService.checkIfIsPrecludeDate(currentDate, dateOfPlanGeneration);
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
