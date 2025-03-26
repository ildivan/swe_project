package backend.server.domainlevel;

import java.util.ArrayList;
import java.util.List;

public class VolunteerData {
    private String name;
    private List<String> nondisponibilityDays; //contiene le date in formato standard dd-mm-yyyy delle NON disponibilità dei volontari

    public VolunteerData(String name){
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

    public String toString(){
        StringBuffer s = new StringBuffer();
        s.append("\n\n------------\n\n");
        s.append(String.format("Nome volontario: %s", getName()));
        s.append((String.format("\nGiorni in cui il volontario non è libero: %s", dispDaysToString())));
        return s.toString();
    }

    private String dispDaysToString(){
        String out ="";
        for(String s : nondisponibilityDays){
            out = out + s + ", ";
        }

        return out;
    }
}
