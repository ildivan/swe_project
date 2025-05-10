package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

import com.google.gson.JsonObject;

import server.DateService;
import server.datalayerservice.DataLayerDispatcherService;
import server.datalayerservice.JsonDataLocalizationInformation;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.volunteerservice.Volunteer;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;

public class DailyPlan {
    private static final String VOLUNTEER_PATH = "JF/volunteers.json";
    private static final String VOLUNTEER_MEMBER_NAME = "volunteers";
    private static final String VOLUNTEER_KEY_DESC = "name";
    private LocalDate date;
    private Map<String, String> plan = new HashMap<>();

    //non deve essere serializzato -> inserisco transient
    private transient IJsonFactoryService jsonFactoryService = new JsonFactoryService();
        
    

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
            plan.put(title, orario);
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
       
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(VOLUNTEER_PATH);
        locInfo.setMemberName(VOLUNTEER_MEMBER_NAME);
        locInfo.setKeyDesc(VOLUNTEER_KEY_DESC);
        
        for (String name : volunteers) {

            locInfo.setKey(name);

            JsonObject volunteerJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.get(locInfo));
            Volunteer volunteer = jsonFactoryService.createObject(volunteerJO, Volunteer.class);

            for (String d : volunteer.getDisponibilityDays()) {
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
        return (Boolean) DateService.Service.CHECK_IF_BETWEEN.start(date, a.getFirstProgrammableDate(), a.getLastProgrammableDate());
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
     * @param act
     * @return
     */
    private List<Activity> getBestCombination(List<Activity> act) {
        act.sort(Comparator.comparing(Activity::getEndTime));
    
        int n = act.size();
        Duration[] dp = new Duration[n];
        int[] previous = new int[n];
    
        for (int i = 0; i < n; i++) {
            dp[i] = act.get(i).getDurationAsDuration();
            previous[i] = -1;
    
            for (int j = i - 1; j >= 0; j--) {
                boolean samePlace = act.get(i).getPlaceName().equals(act.get(j).getPlaceName());
                boolean nonOverlapping = !act.get(j).getEndTime().isAfter(act.get(i).getProgrammableHour());
    
                // Attività compatibili se:
                // - stesso luogo ma non si sovrappongono
                // - luoghi diversi (sovrapposizione consentita)
                if ((samePlace && nonOverlapping) || !samePlace) {
                    Duration withPrev = dp[j].plus(act.get(i).getDurationAsDuration());
                    if (withPrev.compareTo(dp[i]) > 0) {
                        dp[i] = withPrev;
                        previous[i] = j;
                    }
                }
            }
    
            if (i > 0 && dp[i - 1].compareTo(dp[i]) > 0) {
                dp[i] = dp[i - 1];
                previous[i] = previous[i - 1];
            }
        }
    
        // Ricostruzione
        List<Activity> result = new ArrayList<>();
        Set<String> usedVolunteers = new HashSet<>();
    
        for (int i = n - 1; i >= 0;) {
            boolean include = false;
            Activity current = act.get(i);
            String volunteer = current.getVolunteers()[0];
    
            // Check se dp[i] è migliore di dp[i-1] e il volontario non è già stato usato
            if ((i == 0 || dp[i].compareTo(dp[i - 1]) > 0) && !usedVolunteers.contains(volunteer)) {
                // Inoltre verifichiamo che non ci siano attività già selezionate nello stesso luogo che si sovrappongono
                boolean overlapSamePlace = false;
                for (Activity selected : result) {
                    if (selected.getPlaceName().equals(current.getPlaceName())) {
                        if (!(selected.getEndTime().isBefore(current.getProgrammableHour()) || current.getEndTime().isBefore(selected.getProgrammableHour()))) {
                            overlapSamePlace = true;
                            break;
                        }
                    }
                }
    
                if (!overlapSamePlace) {
                    include = true;
                }
            }
    
            if (include) {
                result.add(0, current);
                usedVolunteers.add(volunteer);
                i = previous[i];
            } else {
                i--;
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

    public Map<String, String> getPlan() {
        return plan;
    }

    public void setPlan(Map<String, String> plan) {
        this.plan = plan;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
