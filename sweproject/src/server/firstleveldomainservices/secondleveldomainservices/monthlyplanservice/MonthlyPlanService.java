package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;
import server.DateService;
import server.datalayerservice.DataLayerDispatcherService;
import server.datalayerservice.JsonDataLocalizationInformation;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.volunteerservice.Volunteer;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;

public class MonthlyPlanService {

    private static final String ACTIVITY_PATH = "JF/activities.json";
    private static final String ACTIVITY_MEMBER_NAME = "activities";
    private static final String MONTHLY_PLAN_PATH = "JF/monthlyPlan.json";
    private static final String MONTHLY_PLAN_MEMBER_NAME = "monthlyPlan";
    private static final String MONTHLY_CONFIG_KEY_DESC = "type";
    private static final String MONTHLY_CONFIG_KEY = "current";
    private static final String MONTHLY_CONFIG_MEMEBER_NAME = "mc";
    private static final String MONTHLY_CONFIG_PATH = "JF/monthlyConfigs.json";
    private static final String MONTHLY_PLAN_KEY_DESC = "date";
    private static final String VOLUNTEER_PATH = "JF/volunteers.json";
    private static final String VOLUNTEER_MEMBER_NAME = "volunteers";
    private static final String VOLUNTEER_KEY_DESC = "name";

    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private transient DateService dateService = new DateService();


    public boolean buldMonthlyPlan() {
        LocalDate today = dateService.getTodayDate();
        MonthlyPlan monthlyPlan = new MonthlyPlan(today);

        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(ACTIVITY_PATH);
        locInfo.setMemberName(ACTIVITY_MEMBER_NAME);
    
        List<JsonObject> activityJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.getAll(locInfo));
        List<Activity> activity = jsonFactoryService.createObjectList(activityJO, Activity.class);

        monthlyPlan.generateMonthlyPlan(activity);

        locInfo.setPath(MONTHLY_PLAN_PATH);
        locInfo.setMemberName(MONTHLY_PLAN_MEMBER_NAME);

        DataLayerDispatcherService.start(locInfo, layer -> layer.add(jsonFactoryService.createJson(monthlyPlan), locInfo));
       
        monthlyPlan.setPlanBuildFlagAsTrue();
        monthlyPlan.incrementMonthOfPlan();
        monthlyPlan.clearPrecludedDates();
            
        refreshVolunteers();

        return true;//return true se va tutto bene, sarebbe meglio implementare anche iil false con delle eccezioni dentro
        //DA FARE
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
         JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(VOLUNTEER_PATH);
        locInfo.setMemberName(VOLUNTEER_MEMBER_NAME);
        locInfo.setKeyDesc(VOLUNTEER_KEY_DESC);
        locInfo.setKey(volunteer.getName());

        DataLayerDispatcherService.start(locInfo, layer->layer.modify(jsonFactoryService.createJson(volunteer), locInfo));
    }

    /**
     * metodo per ottenere i volontari
     * @param locInfo
     */
    private List<Volunteer> getVolunteers() {
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(VOLUNTEER_PATH);
        locInfo.setMemberName(VOLUNTEER_MEMBER_NAME);
        locInfo.setKeyDesc(VOLUNTEER_MEMBER_NAME);

        List<JsonObject> volunteersJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.getAll(locInfo));
        List<Volunteer> volunteers = jsonFactoryService.createObjectList(volunteersJO, Volunteer.class);
        return volunteers;
    }



    /**
     * metodo per ottenere il monthly plan in base alla data di sistema
     * @return
     */
    public MonthlyPlan getMonthlyPlan(){
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(MONTHLY_PLAN_PATH);
        locInfo.setMemberName(MONTHLY_PLAN_MEMBER_NAME);
        //poiche uso il metodo get devo aver sia la key che la keydesc settate nelle localization info
        locInfo.setKeyDesc(MONTHLY_PLAN_KEY_DESC);
        locInfo.setKey(getMonthlyPlanDate());
        JsonObject mpJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.get(locInfo));

        return jsonFactoryService.createObject(mpJO, MonthlyPlan.class);
    }

    private String getMonthlyPlanDate(){
        MonthlyConfig mc = getMonthlyConfig();
        LocalDate date = mc.isPlanConfigured().keySet().iterator().next();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return date.format(formatter);
    }

    public MonthlyConfig getMonthlyConfig(){
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(MONTHLY_CONFIG_PATH);
        locInfo.setMemberName(MONTHLY_CONFIG_MEMEBER_NAME);
        locInfo.setKeyDesc(MONTHLY_CONFIG_KEY_DESC);
        locInfo.setKey(MONTHLY_CONFIG_KEY);

        JsonObject mcJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.get(locInfo));
        MonthlyConfig mc = jsonFactoryService.createObject(mcJO, MonthlyConfig.class);
        return mc;

    }
}
