package server.demonservices.demons;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonObject;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.demonservices.IDemon;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityInfo;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.DailyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlanService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.PerformedActivity;
import server.firstleveldomainservices.volunteerservice.Volunteer;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;
import server.utils.ConfigType;

public class MonthlyPlanDemon implements IDemon{


    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory;
    private IDataLayer<JsonDataLocalizationInformation> dataLayer;
    private MonthlyPlanService monthlyPlanService;
    private MonthlyConfigService monthlyConfigService;

    public MonthlyPlanDemon(ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory, 
    ConfigType configType, IDataLayer<JsonDataLocalizationInformation> dataLayer) {
        this.locInfoFactory = locInfoFactory;
        this.dataLayer = dataLayer;
        this.monthlyPlanService = new MonthlyPlanService(locInfoFactory, configType, dataLayer);
        this.monthlyConfigService = new MonthlyConfigService(locInfoFactory, dataLayer);
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
       MonthlyPlan monthlyPlan = monthlyPlanService.getMonthlyPlan();

       if(monthlyPlan == null){
        return;
       }

       checkIfActivitiesNeedToBeArchived(monthlyPlan);
       monthlyPlan = checkActivities(monthlyPlan);
       
       //salvo i cambiamenti
       monthlyPlanService.refreshMonthlyPlan(monthlyPlan);
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

                if (needsToBeDeleted(activityInfo, activity, date)) {
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
     * @param activity
     * @param dateOfActivity
     * @return
     */
    private boolean needsToBeDeleted(ActivityInfo activityInfo, Activity activity, LocalDate dateOfActivity) {
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
        if(checkIfActivitiesHaveMaxNumberOfSubscriptions(activityInfo, activity)){
            activityInfo.setState(ActivityState.COMPLETA);
        }else{
            //faccio cosi perche se qualcuno disdice almeno torna proposta
            activityInfo.setState(ActivityState.PROPOSTA);
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
        String [] volunteers = activity.getVolunteers();
       
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        
        for (String name : volunteers) {

            locInfo.setKey(name);

            JsonObject volunteerJO = dataLayer.get(locInfo);
            Volunteer volunteer = jsonFactoryService.createObject(volunteerJO, Volunteer.class);

            if(volunteer.getDisponibilityDaysOld()==null || volunteer.getDisponibilityDaysOld().isEmpty()){
                return false; 
            }

            for (String d : volunteer.getDisponibilityDaysOld()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String formattedDate = dateOfActivity.format(formatter);

                if(d.equalsIgnoreCase(formattedDate.toString())){
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
        MonthlyConfig mc = monthlyConfigService.getMonthlyConfig();
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
        JsonDataLocalizationInformation locInfo = locInfoFactory.getActivityLocInfo();
        locInfo.setKey(name);
        return jsonFactoryService.createObject(dataLayer.get(locInfo), Activity.class);
    }


    /**
     * metodo per contorllare se le attivita necessitano di essere archiviate
     */
    private void checkIfActivitiesNeedToBeArchived(MonthlyPlan monthlyPlan) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getArchiveLocInfo();
       
       //ottengo la lista di attività da aggiungere e le aggiungo al file di archivio
       
        for (Map.Entry<LocalDate, DailyPlan> dailyEntry : monthlyPlan.getMonthlyPlan().entrySet()) {
            LocalDate date = dailyEntry.getKey();
            DailyPlan dailyPlan = dailyEntry.getValue();
    
            for (Map.Entry<String, ActivityInfo> activityEntry : dailyPlan.getPlan().entrySet()) {
                String activityName = activityEntry.getKey();
                ActivityInfo activityInfo = activityEntry.getValue();
    
                if (activityInfo.getState() == ActivityState.EFFETTUATA && !activityArchived(activityName,date)) {
                    int subs = activityInfo.getNumberOfSub();
                    String time = activityInfo.getTime();

                    PerformedActivity pf = new PerformedActivity(activityName, date, subs, time);

                    dataLayer.add(jsonFactoryService.createJson(pf), locInfo);
                }
            }
        }
    
    }


    private boolean activityArchived(String name, LocalDate date) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getArchiveLocInfo();
        locInfo.setKeyDesc("name");
        locInfo.setKey(name);

        JsonObject pfJO = dataLayer.get(locInfo);
        if(pfJO ==null){
            return false;
        }
        PerformedActivity pf = jsonFactoryService.createObject(pfJO, PerformedActivity.class);

        return pf.getDate().equals(date) && pf.getName().equals(name);
    }
    
}
