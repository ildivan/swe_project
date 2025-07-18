package server.firstleveldomainservices.configuratorservice;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import server.DateService;
import server.data.Activity;
import server.data.Place;
import server.data.Volunteer;
import server.data.facade.FacadeHub;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuService;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.menus.ConfiguratorMenu;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityRecord;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlanBuilder;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.ioservice.objectformatter.IIObjectFormatter;
import server.ioservice.objectformatter.TerminalObjectFormatter;
import server.utils.ActivityUtil;
import server.utils.ConfigType;
import server.utils.Configs;
import server.utils.MainService;


public class ConfigService extends MainService<Void>{
  
    private static final int MONTH_TO_ADD_PRECLUDE_DATE = 3;
    private static final String USER_KEY_DESC = "userConfigured";
    private static final String CONFIG_PLACE_KEY_DESC = "placesFirtsConfigured";
    private static final String CONFIG_ACTIVITY_KEY_DESC = "activitiesFirtsConfigured";
    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";

    private final ConfigType configType;
    private final MenuService menu; 
    private final IInputOutput ioService = new IOService();
    private final IIObjectFormatter<String> formatter= new TerminalObjectFormatter();
    private final ActivityUtil activityUtil;
    private final MonthlyPlanBuilder monthlyPlanService;
    private final EditPossibilitiesService editPossibilitiesService;
    private final FacadeHub data;
    

    public ConfigService(Socket socket,
    ConfigType configType, FacadeHub data) {

        super(socket);
        this.configType = configType;
        this.monthlyPlanService = new MonthlyPlanBuilder(configType, data);
        this.activityUtil = new ActivityUtil(data);
        this.editPossibilitiesService = new EditPossibilitiesService(socket,configType, data);
        this.menu = new ConfiguratorMenu(this, configType,data);
        this.data = data;
    }   

