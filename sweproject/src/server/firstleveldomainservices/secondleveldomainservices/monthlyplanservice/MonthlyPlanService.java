package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;

import lock.MonthlyPlanLockManager;
import server.DateService;
import server.authservice.User;
import server.data.facade.FacadeHub;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigUpdater;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.PlanState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.precludedateservice.PrecludeDateService;
import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.Subscription;
import server.firstleveldomainservices.volunteerservice.VMIOUtil;
import server.firstleveldomainservices.volunteerservice.Volunteer;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;
import server.utils.ConfigType;
import server.utils.Configs;
import server.utils.ConfigsUtil;

public class MonthlyPlanService {

    private static final int DAY_OF_PLAN = 16;
  
    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private transient DateService dateService = new DateService();
    private transient IJsonLocInfoFactory locInfoFactory;
    private transient JsonDataLayer dataLayer;
    private MonthlyConfigService monthlyConfigService;
    private VMIOUtil volUtil;
    private final ConfigsUtil configsUtil;
    private final ConfigType configType;
    private final FacadeHub data;


    public MonthlyPlanService(
        IJsonLocInfoFactory locInfoFactory,ConfigType configType, 
        JsonDataLayer dataLayer, FacadeHub data) {
        this.locInfoFactory = locInfoFactory;
        this.configType = configType;
        this.dataLayer = dataLayer;
        this.monthlyConfigService = new MonthlyConfigService(locInfoFactory, dataLayer);
        this.configsUtil = new ConfigsUtil(locInfoFactory, configType, dataLayer);
        this.volUtil = new VMIOUtil(locInfoFactory, dataLayer);
        this.data = data;
    }

    public boolean buildMonthlyPlan() {
        boolean lockAcquired = false;
        try {
            // Provo ad acquisire il lock entro 2 secondi
            lockAcquired = MonthlyPlanLockManager.tryLock(2, TimeUnit.SECONDS);

            if (!lockAcquired) {
                System.out.println("Impossibile generare il piano mensile: risorsa già in uso.");
                return false;
            }

            boolean firstMonthlyPlan = checkIfFirstMonthlyPlan();
            LocalDate today = dateService.getTodayDate().withDayOfMonth(16);

            // Evita race condition modificando lo stato del piano in configurazione
            MonthlyConfig mc = monthlyConfigService.getMonthlyConfig();
            mc = setIsBeingConfigured(mc, PlanState.DISPONIBILITA_APERTE, false);
            mc = setIsBeingConfigured(mc, PlanState.GENERAZIONE_PIANO, true);

            MonthlyPlan monthlyPlan = new MonthlyPlan(
                today, locInfoFactory, monthlyConfigService,
                new PrecludeDateService(locInfoFactory, dataLayer)
            );

            MonthlyConfigUpdater monthlyConfigManager = new MonthlyConfigUpdater(mc, today, locInfoFactory, dataLayer);

            List<Activity> activities = data.getActivitiesFacade().getActivities();

            monthlyPlan.generateMonthlyPlan(activities);

            JsonDataLocalizationInformation monthlyPlanLocInfo = locInfoFactory.getMonthlyPlanLocInfo();
            dataLayer.erase(monthlyPlanLocInfo);
            dataLayer.add(jsonFactoryService.createJson(monthlyPlan), monthlyPlanLocInfo);

            if (firstMonthlyPlan) {
                updateConfigAfterFirstPlanGenerated();
            }

            refreshData(monthlyConfigManager, today);

            //aggiorno mappa per permetter di modificare, garantisco la sequenzialità
            setIsBeingConfigured(mc, PlanState.GENERAZIONE_PIANO, false);
            setIsBeingConfigured(mc, PlanState.MODIFICHE_APERTE, true);

            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread interrotto durante l’attesa del lock.");
            return false;

        } finally {
            if (lockAcquired) {
                MonthlyPlanLockManager.unlock();
            }
        }
    }


