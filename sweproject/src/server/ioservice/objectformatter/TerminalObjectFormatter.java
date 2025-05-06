package server.ioservice.objectformatter;

import java.time.format.DateTimeFormatter;
import java.util.List;

import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Place;
import server.firstleveldomainservices.volunteerservice.Volunteer;

public class TerminalObjectFormatter implements IIObjectFormatter<String> {
    
    /**
     * metodo per formattare a stringa una attività
     * @param a
     * @return
     */
    public String formatActivity(Activity a){
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        StringBuffer out = new StringBuffer();
        out.append("\n\n----------------\n\n");
        out.append(String.format("Attività: %s", a.getTitle()));
        out.append(String.format("\n\nDescrizione attività: %s", a.getDescription()));
        out.append(String.format("\n\nPunto di ritrovo: %s", formatAddress(a.getMeetingPoint())));
        out.append(String.format("\n\nMassimo numero partecipanti: %s", a.getMaxPartecipanti()));
        out.append(String.format("\nPrima data utile: %s", a.getFirstProgrammableDate().format(formatterDate)));
        out.append(String.format("\nUltima data utile: %s", a.getLastProgrammableDate().format(formatterDate)));
        out.append(String.format("\nOra di inizio: %s", a.getProgrammableHour().format(formatter)));
        out.append(String.format("\nDurata: %s", a.getDurationAsLocalTime().format(formatter)));
        out.append(String.format("\nVolontari associati: %s", getVolunteersToString(a.getVolunteers())));
        return out.toString();
    }

    /**
     * metodo di utilità a quello sopra per ottenere una rappresentazione a stringa
     * dei volontari presenti nell'attività
     * @param volunteers
     * @return
     */
    private String getVolunteersToString(String[] volunteers){
        String out = "";
        for(String s :volunteers){
            out = out + s + ", ";
        }
        return out;
    }

    private String formatAddress(Address a){
        return "\nVia: " + a.getStreet() + "\nCittà: " + a.getCity() + "\nCodice Postale: " + a.getZipCode() + "\nStato: " + a.getState();
    }

    /**
     * metodo per formatare una lista di attività
     * @param aList
     * @return
     */
    public String formatListActivity(List<Activity> aList){
       String out = "";
        for (Activity activity : aList) {
           out = out + formatActivity(activity); 
        }

        return out;
    }

    /**
     * metodo per formattare un volontario
     * @param v
     * @return
     */
    public String formatVolunteer(Volunteer v){
        StringBuffer s = new StringBuffer();
        s.append("\n\n------------\n\n");
        s.append(String.format("Nome volontario: %s", v.getName()));
        s.append((String.format("\nGiorni in cui il volontario non è libero: %s", dispDaysToString(v))));
        return s.toString();
    }

    private String dispDaysToString(Volunteer v){
        String out ="";
        for(String s : v.getDisponibilityDays()){
            out = out + s + ", ";
        }

        return out;
    }

    /**
     * metodo per formatare una lista di volontari
     * @param aList
     * @return
     */
    public String formatListVolunteer(List<Volunteer> vList){
        String out = "";
         for (Volunteer volunteer : vList) {
            out = out + formatVolunteer(volunteer); 
         }
 
         return out;
    }

    /**
     * metoodo per formattare un oggetto place
     * @param p
     * @return
     */
    public String formatPlace(Place p){
    
        return "------------"+"\nPLACE " + "\nName: " + p.getName() +"\nAddress:" + formatAddress(p.getAddress()) + "\nDescription: " + p.getDescription()+  "\n------------";
    }

    /**
     * metodo per formatare una lista di place
     * @param aList
     * @return
     */
    public String formatListPlace(List<Place> pList){
        String out = "";
         for (Place place : pList) {
            out = out + formatPlace(place); 
         }
 
         return out;
    }
}
