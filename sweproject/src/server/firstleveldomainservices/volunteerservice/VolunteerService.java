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
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
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
import server.utils.ConfigType;
import server.utils.MainService;

public class VolunteerService extends MainService<Void>{
    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";

    
    private Gson gson;
    private final String name;
    private final MenuService menu = new VolunteerMenu(this);
    private ConfigType configType;
    private final IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private final IInputOutput ioService = new IOService();
    private final IIObjectFormatter<String> formatter = new TerminalObjectFormatter();
    private final MonthlyPlanService monthlyPlanService = new MonthlyPlanService();
    private final DateService dateService = new DateService();
    private final ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory = new JsonLocInfoFactory();
    private final IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();
    
  

    public VolunteerService(Socket socket, Gson gson, ConfigType configType, String name) {
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
        
        doOperations();
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

        if(mc.isBeingConfigured()){
            ioService.writeMessage("Il piano mensile è in fase di configurazione, non puoi aggiungere date precluse", false);
            return;
        }

        int maxNumDay = mc.getMonthAndYear().getMonth().length(mc.getMonthAndYear().isLeapYear());
        int minNumDay = 1;
        
        int day = ioService.readIntegerWithMinMax("\"Inserire giorno in cui non si è disponibili nel prossimo mese", minNumDay, maxNumDay);

        /*
         * prendere il mese e l'anno cosi permette di evitare race conditions
         */
        int month = dateService.setMonthOnPrecludeDayVolunteer(mc, day);
        int year = dateService.setYearOnPrecludeDayVolunteer(mc, day);

        LocalDate date = LocalDate.of(year, month, day);


        addPrecludeDatesForVolunteer(date);

    }

    /**
     * metodo per aggiungere la data in cui il volontario on è disponibile al file
     * @param date
     */
    private void addPrecludeDatesForVolunteer(LocalDate date){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = date.format(formatter);

        assert formattedDate != null && !formattedDate.trim().isEmpty();

        if(!dateContainsVolunteerActivity(date, getMyActivities())){
            ioService.writeMessage("Non hai attività programmate per questa data, non puoi aggiungerla come preclusa", false);
            return;
        }

        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        locInfo.setKey(name);
        JsonObject volunteerJO = dataLayer.get(locInfo);
        Volunteer volunteer = jsonFactoryService.createObject(volunteerJO, Volunteer.class);

        Set<String> precludeDates = volunteer.getNondisponibilityDaysCurrent();
        precludeDates.add(formattedDate);

        JsonObject newVolunteerJO = jsonFactoryService.createJson(volunteer);

        dataLayer.modify(newVolunteerJO, locInfo);

        ioService.writeMessage("Data " + formattedDate + " aggiunta con successo come preclusa", false);
    }

    private boolean isMyActivity(String actName, List<Activity> myActivities){
        assert getActivities().stream().map(Activity::getTitle).toList().contains(actName);

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
        JsonDataLocalizationInformation locInfo = locInfoFactory.getActivityLocInfo();
        List<JsonObject> activitiesJO = dataLayer.getAll(locInfo);
        List<Activity> activities = jsonFactoryService.createObjectList(activitiesJO, Activity.class);
        return activities;
    }

    private void doOperations() {
        boolean continuare;
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
    }

    /**
     * Metodo che verifica se il volontario ha un'attività programmata per una data specifica
     * @param data
     * @param attivitaVolontario
     * @param monthlyPlan
     * @return
     */
    public boolean dateContainsVolunteerActivity(LocalDate data, List<Activity> myActivities) {
        MonthlyPlanService monthlyPlanService = new MonthlyPlanService();
        MonthlyPlan monthlyPlan = monthlyPlanService.getMonthlyPlan();


        DailyPlan dailyPlan = monthlyPlan.getDailyPlan(data);
        if (dailyPlan == null || dailyPlan.getPlan().isEmpty()) {
            return false;
        }

        Set<String> dailyActivity = dailyPlan.getPlan().keySet();

        for (Activity a : myActivities) {
            if (dailyActivity.contains(a.getTitle())) {
                return true;
            }
        }

        return false;
    }

}
