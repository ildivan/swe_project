package server.firstleveldomainservices.volunteerservice;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import server.DateService;
import server.datalayerservice.DataLayerDispatcherService;
import server.datalayerservice.JsonDataLocalizationInformation;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuService;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.menus.VolunteerMenu;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityInfo;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityRecord;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.DailyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlanService;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.ioservice.objectformatter.IIObjectFormatter;
import server.ioservice.objectformatter.TerminalObjectFormatter;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;
import server.utils.MainService;

public class VolunteerService extends MainService<Void>{
    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";
    private static final String ACTIVITY_PATH = "JF/activities.json";
    private static final String ACTIVITY_MEMBER_NAME = "activities";
    private static final String VOLUNTEER_PATH = "JF/volunteers.json";
    private static final String VOLUNTEER_MEMBER_NAME = "volunteers";
    private static final String VOLUNTEER_KEY_DESC = "name";
    
    private Gson gson;
    private String name;
    private MenuService menu = new VolunteerMenu(this);
    private String configType;
    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private IInputOutput ioService = new IOService();
    private IIObjectFormatter<String> formatter= new TerminalObjectFormatter();
    private MonthlyPlanService monthlyPlanService = new MonthlyPlanService();
    private DateService dateService = new DateService();
    
  

    public VolunteerService(Socket socket, Gson gson, String configType, String name) {
        super(socket);
        this.configType = configType;
        this.gson = gson;
        this.name = name;
    }
    /**
     * apply the logic of the service
     * @return null
     * @throws IOException
     */
    public Void applyLogic() throws IOException {
        
        boolean continuare = true;
        
        do{
            ioService.writeMessage(CLEAR,false);
            Runnable toRun = menu.startMenu();
            ioService.writeMessage(SPACE, false);
            if(toRun==null){
                continuare = false;
            }else{
                toRun.run();
                continuare = continueChoice("scelta operazioni");
            }
                
        }while(continuare);
        ioService.writeMessage("\nArrivederci!\n", false);

        return null;
    }

    /**
     * mostra le attività del pianp in cui è presente il volontario
     */
    public void showMyActivities(){
        MonthlyPlanService monthlyPlanService = new MonthlyPlanService();
        MonthlyPlan monthlyPlan = monthlyPlanService.getMonthlyPlan();

        List<ActivityRecord> result = new ArrayList<>();

        for (Map.Entry<LocalDate, DailyPlan> dailyEntry : monthlyPlan.getMonthlyPlan().entrySet()) {
            LocalDate date = dailyEntry.getKey();
            DailyPlan dailyPlan = dailyEntry.getValue();
            List<Activity> myActivities = getMyActivities();

            for (Map.Entry<String, ActivityInfo> activityEntry : dailyPlan.getPlan().entrySet()) {
                String activityName = activityEntry.getKey();
                ActivityInfo activityInfo = activityEntry.getValue();
    
                if (isMyActivity(activityName,myActivities)) {
                    result.add(new ActivityRecord(date, activityName, activityInfo));
                }
            }
        }
    
        ioService.writeMessage(formatter.formatListActivityRecord(result), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * metodo che permette di visualizzare la descrizone e la struttura di tutte le visite in cui 
     * è associato il nome del volontario
     */
    public void showMyActivitiesDescription(){
        
        List<Activity> myActivities = getMyActivities();
        
        ioService.writeMessage(formatter.formatListActivity(myActivities), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * metodo che permette al volontario di inserire le date in cui esso non è disponibile
     * 
     * solo nei giorni in cui sono presenti sue visite e che non sono preclusi
     */
    public void addPrecludeDate(){
        
        MonthlyConfig mc = monthlyPlanService.getMonthlyConfig();

        int maxNumDay = mc.getMonthAndYear().getMonth().length(mc.getMonthAndYear().isLeapYear());
        int minNumDay = 1;
        
        int day = ioService.readIntegerWithMinMax("\"Inserire giorno in cui non si è disponibili nel prossimo mese", minNumDay, maxNumDay);

        int month = dateService.setMonthOnPrecludeDayVolunteer(mc, day);
        int year = dateService.setYearOnPrecludeDayVolunteer(mc, day);

        LocalDate date = LocalDate.of(year, month, day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = date.format(formatter);

        addPrecludeDatesForVolunteer(formattedDate);

    }

    /**
     * metodo per aggiungere la data in cui il volontario on è disponibile al file
     * @param date
     */
    private void addPrecludeDatesForVolunteer(String date){
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(VOLUNTEER_PATH);
        locInfo.setMemberName(VOLUNTEER_MEMBER_NAME);
        locInfo.setKeyDesc(VOLUNTEER_KEY_DESC);
        locInfo.setKey(name);
        JsonObject volunteerJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.get(locInfo));
        Volunteer volunteer = jsonFactoryService.createObject(volunteerJO, Volunteer.class);

        Set<String> precludeDates = volunteer.getNondisponibilityDaysCurrent();
        precludeDates.add(date);

        JsonObject newVolunteerJO = jsonFactoryService.createJson(volunteer);

        DataLayerDispatcherService.start(locInfo, layer->layer.modify(newVolunteerJO, locInfo));
    }

    private boolean isMyActivity(String actName, List<Activity> myActivities){

        for (Activity activity : myActivities) {
            if(activity.getTitle().equalsIgnoreCase(actName)){
                return true;
            }
        }
        return false;
    }

    private List<Activity> getMyActivities(){
        List<Activity> activities = getActivities();
        List<Activity> myActivities = new ArrayList<>();
        
        for (Activity activity : activities) {
            if(checkIfVolunteerInActivity(activity)){
                myActivities.add(activity);
            }
        }
        return myActivities;

    }

    private boolean checkIfVolunteerInActivity(Activity act){
        return act.getVolunteers()[0].equalsIgnoreCase(name);
    }

    private List<Activity> getActivities(){
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(ACTIVITY_PATH);
        locInfo.setMemberName(ACTIVITY_MEMBER_NAME);
        List<JsonObject> activitiesJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.getAll(locInfo));
        List<Activity> activities = jsonFactoryService.createObjectList(activitiesJO, Activity.class);
        return activities;
    }
}
