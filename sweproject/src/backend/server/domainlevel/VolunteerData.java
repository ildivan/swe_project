package backend.server.domainlevel;

import java.util.ArrayList;

public class VolunteerData {
    private String name;
    private ArrayList<String> disponibilityDays; //contiene le date in formato standard dd-mm-yyyy delle disponibilità dei volontari

    public VolunteerData(String name){
        this.name = name;
        this.disponibilityDays = new ArrayList<>();
    }
}
