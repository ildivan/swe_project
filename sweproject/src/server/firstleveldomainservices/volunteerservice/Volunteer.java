package server.firstleveldomainservices.volunteerservice;

import java.util.ArrayList;
import java.util.List;

public class Volunteer {
    private String name;
    private List<String> nondisponibilityDays; //contiene le date in formato standard dd-mm-yyyy delle NON disponibilità dei volontari

    public Volunteer(String name){
        this.name = name;
        this.nondisponibilityDays = new ArrayList<>();
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name=name;
    }

    public List<String> getDisponibilityDays(){
        return nondisponibilityDays;
    }

    public void setDisponibilityDays(List<String> disponibilityDays){
        this.nondisponibilityDays=disponibilityDays;
    }

}
