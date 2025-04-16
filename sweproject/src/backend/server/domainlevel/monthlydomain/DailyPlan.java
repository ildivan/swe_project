package backend.server.domainlevel.monthlydomain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import backend.server.domainlevel.Activity;
import backend.server.genericservices.DateUtil;

public class DailyPlan {
    private LocalDate date;
    private Map<String, String> plan = new HashMap<>();

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
    
    /*
     * mi genera la lista delle attività possibili nel giorno in cui sto facendo il piano (solo la lista delle attività possibili non il piano)
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

    /*
     * controllo se sono nel periodo di programmazione possibile della visita
     */
    private boolean isProgrammablePeriodCheck(Activity a){
        return DateUtil.checkIfIsBetween(date, a.getFirstProgrammableDate(), a.getLastProgrammableDate());
    }

    /*
     * controllo se il giorno della settimana in cui faccio il piano è quello in cui ho possibilità di realizzare la visita
     */
    private boolean isOnCorrectDay(Activity a){
        String dayOfTheWeekofDate = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
        dayOfTheWeekofDate = capitalize(dayOfTheWeekofDate);
        dayOfTheWeekofDate = dayOfTheWeekofDate.replace('ì', 'i');
        return Arrays.asList(a.getProgrammableDays()).contains(dayOfTheWeekofDate); 
    }

    /*
     * ottiene tra tutte le combinazioni possibili quella che ha piu ore occupate nella giornata
     */
    private List<Activity> getBestCombination(List<Activity> act){
        act.sort(Comparator.comparing(Activity::getEndTime));

        int n = act.size();
        Duration[] dp = new Duration[n];
        int[] previous = new int[n];

        for (int i = 0; i < n; i++) {
            // Durata dell'attività corrente
            dp[i] = act.get(i).getDurationAsDuration();

            // Trova la precedente attività che NON si sovrappone
            previous[i] = -1;
            for (int j = i - 1; j >= 0; j--) {
                if (!act.get(j).getEndTime().isAfter(act.get(i).getProgrammableHour())) {
                    previous[i] = j;
                    break;
                }
            }

            if (previous[i] != -1) {
                dp[i] = dp[i].compareTo(dp[previous[i]].plus(act.get(i).getDurationAsDuration())) < 0
                        ? dp[previous[i]].plus(act.get(i).getDurationAsDuration())
                        : dp[i];
            }

            if (i > 0 && dp[i - 1].compareTo(dp[i]) > 0) {
                dp[i] = dp[i - 1];
            }
        }

        // Ricostruisci le attività selezionate
        List<Activity> result = new ArrayList<>();
        for (int i = n - 1; i >= 0;) {
            boolean include = false;
            if (i == 0 || dp[i].compareTo(dp[i - 1]) > 0) {
                include = true;
            }

            if (include) {
                result.add(0, act.get(i));
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