    /**
     * apply the logic of the service
     * @return null
     */
    public Void applyLogic() {
        
            /*
             * classe che mi gestisce la valutazione della data odierna qua, in base alla data mi restituisce 
             * la mappa con le varie possibili voci
             */
            if(configType == ConfigType.NORMAL && !checkIfConfigured(USER_KEY_DESC)){
                if(firstTimeConfiguration() ){
                    ioService.writeMessage("Configurazione base completata", false);
                }else{
                    ioService.writeMessage("Errore durante la configurazione", false);
                    return null;
                }
            }

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
     * check if place and max number of subscriptions are configured -> firtst things to configure
     * @return true if the user is already configured
     */
    private boolean checkIfConfigured(String keyDesc) {

        Configs JO = data.getConfigFacade().getConfig(configType);
        return JO.getUserConfigured();
    }

    /**
     * starts the first configuration of areaofintrest maxsubscriptions, places and activities related
     * @return true if ended correctly
     */
    private boolean firstTimeConfiguration(){
        try {
            ioService.writeMessage("Prima configurazione necessaria:", false);
            String areaOfIntrest = configureArea();
            Integer maxSubscriptions = configureMaxSubscriptions();
    
            Configs configs = new Configs();
            configs.setUserConfigured(true);
            configs.setAreaOfIntrest(areaOfIntrest);
            configs.setMaxSubscriptions(maxSubscriptions);
    
            if(!checkIfConfigured(CONFIG_PLACE_KEY_DESC)){
                ioService.writeMessage("Inizio prima configurazione luoghi", false);
                editPossibilitiesService.addPlace();
                configs.setPlacesFirtsConfigured(true);
            }
            if(!checkIfConfigured(CONFIG_ACTIVITY_KEY_DESC)){
                ioService.writeMessage("Inizio prima configurazione attività", false);
                editPossibilitiesService.addActivity();
                configs.setActivitiesFirtsConfigured(true);
            }
            
            //creo anche i file di sola lettura
            copyToReadOnlyFiles();

            return data.getConfigFacade().save(configs, configType);
            
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        
    }

    /**
     * istanzio anche i file di sola lettura
     */
   private void copyToReadOnlyFiles() {

        data.getPlacesFacade().copyToReadOnlyPlace();
        data.getActivitiesFacade().copyToReadOnlyActivity();
   }

   /**
     * Chiede all’utente di inserire l’area di interesse e la restituisce.
     *
     * @pre Nessuna precondizione. L'input può essere qualsiasi stringa non nulla.
     * @post Ritorna una stringa non nulla e non vuota rappresentante l'area di interesse.
     *
     * @return l'area di interesse inserita dall'utente.
     * @throws IOException se si verifica un errore nella lettura dell’input.
     */
    private String configureArea() throws IOException{
        String area = ioService.readString("Inserire luogo di esercizio");
        
        assert area != null && !area.trim().isEmpty() : "Area di interesse non valida (nulla o vuota)";

        return area;
    }
    
    /**
     * Chiede all'utente di inserire il numero massimo di iscrizioni contemporanee
     * per una singola iniziativa.
     *
     * @pre Nessuna precondizione, l’utente deve solo inserire un numero compreso tra 1 e 50.
     * @post Ritorna un intero maggiore di 0 e minore o uguale a 50.
     *
     * @return il numero massimo di iscrizioni contemporanee consentite.
     * @throws IOException se si verifica un errore nella lettura dell’input.
     */
    private Integer configureMaxSubscriptions() throws IOException{
        int max = ioService.readIntegerWithMinMax("Inserire numero massimo di iscrizioni contemporanee ad una iniziativa", 1, 50);
        
        assert max >= 1 && max <= 50 : "Numero massimo di iscrizioni fuori dai limiti (1-50)";

        return max;

    }

    /**
     * Mostra tutti i volontari registrati nel sistema.
     */
    public void showVolunteers() {
        List<Volunteer> volunteers = data.getVolunteersFacade().getVolunteers();
        ioService.writeMessage(formatter.formatListVolunteer(volunteers), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * method to show all places
     */
    public void showPlaces() {
        List<Place> places = data.getPlacesFacade().getPlaces();

        ioService.writeMessage(formatter.formatListPlace(places), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * method to show all activities
     */
    public void showActivities() {
        List<Activity> activities = data.getActivitiesFacade().getActivities();

        ioService.writeMessage(formatter.formatListActivity(activities), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * mostra le visite in base allo stato richiesto
     * @param desiredState stato delle visite che vuoi visualizzare
     */
    public void showActivitiesWithCondition(ActivityState desiredState) {
        
        List<ActivityRecord> result = activityUtil.getActivitiesByState(desiredState);
    
        ioService.writeMessage(formatter.formatListActivityRecord(result), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * method to generate a monthly plan
     * 
     */
    public void generateMonthlyPlan() {

        updateDaysBeforeConfirmation(choseTheAmountOfDaysBeforeTheActivityToBeConfirmed());

        if(!monthlyPlanService.buildMonthlyPlan()){
            ioService.writeMessage("Piano mensile non generato, volontari stanno modificando", false);
            return;
        };
        ioService.writeMessage("Piano mensile generato", false);
        showMonthlyPlan();
    }

    /**
     * salva il numero di giorni prima dell'attività quando effettuo la verifica
     * @param daysBeforeActivityConfirmation
     */
    private void updateDaysBeforeConfirmation(int daysBeforeActivityConfirmation) {
        MonthlyConfig mc = data.getMonthlyConfigFacade().getMonthlyConfig();
        mc.setDaysBeforeActivityConfirmation(daysBeforeActivityConfirmation);
        data.getMonthlyConfigFacade().saveMonthlyConfig(mc);
    }

    /**
     * metodo per ottenere quanti giorni prima controllo se posso confermare una visita
     * @return
     */
    private int choseTheAmountOfDaysBeforeTheActivityToBeConfirmed() {
        return ioService.readIntegerWithMinMax("\nQuanto prima dell'attività (in giorni) verifico se ho raggiunto il numero minimo di iscrizioni?", 1, 25);
    }

    /**
     * method to show monthly plan
     *
     */
    public void showMonthlyPlan() {

        MonthlyPlan monthlyPlan = data.getMonthlyPlanFacade().getMonthlyPlan();

        if(monthlyPlan == null){
            ioService.writeMessage("Piano Mensile non ancora generato", false);
            return;
        }

        ioService.writeMessage(CLEAR,false);
    
        ioService.writeMessage(formatter.formatMonthlyPlan(monthlyPlan), false);
    }

    /**
     * method to add a preclude date
     */
    public void addPrecludeDate(){
        DateService dateService = new DateService();

        ioService.writeMessage(CLEAR,false);

        MonthlyConfig mc = data.getMonthlyConfigFacade().getMonthlyConfig();

        int maxNumDay = dateService.getMaxNumDay(mc, MONTH_TO_ADD_PRECLUDE_DATE);
        int minNumDay = 1;
        
        int day = ioService.readIntegerWithMinMax("Inserire giorno precluso alle visite", minNumDay, maxNumDay);

        boolean firstPlanConfigured = data.getConfigFacade().getConfig(configType).getFirstPlanConfigured();
        int month = mc.getMonthAndYear().getMonth().plus(2).getValue();
        
        int year = mc.getMonthAndYear().getYear();

        if(dateService.incrementYearOnPrecludeDay(mc.getMonthAndYear().getMonthValue(), firstPlanConfigured)){
            year =+1;
        }

        data.getPrecludeDateFacade().savePrecludeDate(LocalDate.of(year, month, day));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        ioService.writeMessage(String.format("Aggiunta data preclusa: %s", LocalDate.of(year, month, day).format(dateFormatter)), false);

    }

    public void modifyData(ConfigType configType) {
        editPossibilitiesService.run();
    }
    
}
