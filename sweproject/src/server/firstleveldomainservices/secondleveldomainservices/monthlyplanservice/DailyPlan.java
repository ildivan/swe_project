package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import com.google.gson.JsonObject;
import server.DateService;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.volunteerservice.Volunteer;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;

public class DailyPlan {
  
    private LocalDate date;
    private Map<String, ActivityInfo> plan = new HashMap<>();

    //non deve essere serializzato -> inserisco transient
    private transient ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory = new JsonLocInfoFactory();
    private transient IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private transient DateService dateService = new DateService();
    private transient IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();
        
    

    public DailyPlan(LocalDate date) {
        this.date = date;
    }

    /*
     * mi genera il piano giornaliero delle attività
     */
    public DailyPlan generate(List<Activity> activity){
       List<Activity> bestActivities = getBestCombination(getPossibleActivities(activity));

       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Activity a: bestActivities) {
            String title = a.getTitle();
            String orario = a.getProgrammableHour().format(formatter) + " - " + a.getEndTime().format(formatter);
            ActivityInfo activityInfo = new ActivityInfo(0, ActivityState.PROPOSTA, orario);
            plan.put(title, activityInfo);
        }
        return this;
    }
    
   /**
    * mi genera la lista delle attività possibili nel giorno in cui sto facendo il piano (solo la lista delle attività possibili non il piano)
    * @param activities
    * @return
    */
    private List<Activity> getPossibleActivities(List<Activity> activities){
        List<Activity> actPossibleToday = new ArrayList<>();
        for (Activity act : activities){
            //controllo se il gionro in cui sto facendo il piano (attributo date) è compreso nel periodo di esecuzione della visita
            //controllo se il giorno della settimana della visita è quello in cui sto facendo il piano
            //aggiiungo l'attivita alle visite possibili
            if(isProgrammablePeriodCheck(act) && isOnCorrectDay(act) && checkIfVolunteersAreFree(act)){
                actPossibleToday.add(act);
            }
        }

        return actPossibleToday;
    }

    private boolean checkIfVolunteersAreFree(Activity activity){
        String [] volunteers = activity.getVolunteers();
       
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        
        for (String name : volunteers) {

            locInfo.setKey(name);

            JsonObject volunteerJO = dataLayer.get(locInfo);
            Volunteer volunteer = jsonFactoryService.createObject(volunteerJO, Volunteer.class);

            for (String d : volunteer.getNondisponibilityDaysOld()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String formattedDate = date.format(formatter);

                if(d.equalsIgnoreCase(formattedDate.toString())){
                    return false;
                }
            }

        }
        return true;
    }

    /**
     * controllo se sono nel periodo di programmazione possibile della visita
     * @param a
     * @return
     */
    private boolean isProgrammablePeriodCheck(Activity a){
        return dateService.checkIfIsBetween(date, a.getFirstProgrammableDate(), a.getLastProgrammableDate());
    }

    /**
     * controllo se il giorno della settimana in cui faccio il piano è quello in cui ho possibilità di realizzare la visita
     * @param a
     * @return
     */
    private boolean isOnCorrectDay(Activity a){
        String dayOfTheWeekofDate = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
        dayOfTheWeekofDate = capitalize(dayOfTheWeekofDate);
        dayOfTheWeekofDate = dayOfTheWeekofDate.replace('ì', 'i');
        return Arrays.asList(a.getProgrammableDays()).contains(dayOfTheWeekofDate); 
    }

    /**
     * metodo per costruire il piano di visite giornaliero
     * sfrutta un algoritmo greedy che mette in ordine crescente di orario fine attività
     * @param act
     * @return
     */
 private List<Activity> getBestCombination(List<Activity> activities) {
    // Ordina per orario di fine (attività più "presto" finiscono prima)
    activities.sort(Comparator.comparing(Activity::getEndTime));

    List<Activity> result = new ArrayList<>();
    Set<String> usedVolunteers = new HashSet<>();
    Map<String, List<Activity>> activitiesByPlace = new HashMap<>();

    for (Activity activity : activities) {
        String volunteer = activity.getVolunteers()[0];
        String place = activity.getPlaceName();

        if (usedVolunteers.contains(volunteer)) {
            continue; // già usato
        }

        boolean overlap = false;
        List<Activity> samePlace = activitiesByPlace.getOrDefault(place, new ArrayList<>());

        for (Activity existing : samePlace) {
            // Se si sovrappongono temporalmente nel luogo
            if (!(existing.getEndTime().isBefore(activity.getProgrammableHour())
               || activity.getEndTime().isBefore(existing.getProgrammableHour()))) {
                overlap = true;
                break;
            }
        }

        if (!overlap) {
            result.add(activity);
            usedVolunteers.add(volunteer);
            samePlace.add(activity);
            activitiesByPlace.put(place, samePlace);
        }
    }

    return result;
}


    

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Map<String, ActivityInfo> getPlan() {
        return plan;
    }

    public void setPlan(Map<String, ActivityInfo> plan) {
        this.plan = plan;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
