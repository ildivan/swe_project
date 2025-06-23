package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;
import server.DateService;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigUpdater;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.PlanState;
import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.Subscription;
import server.firstleveldomainservices.volunteerservice.Volunteer;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;

public class MonthlyPlanService {

    private static final String MONTHLY_CONFIG_KEY = "current";
  
    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private transient DateService dateService = new DateService();
    private transient ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory = new JsonLocInfoFactory();
    private transient IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();
    private MonthlyConfigService monthlyConfigService = new MonthlyConfigService();


    public boolean buldMonthlyPlan() {
        LocalDate today = dateService.getTodayDate();

        //permette di evitare race conditions durante la configurazione del piano mensile
        MonthlyConfig mc = monthlyConfigService.getMonthlyConfig();


        // si potrebbe fare un metodo nel maager che fa questi, ma non credo sia necessatio
        mc = setIsBeingConfigured(mc, PlanState.DISPONIBILITA_APERTE, false);
        mc = setIsBeingConfigured(mc,PlanState.GENERAZIONE_PIANO, true);
        
        MonthlyPlan monthlyPlan = new MonthlyPlan(today);
        MonthlyConfigUpdater monthlyConfigManager = new MonthlyConfigUpdater(mc, today);

        JsonDataLocalizationInformation locInfo = locInfoFactory.getActivityLocInfo();
    
        List<JsonObject> activityJO = dataLayer.getAll(locInfo);
        List<Activity> activity = jsonFactoryService.createObjectList(activityJO, Activity.class);

        monthlyPlan.generateMonthlyPlan(activity);

        final JsonDataLocalizationInformation monthlyPlanLocInfo = locInfoFactory.getMonthlyPlanLocInfo();

        dataLayer.erase(monthlyPlanLocInfo);
        dataLayer.add(jsonFactoryService.createJson(monthlyPlan), monthlyPlanLocInfo);
       
        monthlyConfigManager.updateMonthlyConfigAfterPlan();
            
        refreshVolunteers();

        //conclusione della generazine del piano, esco dalla sezione critica
        setIsBeingConfigured(mc, PlanState.GENERAZIONE_PIANO, false);
        setIsBeingConfigured(mc, PlanState.MODIFICHE_APERTE, true); //una volta generato il piano posso modificare le attività, entreranno in vigore dal mese successivo


        return true;//return true se va tutto bene, sarebbe meglio implementare anche iil false con delle eccezioni dentro
        //DA FARE
    }

    /**
     * metodo epr modificare il fatto che si sta iniziando a configurare il piano mensile
     * @param mc
     * @param isBeingConfigured
     */
    private MonthlyConfig setIsBeingConfigured(MonthlyConfig mc, PlanState isBeingConfigured, Boolean value) {

        Map<PlanState, Boolean> stateMap = mc.getPlanStateMap();
        stateMap.put(isBeingConfigured, value);
        mc.setPlanStateMap(stateMap);
        monthlyConfigService.saveMonthlyConfig(mc);

        return mc;

    }

    /**
     * metodo per permettere un nuovo inserimento di date precluse hai volontari
     */
    private void refreshVolunteers() {
        
        List<Volunteer> volunteers = getVolunteers();
        Set<String> newDays;

        for (Volunteer volunteer : volunteers) {
            if(volunteer.getDisponibilityDaysCurrent() == null) {
                newDays = new LinkedHashSet<>();
            }else{
                newDays = volunteer.getDisponibilityDaysCurrent();
            }
            
            volunteer.setDisponibilityDaysOld(newDays);
            volunteer.setDisponibilityDaysCurrent(new LinkedHashSet<>());

            saveVolunteer(volunteer);
        }
       

    }

    private void saveVolunteer(Volunteer volunteer) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        
        locInfo.setKey(volunteer.getName());

        dataLayer.modify(jsonFactoryService.createJson(volunteer), locInfo);
    }

    /**
     * metodo per ottenere i volontari
     * @param locInfo
     */
    private List<Volunteer> getVolunteers() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();

        List<JsonObject> volunteersJO = dataLayer.getAll(locInfo);
        List<Volunteer> volunteers = jsonFactoryService.createObjectList(volunteersJO, Volunteer.class);
        return volunteers;
    }



    /**
     * metodo per ottenere il monthly plan in base alla data di sistema
     * @return
     */
    public MonthlyPlan getMonthlyPlan(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyPlanLocInfo();
    
        locInfo.setKey(getMonthlyPlanDate());
        JsonObject mpJO = dataLayer.get(locInfo);

        return jsonFactoryService.createObject(mpJO, MonthlyPlan.class);
    }

    public String getMonthlyPlanDate(){
        
        MonthlyConfig mc = monthlyConfigService.getMonthlyConfig();
        LocalDate date = mc.isPlanConfigured().keySet().iterator().next();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return date.format(formatter);
    }

    /**
     * metodo per aggiornare il monthly plan dopo una iscrizione
     * @param dateOfSubscription
     * @param updatedDailyPlan
     */
    public void updateMonthlyPlan(LocalDate dateOfSubscription, DailyPlan updatedDailyPlan) {
        MonthlyPlan monthlyPlan = getMonthlyPlan();
        Map<LocalDate, DailyPlan> monthlyMap = monthlyPlan.getMonthlyPlan();
        monthlyMap.put(dateOfSubscription, updatedDailyPlan);
        monthlyPlan.setMonthlyPlan(monthlyMap);


        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyPlanLocInfo();
        locInfo.setKey(getMonthlyPlanDate());
        dataLayer.modify(jsonFactoryService.createJson(monthlyPlan), locInfo);
    }

    /**
     * metodo per aggiornare il piano mensile dopo una eliminazione di una iscrizione
     * @param subscription
     */
    public void removeSubscription(Subscription subscription) {
        //ottengo il piano giornaliero della data dell'attività a cui sono iscritto
        DailyPlan dailyPlan = getDailyPlan(subscription.getDateOfActivity());
        if(dailyPlan == null) {
            return;
        }

        //rimuovo l'iscrizione
        dailyPlan.removeSubscriptionOnActivity(subscription);

        //aggiorno il piano mensile
        updateMonthlyPlan(subscription.getDateOfActivity(), dailyPlan);

    }

        /**
     * metodo per ottenere le informazoni delle attività del giorno scelto
     * @param day
     * @return
     */
    public DailyPlan getDailyPlanOfTheChosenDay(int day) {

        LocalDate data = getFullDateOfChosenDay(day); //ottengo la data completa del giorno scelto

        return getDailyPlan(data);
    
    }

    private DailyPlan getDailyPlan(LocalDate date) {
        Map<LocalDate, DailyPlan> monthlyMap = getMonthlyPlan().getMonthlyPlan();

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
    public LocalDate getFullDateOfChosenDay(int day) {
        DateService dateService = new DateService();
        MonthlyPlanService monthlyPlanService = new MonthlyPlanService();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String dateString = monthlyPlanService.getMonthlyPlanDate();
        LocalDate dateOfPlan = LocalDate.parse(dateString, formatter); //data del piano, uso questa data per ottenere il mese e l'anno

        int chosenMonth = dateService.setMonthOnDayOfSubscription(dateOfPlan, day);
        int chosenYear = dateService.setYearOnDayOfSubscription(dateOfPlan, day);

        LocalDate data = LocalDate.of(chosenYear, chosenMonth, day); //data del giorno scelto
        return data;
    }

}
