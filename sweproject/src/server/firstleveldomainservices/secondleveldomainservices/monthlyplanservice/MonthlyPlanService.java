package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

public class MonthlyPlanService {

    private static final String MONTHLY_CONFIG_KEY = "current";
  
    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private transient DateService dateService = new DateService();
    private transient ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory = new JsonLocInfoFactory();
    private transient IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();


    public boolean buldMonthlyPlan() {
        LocalDate today = dateService.getTodayDate();

        //flag per evitare race conditions

         //permette di evitare race conditions durante la configurazione del piano mensile
        MonthlyConfig mc = getMonthlyConfig();
        setIsBeingConfigured(mc,true);

        
        MonthlyPlan monthlyPlan = new MonthlyPlan(today);

        JsonDataLocalizationInformation locInfo = locInfoFactory.getActivityLocInfo();
    
        List<JsonObject> activityJO = dataLayer.getAll(locInfo);
        List<Activity> activity = jsonFactoryService.createObjectList(activityJO, Activity.class);

        monthlyPlan.generateMonthlyPlan(activity);

        final JsonDataLocalizationInformation monthlyPlanLocInfo = locInfoFactory.getMonthlyPlanLocInfo();

        dataLayer.add(jsonFactoryService.createJson(monthlyPlan), monthlyPlanLocInfo);
       
        monthlyPlan.setPlanBuildFlagAsTrue();
        monthlyPlan.incrementMonthOfPlan();
        monthlyPlan.clearPrecludedDates();
            
        refreshVolunteers();

        //conclusione della generazine del piano, esco dalla sezione critica
        setIsBeingConfigured(mc, false);


        return true;//return true se va tutto bene, sarebbe meglio implementare anche iil false con delle eccezioni dentro
        //DA FARE
    }

    /**
     * metodo epr modificare il fatto che si sta iniziando a configurare il piano mensile
     * @param mc
     * @param isBeingConfigured
     */
    private void setIsBeingConfigured(MonthlyConfig mc, boolean isBeingConfigured) {
        mc.setBeingConfigured(isBeingConfigured);
        
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyConfigLocInfo();
        locInfo.setKey(MONTHLY_CONFIG_KEY);
        dataLayer.modify(jsonFactoryService.createJson(mc), locInfo);

    }

    /**
     * metodo per permettere un nuovo inserimento di date precluse hai volontari
     */
    private void refreshVolunteers() {
        
        List<Volunteer> volunteers = getVolunteers();

        for (Volunteer volunteer : volunteers) {
            Set<String> newDays = volunteer.getNondisponibilityDaysCurrent();
            volunteer.setNondisponibilityDaysOld(newDays);
            volunteer.setNondisponibilityDaysCurrent(new LinkedHashSet<>());

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

    private String getMonthlyPlanDate(){
        MonthlyConfig mc = getMonthlyConfig();
        LocalDate date = mc.isPlanConfigured().keySet().iterator().next();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return date.format(formatter);
    }

    public MonthlyConfig getMonthlyConfig(){
        JsonDataLocalizationInformation locInfo =locInfoFactory.getMonthlyConfigLocInfo();

        locInfo.setKey(MONTHLY_CONFIG_KEY);

        JsonObject mcJO = dataLayer.get(locInfo);
        MonthlyConfig mc = jsonFactoryService.createObject(mcJO, MonthlyConfig.class);
        return mc;

    }
}
