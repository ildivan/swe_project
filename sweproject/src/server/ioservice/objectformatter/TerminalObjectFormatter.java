package server.ioservice.objectformatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Place;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityInfo;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityRecord;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.DailyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.firstleveldomainservices.volunteerservice.ConfirmedActivity;
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
        for(String s : v.getNondisponibilityDaysCurrent()){
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

    /**
     * metodo per formatare una lista di ActivityRecord, dato ottenuto dopo aver
     * selezionato le visite in base allo stato delle stesse
     * @param aList
     * @return
     */
    public String formatListActivityRecord(List<ActivityRecord> arList){
        String out = "";
         for (ActivityRecord activityRecord : arList) {
            out = out + formatActivityRecord(activityRecord); 
         }
 
         return out;
    }

    /**
     * metodo per formattare un oggetto activity record
     * @param activityRecord
     * @return
     */
    private String formatActivityRecord(ActivityRecord record) {
       if (record == null) return "null";

    return "\n\n----------------\n\n" + 
            "\n\nVisita:\n\n" +
            "Data:\n" + record.getDate() +
            "\n\nNome:\n" + record.getName() +
            "\n\nNumero di iscritti:\n" + record.getActivity().getNumberOfSub() +
            "\n\nStato visita:\n" + record.getActivity().getState() +
            "\n\nOrario della visita:\n'" + record.getActivity().getTime();
    }

    /**
     * fromatter per il piano mensile
     */
    @Override
    public String formatMonthlyPlan(MonthlyPlan monthlyPlanData) {
       
        StringBuffer output = new StringBuffer();

        LocalDate date = monthlyPlanData.getDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String dayOfPlan = date.format(formatter);

        output.append("Piano mensile generato: ").append(dayOfPlan).append("\n\n\n");
        output.append("Piano giorno per giorno:\n\n\n");

        Map<LocalDate, DailyPlan> monthlyPlan = monthlyPlanData.getMonthlyPlan();

        for (Map.Entry<LocalDate, DailyPlan> entry : monthlyPlan.entrySet()) {
        
            output.append(formatDailyPlan(entry.getValue()));

        }

        return output.toString();
    }
    
    private String formatDailyPlan(DailyPlan dailyPlan){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        StringBuffer output = new StringBuffer();
        
        LocalDate dateOfDailyPlan = dailyPlan.getDate();
        String dateOfDailyPlanString = dateOfDailyPlan.format(formatter);
        output.append("\n\n----\n\n");
        output.append("Data: ").append(dateOfDailyPlanString).append("\n\n");

        Map<String, ActivityInfo> plan = dailyPlan.getPlan();

        for (Map.Entry<String, ActivityInfo> entry : plan.entrySet()) {
            String activityName = entry.getKey();
            ActivityInfo info = entry.getValue();

            output.append("  • ").append(activityName).append("\n")
            .append("    ↳ Stato: ").append(info.getState()).append("\n")
            .append("    ↳ Orario: ").append(info.getTime()).append("\n")
            .append("    ↳ Iscritti: ").append(info.getNumberOfSub()).append("\n\n");
        }

        return output.toString();
    }

    /**
     * metodo per formattare al volontario una visita confermata
     */
    @Override
    public String formatListConfirmedActivity(List<ConfirmedActivity> actList) {

        StringBuilder out = new StringBuilder();
        for (ConfirmedActivity activity : actList) {
            out.append("\n\n\nAttività confermate\n\n");
            out.append(formatConfirmedActivity(activity));
        }
        return out.toString();
       
    }

    private Object formatConfirmedActivity(ConfirmedActivity confirmedAactivity) {
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
         StringBuffer sb = new StringBuffer();
        sb.append("\n\n----\n\n");
        sb.append("activityName='").append(confirmedAactivity.getActivityName()).append("\n");
        sb.append("numberOfSub=").append(confirmedAactivity.getNumberOfSub()).append("\n");
        sb.append("timeOfTheActivity='").append(confirmedAactivity.getTimeOfTheActivity()).append("\n");
        sb.append("dateOfTheActivity=").append(confirmedAactivity.getDateOfTheActivity().format(formatterDate)).append("\n");
        
        return sb.toString();
    }

    


}
