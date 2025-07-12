package server.firstleveldomainservices.volunteerservice;

import java.io.IOException;
import java.net.Socket;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import server.DateService;
import server.data.facade.FacadeHub;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuService;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.menus.VolunteerMenu;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.PlanState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityInfo;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityRecord;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.DailyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlanService;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.ioservice.objectformatter.IIObjectFormatter;
import server.ioservice.objectformatter.TerminalObjectFormatter;
import server.utils.ConfigType;
import server.utils.ConfigsUtil;
import server.utils.MainService;

public class VolunteerService extends MainService<Void>{
    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";


    private final MenuService menu;
    private final IInputOutput ioService = new IOService();
    private final IIObjectFormatter<String> formatter = new TerminalObjectFormatter();
    private final MonthlyPlanService monthlyPlanService;
    private final DateService dateService = new DateService();
    private final MonthlyConfigService monthlyConfigService;
    private final ConfigsUtil configsUtil;
    private final VMIOUtil volUtil;
    private final String name;
    private final FacadeHub data;
    
  

    public VolunteerService(Socket socket, String name, IJsonLocInfoFactory locInfoFactory,
    ConfigType configType, JsonDataLayer dataLayer, FacadeHub data) {
        super(socket);
        this.name = name;
        this.monthlyPlanService = new MonthlyPlanService(locInfoFactory, configType, dataLayer, data);
        this.monthlyConfigService = new MonthlyConfigService(locInfoFactory, dataLayer);
        this.configsUtil = new ConfigsUtil(locInfoFactory, configType, dataLayer);
        this.volUtil = new VMIOUtil(locInfoFactory, dataLayer);
        this.menu = new VolunteerMenu(this, locInfoFactory, configType, dataLayer);
        this.data = data;
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
     * mostra le attività del pianp in cui è presente il volontario
     */
    public void showMyActivities(){
        MonthlyPlan monthlyPlan = monthlyPlanService.getMonthlyPlan();

        if(monthlyPlan == null){
            ioService.writeMessage("\nPiano mensile non ancora generato", false);
            return;
        }

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
     * metodo che permette al volontario di inserire le date in cui esso è disponibile
     * 
     * solo in giorni non preclusi i cui possono essere instanziate sue visite
     */
    public void addDisponibilityDate(){
        
        MonthlyConfig mc = monthlyConfigService.getMonthlyConfig();

        if(!mc.getPlanStateMap().get(PlanState.DISPONIBILITA_APERTE)){
            ioService.writeMessage("Piano mensile o modifica attivita in corso, non puoi aggiungere date di disponibilità", false);
            return;
        }
        

        int maxNumDay = dateService.getMaxNumDay(mc, 2);
        int minNumDay = 1;
        
        int day = ioService.readIntegerWithMinMax("\"Inserire giorno in cui si è disponibili nel prossimo mese", minNumDay, maxNumDay);

        /*
         * prendere il mese e l'anno cosi permette di evitare race conditions
         */
        boolean firstPlanConfigured = configsUtil.getConfig().getFirstPlanConfigured();
        int month = mc.getMonthAndYear().getMonth().plus(1).getValue();
        
        int year = mc.getMonthAndYear().getYear();

        if(dateService.incrementYearOnDisponibilityDayVolunteer(mc.getMonthAndYear().getMonthValue(), firstPlanConfigured)){
            year =+1;
        }

        LocalDate date = LocalDate.of(year, month, day);


        addDisponibilityDatesForVolunteer(date, firstPlanConfigured);

    }

    /**
     * metodo per aggiungere la data in cui il volontario è disponibile al file
     * @param date
     * @param firstPlanConfigured
     */
    private void addDisponibilityDatesForVolunteer(LocalDate date, boolean firstPlanConfigured){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = date.format(formatter);

        assert formattedDate != null && !formattedDate.trim().isEmpty();

        if(!dateContainsPossibleVolunteerActivity(date, getMyActivities())){
            ioService.writeMessage("Non è possibile programmare tue attività per questa data, non puoi aggiungerla come data disponibile", false);
            return;
        }

        volUtil.addDisponibilityDate(firstPlanConfigured, name, formattedDate);
        
        ioService.writeMessage("Data " + formattedDate + " aggiunta con successo come data di disponibilità", false);
    }

    private boolean isMyActivity(String actName, List<Activity> myActivities){
        assert data.getActivitiesFacade().getActivities()
                .stream()
                .map(Activity::getTitle)
                .toList()
                .contains(actName);

        for (Activity activity : myActivities) {
            if(activity.getTitle().equalsIgnoreCase(actName)){
                return true;
            }
        }
        return false;
    }

    private List<Activity> getMyActivities(){
        List<Activity> activities = data.getActivitiesFacade().getActivities();
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

    
    /**
     * Metodo che verifica se il volontario puo avere un'attività programmata per una data specifica,
     * controllo se il giorno è compatibile
     * @param data
     * @param attivitaVolontario
     * @param monthlyPlan
     * @return
     */
    public boolean dateContainsPossibleVolunteerActivity(LocalDate date, List<Activity> myActivities) {
      
        DayOfWeek dayOfWeek = date.getDayOfWeek(); 

        String nameDay = dayOfWeek.name();

        //lo formatto per averlo in italiano e con solo la prima lettera maiuscola
        nameDay = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ITALIAN);
        nameDay = Character.toUpperCase(nameDay.charAt(0)) + nameDay.substring(1);
        for (Activity a : myActivities) {
            String[] days = a.getProgrammableDays();
            for (String day : days) {
                if(day.equalsIgnoreCase(nameDay)){
                    return true;
                }
            }
        
        }

        return false;
    }

    /**
     * visualizza viste confermate del volontario
     */
    public void showMyConfirmedActivitiesDescription(){
        
        List<ConfirmedActivity> myActivities = getMyConfirmedActivities();

        if(myActivities == null){
            ioService.writeMessage("\nPiano mensile non ancora generato", false);
            return;
        }

        
        ioService.writeMessage(formatter.formatListConfirmedActivity(myActivities), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * metodo che ottiene le mie visite confermate
     * @return
     */
    private List<ConfirmedActivity> getMyConfirmedActivities() {
        MonthlyPlan monthlyPlan = monthlyPlanService.getMonthlyPlan();
        
        if(monthlyPlan == null){
            return null;
        }
        
        List<ConfirmedActivity> myActivities = new ArrayList<>();

        for (Map.Entry<LocalDate, DailyPlan> dailyEntry : monthlyPlan.getMonthlyPlan().entrySet()) {
            LocalDate date = dailyEntry.getKey();
            DailyPlan dailyPlan = dailyEntry.getValue();

            for (Map.Entry<String, ActivityInfo> activityEntry : dailyPlan.getPlan().entrySet()) {
                String activityName = activityEntry.getKey();
                ActivityInfo activityInfo = activityEntry.getValue();

                if (activityInfo.getState() == ActivityState.CONFERMATA && isMyActivity(activityName, getMyActivities())) {
                    myActivities.add(new ConfirmedActivity(activityName, activityInfo.getNumberOfSub() ,activityInfo.getTime(), date));
                }
            }
        }

        return myActivities;
    }

}
