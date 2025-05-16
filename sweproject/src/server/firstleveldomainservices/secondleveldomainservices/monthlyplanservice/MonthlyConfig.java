package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;
import java.util.Set;

public class MonthlyConfig {
    private LocalDate monthAndYear; //key a cui accedo, nel mese di aprile modifico il piano di maggio
    //le date disponibili dei volontari di giugno e le date non utilizzabili di luglio
    private boolean planConfigured;
    private Set<LocalDate> precludeDates;
    private String type = "current"; //old or current depends if is the current or not

    public MonthlyConfig(LocalDate date, boolean planConfigured, Set<LocalDate> precludeDates) {
        this.monthAndYear = date;
        this.planConfigured = planConfigured;
        this.precludeDates = precludeDates;
    }

    public MonthlyConfig(){

    }

    public LocalDate getMonthAndYear() {
        return monthAndYear;
    }

    public void setMonthAndYear(LocalDate monthAndYear) {
        this.monthAndYear = monthAndYear;
    }

    public boolean isPlanConfigured() {
        return planConfigured;
    }

    public void setPlanConfigured(boolean planConfigured) {
        this.planConfigured = planConfigured;
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
}
