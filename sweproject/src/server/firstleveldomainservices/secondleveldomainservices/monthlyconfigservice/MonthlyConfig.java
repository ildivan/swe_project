package server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MonthlyConfig {

    //per comodita istanzio i gionri prima della visita in cui controllo se è completa hai giorni 
    //di differenza dopo i quali non posso piu iscrivere
    private static final int DAYS_BEFORE_SUBSCRIPTION_CLOSURE = 3;
    
    /*è la data in cui teoricamente genero il prossimo piano , il 16-07-2025 genero i piano dal 17-08-2025 al 17-09-2025 */
    private LocalDate monthAndYear; //key

    private Map<LocalDate, Boolean> previousPlanlanConfigured;
    private String type = "current"; //old or current depends if is the current or not
    private Map<PlanState, Boolean> planStateMap; //permette di ottenere sequenzialita
    private int sequenceSubscriptionNumber;
    private int daysBeforeActivityConfirmation;

    public MonthlyConfig(LocalDate date, Map<LocalDate, Boolean> planConfigured, Set<LocalDate> precludeDates) {
        this.monthAndYear = date;
        this.previousPlanlanConfigured = planConfigured;
        this.planStateMap = new HashMap<>(Map.of(
        PlanState.GENERAZIONE_PIANO, false,
        PlanState.MODIFICHE_APERTE, false,
        PlanState.DISPONIBILITA_APERTE, true
        ));
        this.sequenceSubscriptionNumber = 1;
        this.daysBeforeActivityConfirmation = DAYS_BEFORE_SUBSCRIPTION_CLOSURE;

    }

    public MonthlyConfig(){

    }

    public LocalDate getMonthAndYear() {
        return monthAndYear;
    }

    public void setMonthAndYear(LocalDate monthAndYear) {
        this.monthAndYear = monthAndYear;
    }

    public Map<LocalDate, Boolean> isPlanConfigured() {
        return previousPlanlanConfigured;
    }

    public void setPlanConfigured(Map<LocalDate, Boolean> planConfigured) {
        this.previousPlanlanConfigured = planConfigured;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<PlanState, Boolean> getPlanStateMap() {
        return planStateMap;
    }

    public void setPlanStateMap(Map<PlanState, Boolean> planStateMap) {
        this.planStateMap = planStateMap;
    }

    public int getSequenceSubscriptionNumber() {
        return sequenceSubscriptionNumber;
    }
    
    public void setSequenceSubscriptionNumber(int sequenceSubscriptionNumber) {
        this.sequenceSubscriptionNumber = sequenceSubscriptionNumber;
    }

    public int getDaysBeforeActivityConfirmation(){
        return this.daysBeforeActivityConfirmation;
    }

    public void setDaysBeforeActivityConfirmation(int daysBeforeActivityConfirmation){
        this.daysBeforeActivityConfirmation = daysBeforeActivityConfirmation;
    }

}
