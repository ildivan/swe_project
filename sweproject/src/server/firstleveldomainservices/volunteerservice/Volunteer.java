package server.firstleveldomainservices.volunteerservice;

import java.util.LinkedHashSet;
import java.util.Set;

public class Volunteer {
    private String name;
    private Set<String> nondisponibilityDaysCurrent; // contiene le date in formato standard dd-mm-yyyy delle NON disponibilità dei volontari
    private Set<String> nondisponibilityDaysOld;

    public Volunteer(String name) {
        this.name = name;
        this.nondisponibilityDaysCurrent = new LinkedHashSet<>();
        this.nondisponibilityDaysOld = new LinkedHashSet<>();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getNondisponibilityDaysCurrent() {
        return nondisponibilityDaysCurrent;
    }

    public void setNondisponibilityDaysCurrent(Set<String> nondisponibilityDaysCurrent) {
        this.nondisponibilityDaysCurrent = nondisponibilityDaysCurrent;
    }

    public Set<String> getNondisponibilityDaysOld() {
        return nondisponibilityDaysOld;
    }

    public void setNondisponibilityDaysOld(Set<String> nondisponibilityDaysOld) {
        this.nondisponibilityDaysOld = nondisponibilityDaysOld;
    }
}
