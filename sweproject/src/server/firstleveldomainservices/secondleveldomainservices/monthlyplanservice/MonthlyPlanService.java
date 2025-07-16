package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
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
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigUpdater;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.PlanState;
import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.Subscription;
import server.firstleveldomainservices.volunteerservice.Volunteer;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;
import server.utils.ConfigType;
import server.utils.Configs;
import server.utils.ConfigsUtil;

public class MonthlyPlanService {

    private static final int DAY_OF_PLAN = 16;

    private static final String CONFIG_FIRST_PLAN_KEY_DESC = "firstPlanConfigured";
  
    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private transient DateService dateService = new DateService();
    private transient ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory;
    private transient IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();
    private MonthlyConfigService monthlyConfigService;
    private final ConfigsUtil configsUtil;
    private final ConfigType configType;


    public MonthlyPlanService(ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory,ConfigType configType) {
        this.locInfoFactory = locInfoFactory;
        this.configType = configType;
        this.monthlyConfigService = new MonthlyConfigService(locInfoFactory);
        this.configsUtil = new ConfigsUtil(locInfoFactory, configType);

    }

    public boolean buildMonthlyPlan() {

        boolean firstMonthlyPlan = checkIfFirstMonthlyPlan();
        LocalDate today = dateService.getTodayDate();

        //permette di evitare race conditions durante la configurazione del piano mensile
        MonthlyConfig mc = monthlyConfigService.getMonthlyConfig();


        // si potrebbe fare un metodo nel maager che fa questi, ma non credo sia necessatio
        mc = setIsBeingConfigured(mc, PlanState.DISPONIBILITA_APERTE, false);
        mc = setIsBeingConfigured(mc,PlanState.GENERAZIONE_PIANO, true);

        MonthlyPlan monthlyPlan = new MonthlyPlan(today, locInfoFactory, monthlyConfigService);
        MonthlyConfigUpdater monthlyConfigManager = new MonthlyConfigUpdater(mc, today, locInfoFactory);

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

        if(firstMonthlyPlan){
            updateConfigAfterFirstPlanGenerated();
        }
        return true;//return true se va tutto bene, sarebbe meglio implementare anche iil false con delle eccezioni dentro
        //DA FARE
    }

    /**
     * aggiorno i config indicando che il piano è stato generato la prima volta
     */
    private void updateConfigAfterFirstPlanGenerated() {
        Configs configs = configsUtil.getConfig();
        configs.setFirstPlanConfigured(true);
        configsUtil.save(configs, configType);
    }

    /**
     * verifico se è il primo piano mensile generato
     * @return
     */
    private boolean checkIfFirstMonthlyPlan() {
        Configs configs = configsUtil.getConfig();

        if(!configs.getFirstPlanConfigured()){
            return true;
        }

        return false;
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
    public synchronized MonthlyPlan getMonthlyPlan(){
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
    public synchronized void refreshMonthlyPlan(MonthlyPlan monthlyPlan){
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String dateString = getMonthlyPlanDate();
        LocalDate dateOfPlan = LocalDate.parse(dateString, formatter); //data del piano, uso questa data per ottenere il mese e l'anno

        int chosenMonth = dateService.setMonthOnDayOfSubscription(dateOfPlan, day);
        int chosenYear = dateService.setYearOnDayOfSubscription(dateOfPlan, day);

        LocalDate data = LocalDate.of(chosenYear, chosenMonth, day); //data del giorno scelto
        return data;
    }

    public MonthlyConfig getNewMonthlyConfig(){
        LocalDate dateOfNextPlan = getNextPlanDateBasedOnTodayDate();
    
        Map<LocalDate, Boolean> planConfigured = getPlanConfiguredNewMap(dateOfNextPlan);
        
        Set<LocalDate> precluDates = new HashSet<>();

        return new MonthlyConfig(dateOfNextPlan, planConfigured, precluDates);
    }

    private Map<LocalDate, Boolean> getPlanConfiguredNewMap(LocalDate dateOfNextPlan) {
        Map<LocalDate, Boolean> planConfigured = new HashMap<>();

        LocalDate dateOfPreviousPlan = dateOfNextPlan.minusMonths(1);

        planConfigured.put(dateOfPreviousPlan, false);
        planConfigured.put(dateOfNextPlan, false);

        return planConfigured;
    }

    /**
     * ottiene la data del possimo piano dato usando la data di oggi
     * @return
     */
    public LocalDate getNextPlanDateBasedOnTodayDate() {
        LocalDate date = LocalDate.now(); 
        int month = date.getMonthValue();
        int year = date.getYear();
        if(LocalDate.now().getDayOfMonth()<=DAY_OF_PLAN){
            return LocalDate.of(year, month, DAY_OF_PLAN);
        }else{
            if(month == 12){
                return LocalDate.of(year + 1, 1, DAY_OF_PLAN);
            }
            return LocalDate.of(year, month + 1, DAY_OF_PLAN);
        }
    }



}
