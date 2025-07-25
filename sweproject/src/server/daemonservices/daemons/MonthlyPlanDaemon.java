package server.daemonservices.daemons;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lock.MonthlyPlanLockManager;
import server.daemonservices.IDaemon;
import server.data.facade.FacadeHub;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityInfo;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.DailyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.utils.ConfigType;
import server.data.Activity;
import server.data.Volunteer;

public class MonthlyPlanDaemon implements IDaemon{

    private Map<String, Activity> activities;
    private Map<String, Volunteer> volunteers;
    private FacadeHub data;

    public MonthlyPlanDaemon(ConfigType configType, FacadeHub data) {
        this.data = data;
    }
    //ogni secondo viene chiamato il metodo tick che esegue il compito del demone
    @Override
    public void run() {
        while (true) {
            try {
                tick();
                Thread.sleep(5000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }


    @Override
    public void tick() {
        boolean lockAcquired = false;
        try {
            lockAcquired = MonthlyPlanLockManager.tryLock(2, TimeUnit.SECONDS);

            if (!lockAcquired) {
                System.out.println("Demone: impossibile acquisire il lock, operazione saltata.");
                return;
            }

            MonthlyPlan monthlyPlan = data.getMonthlyPlanFacade().getMonthlyPlan();
            if (monthlyPlan == null) return;

            //------------
            /*
             * parte di codice solo per dimostrazione all'esame
             */
            if(monthlyPlan.getDate().isEqual(LocalDate.of(2025, 06, 16))){
                return;
            }
            //------------

            activities = readAllActivities();
            volunteers = readAllVolunteers();

            data.getMonthlyPlanFacade().checkIfActivitiesNeedToBeArchived(monthlyPlan);
            monthlyPlan = checkActivities(monthlyPlan);

            data.getMonthlyPlanFacade().refreshMonthlyPlan(monthlyPlan);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Demone interrotto durante l’attesa del lock.");
        } finally {
            if (lockAcquired && MonthlyPlanLockManager.isHeldByCurrentThread()) {
                MonthlyPlanLockManager.unlock();
            }
        }
    }


    /**
     * method to read activities data to avoid logical race condition while refreshing data afther monthlyplan generation
     * @return
     */
    private Map<String, Activity> readAllActivities() {
        //leggo dal file non modificato ovviamente, in quanto il piano attuale si basa su di esso
        
        Map<String, Activity> map = new HashMap<>();
        for (Activity a : data.getActivitiesFacade().getDaemonActivities()) {
            map.put(a.getTitle(), a);
        }
        return map;
    }


    /**
     * method to read volunteers info to avoid logical race condition while refreshing data afther monthlyplan generation
     * @return
     */
    private Map<String, Volunteer> readAllVolunteers() {
        Map<String, Volunteer> map = new HashMap<>();
        for (Volunteer v : data.getVolunteersFacade().getVolunteers()) {
            map.put(v.getName(), v);
        }
        return map;
    }


    /**
     * controlla se è necessaria una variazione di stato in ogni attività
     * @param monthlyPlan
     */
    private MonthlyPlan checkActivities(MonthlyPlan monthlyPlan) {
        Map<LocalDate, DailyPlan> monthlyPlanMap = monthlyPlan.getMonthlyPlan();

        for (Map.Entry<LocalDate, DailyPlan> dailyEntry : monthlyPlanMap.entrySet()) {
            LocalDate date = dailyEntry.getKey();
            DailyPlan dailyPlan = dailyEntry.getValue();
            Map<String, ActivityInfo> activityMap = dailyPlan.getPlan();

            Map<String, ActivityInfo> updates = new HashMap<>();
            List<String> removals = new ArrayList<>();

            for (Map.Entry<String, ActivityInfo> activityEntry : activityMap.entrySet()) {
                String activityName = activityEntry.getKey();
                Activity activity = getActivity(activityName);
                ActivityInfo activityInfo = activityEntry.getValue();

                if(needsToBeDeleted(activityInfo, date)) {
                    removals.add(activityName); 
                } else {
                    ActivityInfo updatedInfo = checkActivityState(activityInfo, activity, date);
                    updates.put(activityName, updatedInfo);
                }
            }

            // rimuovo per evitare race conditions lo faccio dopo
            for (String activityName : removals) {
                activityMap.remove(activityName);
            }

            // aggiorno dopo per evitare race conditions
            for (Map.Entry<String, ActivityInfo> update : updates.entrySet()) {
                activityMap.put(update.getKey(), update.getValue());
            }

            dailyPlan.setPlan(activityMap);
            monthlyPlanMap.put(date, dailyPlan);
        }

        return monthlyPlan;
    }


    /**
     * metodo per controllare se una visita è definitivamente da eliminare
     * @param activityInfo
     * @param dateOfActivity
     * @return
     */
    private boolean needsToBeDeleted(ActivityInfo activityInfo, LocalDate dateOfActivity) {
        if((activityInfo.getState() == ActivityState.CANCELLATA) && ((ChronoUnit.DAYS.between(LocalDate.now(), dateOfActivity))<0)){
            return true;
        }

        return false;
    }


    /**
     * metodo per controllare eventuali cambio di stato su una visita
     */
    private ActivityInfo checkActivityState(ActivityInfo activityInfo, Activity activity, LocalDate date) {
        //controllo se è al completo
        if(!(activityInfo.getState() == ActivityState.CONFERMATA || activityInfo.getState() == ActivityState.EFFETTUATA)){
            if(checkIfActivitiesHaveMaxNumberOfSubscriptions(activityInfo, activity)){
                activityInfo.setState(ActivityState.COMPLETA);
            }else{
                if(!(ChronoUnit.DAYS.between(LocalDate.now(), date)<=0)){
                    //faccio cosi perche se qualcuno disdice almeno torna proposta
                    activityInfo.setState(ActivityState.PROPOSTA);
                }
                
            }
        }

        //controllo se è possibile confermarla, altimenti elimino
        if(isTimeToCheckActivityConfirmation(date)){

            if(checkIfActivityCanBeConfirmed(activityInfo, activity, date)){
                //se posso confermarla la confermo
                activityInfo.setState(ActivityState.CONFERMATA);
            }else{
                //altrimenti la elimino
                activityInfo.setState(ActivityState.CANCELLATA);
            }
            
        }

        //controllo se è eseguita
        if(checkIfActivitiesNeedAreDone(activityInfo, activity, date)){
            activityInfo.setState(ActivityState.EFFETTUATA);
        }
        
        return activityInfo;
    }


    /**
     * metodo per controllare se ci sono attività al completo
     * @param activityInfo
     * @param activity
     * @return
     */
    private boolean checkIfActivitiesHaveMaxNumberOfSubscriptions(ActivityInfo activityInfo, Activity activity) {
        if(activityInfo.getNumberOfSub()==activity.getMaxPartecipanti()){
            return true;
        }
        return false;
    }


    /**
     * metodo che controlla se una attività deve essere confermata
     * @param activityInfo
     * @param activity
     * @return
     */
    private boolean checkIfActivityCanBeConfirmed(ActivityInfo activityInfo, Activity activity, LocalDate date) {
        if(minNumberOfSubsReached(activityInfo, activity) && isVolunteerAvailable(activity, date)){
            return true;
        }
        return false;
    }

    /**
     * util method to checkIfActivitiesNeedToBeConfirmed,
     * controlla se il volontario è disponibile tale data
     * 
     * @param activity
     * @param dateOfActivity
     * @return
     */
    private boolean isVolunteerAvailable (Activity activity, LocalDate dateOfActivity) {
        String[] names = activity.getVolunteers();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = dateOfActivity.format(formatter);

        for (String name : names) {
            Volunteer volunteer = volunteers.get(name);
            if (volunteer == null || volunteer.getDisponibilityDaysOld() == null) continue;

            for (String d : volunteer.getDisponibilityDaysOld()) {
                if (d.equalsIgnoreCase(formattedDate)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * util method to checkIfActivitiesNeedToBeConfirmed,
     * controlla se ho raggiunto il numero minimo di iscrizioni
     * @return
     */
    private boolean minNumberOfSubsReached(ActivityInfo activityInfo, Activity activity) {
        if(activityInfo.getNumberOfSub()>=activity.getMinPartecipanti()){
            return true;
        }

        return false;
    }


    /**
     * util method to checkIfActivitiesNeedToBeConfirmed,
     * controlla se ho superato la data per vericare le iscrizioni minime
     * @return
     */
    private boolean isTimeToCheckActivityConfirmation(LocalDate date) {
       LocalDate dateOfToday = LocalDate.now();
       long daysOfDifference = ChronoUnit.DAYS.between(dateOfToday, date); //positivo se data > dateOfToday

       if (daysOfDifference>0) {
            if(daysOfDifference<getDaysBeforeConfirmation()){
                return true;
            }
       }

       return false;
    }


    /**
     * util method to isTimeToCheckActivityConfirmation,
     * metodo per ottenere a quanti gionri da una attività devo controllare se è confermata
     * @return
     */
    private long getDaysBeforeConfirmation() {
        MonthlyConfig mc = data.getMonthlyConfigFacade().getMonthlyConfig();
        return mc.getDaysBeforeActivityConfirmation();
    }


    /**
     * metodo per controllare se una attività è stata eseguita
     * @param activityInfo
     * @param activity
     * @return
     */
    private boolean checkIfActivitiesNeedAreDone(ActivityInfo activityInfo, Activity activity, LocalDate date) {
        if(ChronoUnit.DAYS.between(date, LocalDate.now())>0 && activityInfo.getState()==ActivityState.CONFERMATA){
            return true;
        }

        return false;
    }


    /**
     * ottengo i dati dell'attività
     * @param name
     * @return
     */
    private Activity getActivity(String name) {
        return activities.get(name);
    }
    
}
