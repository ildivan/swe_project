package server.data.facade.implementation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import com.google.gson.JsonObject;
import server.DateService;
import server.data.facade.interfaces.IMonthlyPlanFacade;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.data.json.datalayer.datareadwrite.IJsonReadWrite;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityInfo;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.DailyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.PerformedActivity;
import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.Subscription;
import server.jsonfactoryservice.JsonFactoryService;

public class JsonMonthlyPlanFacade implements IMonthlyPlanFacade{
    private static final String MONTHLY_CONFIG_KEY = "current";
 
    private JsonDataLayer dataLayer;
    private final IJsonLocInfoFactory locInfoFactory;
    private final JsonFactoryService jsonFactoryService = new JsonFactoryService();

    public JsonMonthlyPlanFacade(IJsonReadWrite readWrite, 
                            IJsonLocInfoFactory locInfoFactory) {
        this.dataLayer = new JsonDataLayer(readWrite);
        this.locInfoFactory = locInfoFactory;
    }

    /**
     * metodo per ottenere il monthly plan in base alla data di sistema
     * @return
     */
    @Override
    public synchronized MonthlyPlan getMonthlyPlan(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyPlanLocInfo();
    
        locInfo.setKey(getMonthlyPlanDate());
        JsonObject mpJO = dataLayer.get(locInfo);

        if(mpJO == null){
            return null;
        }

        return jsonFactoryService.createObject(mpJO, MonthlyPlan.class);
    }

    @Override
    public String getMonthlyPlanDate(){
        
        MonthlyConfig mc = getMonthlyConfig();
        LocalDate date = mc.isPlanConfigured().keySet().iterator().next();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return date.format(formatter);
    }

    private MonthlyConfig getMonthlyConfig(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyConfigLocInfo();
        locInfo.setKey(MONTHLY_CONFIG_KEY);

        JsonObject mcJO = dataLayer.get(locInfo);
        MonthlyConfig mc = jsonFactoryService.createObject(mcJO, MonthlyConfig.class);
        assert mc != null;
        return mc;

    }

    /**
     * metodo per aggiornare il monthly plan dopo una iscrizione
     * @param dateOfSubscription
     * @param updatedDailyPlan
     */
    @Override
    public synchronized void updateMonthlyPlan(LocalDate dateOfSubscription, DailyPlan updatedDailyPlan) {
        MonthlyPlan monthlyPlan = getMonthlyPlan();
        Map<LocalDate, DailyPlan> monthlyMap = monthlyPlan.getMonthlyPlan();
        monthlyMap.put(dateOfSubscription, updatedDailyPlan);
        monthlyPlan.setMonthlyPlan(monthlyMap);


        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyPlanLocInfo();
        locInfo.setKey(getMonthlyPlanDate());
        dataLayer.modify(jsonFactoryService.createJson(monthlyPlan), locInfo);
    }

    /**
     * metodo per aggiornare il monthly plan al completo
     * @param monthlyPlan
     */
    @Override
    public synchronized void refreshMonthlyPlan(MonthlyPlan monthlyPlan){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyPlanLocInfo();
        locInfo.setKey(getMonthlyPlanDate());
        dataLayer.modify(jsonFactoryService.createJson(monthlyPlan), locInfo);
    }

    /**
     * metodo per ottenere le informazoni delle attività del giorno scelto
     * @param day
     * @return
     */
    @Override
    public DailyPlan getDailyPlanOfTheChosenDay(int day) {

        LocalDate data = getFullDateOfChosenDay(day); //ottengo la data completa del giorno scelto

        return getDailyPlan(data);
    
    }

    @Override
    public DailyPlan getDailyPlan(LocalDate date) {
        MonthlyPlan monthlyPlan = getMonthlyPlan();

        if(monthlyPlan == null){
            return null;
        }
        
        Map<LocalDate, DailyPlan> monthlyMap = monthlyPlan.getMonthlyPlan();

        if(monthlyMap.containsKey(date)) {
            if(monthlyMap.get(date) == null) {
                return null;
            }
            return monthlyMap.get(date);
        } else {
            return null;
        }
    }

    /**
     * metodo che ritorna la data del gionro scelto
     * @param day
     * @return
     */
    @Override
    public LocalDate getFullDateOfChosenDay(int day) {
        DateService dateService = new DateService();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String dateString = getMonthlyPlanDate();
        LocalDate dateOfPlan = LocalDate.parse(dateString, formatter); //data del piano, uso questa data per ottenere il mese e l'anno

        int chosenMonth = dateService.setMonthOnDayOfSubscription(dateOfPlan, day);
        int chosenYear = dateService.setYearOnDayOfSubscription(dateOfPlan, day);

        LocalDate data = LocalDate.of(chosenYear, chosenMonth, day); //data del giorno scelto
        return data;
    }

    /**
     * metodo per contorllare se le attivita necessitano di essere archiviate
     */
    public void checkIfActivitiesNeedToBeArchived(MonthlyPlan monthlyPlan) {
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

    @Override
    public ActivityInfo getActivityInfoBasedOnSubCode(Subscription subscription){
        DailyPlan dailyPlan = getDailyPlan(subscription.getDateOfActivity());
        Map<String,ActivityInfo> activityMap = dailyPlan.getPlan();
        return activityMap.get(subscription.getActivityName());

    }

    @Override
    public void erasePreviousPlan(MonthlyPlan monthlyPlan){
        JsonDataLocalizationInformation monthlyPlanLocInfo = locInfoFactory.getMonthlyPlanLocInfo();
            dataLayer.erase(monthlyPlanLocInfo);
            dataLayer.add(jsonFactoryService.createJson(monthlyPlan), monthlyPlanLocInfo);
    }


}
