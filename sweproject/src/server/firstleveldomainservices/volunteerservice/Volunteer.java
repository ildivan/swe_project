package server.firstleveldomainservices.volunteerservice;

import java.util.LinkedHashSet;
import java.util.Set;

public class Volunteer {
    private String name;
    private Set<String> nondisponibilityDays; //contiene le date in formato standard dd-mm-yyyy delle NON disponibilità dei volontari

    public Volunteer(String name){
        this.name = name;
        this.nondisponibilityDays = new LinkedHashSet<>();
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name=name;
    }

    public Set<String> getDisponibilityDays(){
        return nondisponibilityDays;
    }

    public void setDisponibilityDays(Set<String> disponibilityDays){
        this.nondisponibilityDays=disponibilityDays;
    }

}
