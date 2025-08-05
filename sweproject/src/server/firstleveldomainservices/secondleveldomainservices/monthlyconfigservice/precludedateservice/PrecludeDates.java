package server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.precludedateservice;

import java.time.LocalDate;
import java.util.Set;

public class PrecludeDates {
    private LocalDate dateOfPlan; //16 del mese prima a cui si riferiscono le date, che sarà il giorno ideale in cui faccio il piano
    private Set<LocalDate> precludeDates;

    public PrecludeDates(LocalDate dateOfPlan, Set<LocalDate> precludeDates) {
        this.dateOfPlan = dateOfPlan; 
        this.precludeDates = precludeDates;
    }

    public LocalDate getDateOfPlan() {
        return dateOfPlan;
    }

    public void setDateOfPlan(LocalDate dateOfPlan) {
        this.dateOfPlan = dateOfPlan;
    }

    public Set<LocalDate> getPrecludeDates() {
        return precludeDates;
    }

    public void setPrecludeDates(Set<LocalDate> precludeDates) {
        this.precludeDates = precludeDates;
    }

    public void addPrecludeDate(LocalDate precludeDate) {
        precludeDates.add(precludeDate);
    }
}
