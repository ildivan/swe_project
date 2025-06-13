package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.monthlyconfig;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MonthlyConfig {
    private LocalDate monthAndYear; //key a cui accedo, nel mese di aprile modifico il piano di maggio
    //le date disponibili dei volontari di giugno e le date non utilizzabili di luglio
    private Map<LocalDate, Boolean> previousPlanlanConfigured;
    private Set<LocalDate> precludeDates;
    private String type = "current"; //old or current depends if is the current or not
    private Map<PlanState, Boolean> planStateMap; //permette di ottenere sequenzialita

    public MonthlyConfig(LocalDate date, Map<LocalDate, Boolean> planConfigured, Set<LocalDate> precludeDates) {
        this.monthAndYear = date;
        this.previousPlanlanConfigured = planConfigured;
        this.precludeDates = precludeDates;
        this.planStateMap = new HashMap<>(Map.of(
        PlanState.GENERAZIONE_PIANO, true,
        PlanState.MODIFICHE_APERTE, false,
        PlanState.DISPONIBILITA_APERTE, false
));


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

    public Set<LocalDate> getPrecludeDates() {
        return precludeDates;
    }

    public void setPrecludeDates(Set<LocalDate> precludeDates) {
        this.precludeDates = precludeDates;
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

}
