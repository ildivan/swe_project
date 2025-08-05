package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import server.DateService;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.Subscription;

public class DailyPlan {
  
    private LocalDate date;
    private Map<String, ActivityInfo> plan = new HashMap<>();

    //non deve essere serializzato -> inserisco transient
    private transient DateService dateService = new DateService();
        
    

    public DailyPlan(LocalDate date, ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory) {
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
            if(isProgrammablePeriodCheck(act) && isOnCorrectDay(act)){
                actPossibleToday.add(act);
            }
        }

        return actPossibleToday;
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
     * @param activities
     * @return
     */
    private List<Activity> getBestCombination(List<Activity> activities) {
        activities.sort(Comparator.comparing(Activity::getEndTime));

        List<Activity> result = new ArrayList<>();
        Set<String> usedVolunteers = new HashSet<>();
        Map<String, List<Activity>> activitiesByPlace = new HashMap<>();

        for (Activity activity : activities) {
            String[] volunteers = activity.getVolunteers();

            if (volunteers == null || volunteers.length == 0) {
                continue;
            }

            //Normalizzo tutti i volontari: trim + lowercase
            List<String> normalizedVolunteers = Arrays.stream(volunteers)
                .map(v -> v.trim().toLowerCase())
                .toList();

            //Se uno qualsiasi dei volontari è già usato quel giorno, skip
            boolean anyUsed = normalizedVolunteers.stream().anyMatch(usedVolunteers::contains);
            if (anyUsed) {
                continue;
            }

            String place = activity.getPlaceName();
            List<Activity> samePlace = activitiesByPlace.getOrDefault(place, new ArrayList<>());

            boolean overlap = false;
            for (Activity existing : samePlace) {
                if (!(existing.getEndTime().isBefore(activity.getProgrammableHour())
                || activity.getEndTime().isBefore(existing.getProgrammableHour()))) {
                    overlap = true;
                    break;
                }
            }

            if (!overlap) {
                result.add(activity);
                usedVolunteers.addAll(normalizedVolunteers); // 🔒 blocco tutti i volontari usati
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

    /**
     * metodo per rimuovere una iscrizione su un'attività
     * @param activityName nome dell'attività
     * @param subscriptionId id dell'iscrizione da rimuovere
     */
    public void removeSubscriptionOnActivity(Subscription subscription) {
        String activityName = subscription.getActivityName();
        if (plan.containsKey(activityName)) {
            ActivityInfo activityInfo = plan.get(activityName);
            activityInfo.removeSubscription(subscription);
        }
    }
}
