package server.firstleveldomainservices.volunteerservice;

import java.util.LinkedHashSet;
import java.util.Set;

public class Volunteer {
    private String name;
    private Set<String> disponibilityDaysCurrent; // contiene le date in formato standard dd-mm-yyyy delle NON disponibilità dei volontari
    private Set<String> disponibilityDaysOld;

    public Volunteer(String name) {
        this.name = name;
        this.disponibilityDaysCurrent = new LinkedHashSet<>();
        this.disponibilityDaysOld = new LinkedHashSet<>();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getDisponibilityDaysCurrent() {
        return disponibilityDaysCurrent;
    }

    public void setDisponibilityDaysCurrent(Set<String> disponibilityDaysCurrent) {
        this.disponibilityDaysCurrent = disponibilityDaysCurrent;
    }

    public Set<String> getDisponibilityDaysOld() {
        return disponibilityDaysOld;
    }

    public void setDisponibilityDaysOld(Set<String> disponibilityDaysOld) {
        this.disponibilityDaysOld = disponibilityDaysOld;
    }
}