    /**
     * metodo per applicare le modifiche effettuate nel mese precedente
     */
    private void refreshData(MonthlyConfigUpdater monthlyConfigManager, LocalDate date) {
        
        monthlyConfigManager.updateMonthlyConfigAfterPlan();
        refreshChangedPlaces();
        refreshChangedActivities();
        refreshUsers();
        refreshVolunteers();
        refreshPrecludeDates(date);
    }

    /**
     * metodo di utilita a refreshData
     * aggiorna le modifiche hai luoghi
     */
    private void refreshChangedPlaces() {

        //file temporaneo per rendere atomica la copia, altrimenti possibili inconsistenze
        //NECESSARIA ATOMIC_MOVE support
        Path changedPlacesPath = Paths.get(locInfoFactory.getChangedPlacesLocInfo().getPath());
        Path originalPlacesPath = Paths.get(locInfoFactory.getPlaceLocInfo().getPath());
        Path tempPath = originalPlacesPath.resolveSibling(originalPlacesPath.getFileName() + ".tmp");

        try {
            // Copia su file temporaneo
            Files.copy(changedPlacesPath, tempPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            // Move atomico (rinomina il file temporaneo in quello definitivo)
            Files.move(tempPath, originalPlacesPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * metodo di utilita a refreshData
     * aggiorna le modifiche alle attività
     */
    private void refreshChangedActivities() {

        //file temporaneo per rendere atomica la copia, altrimenti possibili inconsistenze
        //NECESSARIA ATOMIC_MOVE support
        Path changedActivitiesPath = Paths.get(locInfoFactory.getChangedActivitiesLocInfo().getPath());
        Path originalActivitiesPath = Paths.get(locInfoFactory.getActivityLocInfo().getPath());
        Path tempPath = originalActivitiesPath.resolveSibling(originalActivitiesPath.getFileName() + ".tmp");

        try {
            // Copia su file temporaneo
            Files.copy(changedActivitiesPath, tempPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            // Move atomico
            Files.move(tempPath, originalActivitiesPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * method to refresh users
     */
    private void refreshUsers() {
        JsonDataLocalizationInformation lcoInfo = locInfoFactory.getUserLocInfo();
        List<JsonObject> usersJO = dataLayer.getAll(lcoInfo);

        for (JsonObject jsonObject : usersJO) {
            if(jsonObject.get("deleted").getAsBoolean()){
                JsonDataLocalizationInformation userLocInfo = locInfoFactory.getUserLocInfo();
                userLocInfo.setKey(jsonObject.get("name").getAsString());
                //elimina l'utente
                dataLayer.delete(userLocInfo);

                //se è un volontario elimina anche il volontario
                if(jsonObject.get("role").getAsString().equalsIgnoreCase("volontario")){
                    volUtil.deleteVolunteer(jsonObject.get("name").getAsString());
                }
            }

            if(!jsonObject.get("active").getAsBoolean()){
                JsonDataLocalizationInformation userLocInfo = locInfoFactory.getUserLocInfo();
                userLocInfo.setKey(jsonObject.get("name").getAsString());
                User user = jsonFactoryService.createObject(jsonObject, User.class);
                user.setActive(true);

                dataLayer.modify(jsonFactoryService.createJson(user), userLocInfo);
            }
        }
    }

    /**
     * method to refrehs preclude dates
     */
    private void refreshPrecludeDates(LocalDate dateOfPlan) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getPrecludeDatesLocInfo();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = dateOfPlan.format(formatter);
        locInfo.setKey(formattedDate);

        dataLayer.delete(locInfo);
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

        if(mpJO == null){
            return null;
        }

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
        MonthlyPlan monthlyPlan = getMonthlyPlan();

        if(monthlyPlan == null){
            return null;
        }
        
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

    public ActivityInfo getActivityInfoBasedOnSubCode(Subscription subscription){
        DailyPlan dailyPlan = getDailyPlan(subscription.getDateOfActivity());
        Map<String,ActivityInfo> activityMap = dailyPlan.getPlan();
        return activityMap.get(subscription.getActivityName());

    }



}
