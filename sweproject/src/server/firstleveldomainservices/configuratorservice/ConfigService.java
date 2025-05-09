package server.firstleveldomainservices.configuratorservice;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import server.datalayerservice.*;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Place;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.ConfiguratorMenu;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlanService;
import server.firstleveldomainservices.volunteerservice.Volunteer;
import server.ioservice.AMIOUtil;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.ioservice.objectformatter.IIObjectFormatter;
import server.ioservice.objectformatter.TerminalObjectFormatter;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;
import server.objects.Configs;
import server.objects.MainService;


public class ConfigService extends MainService<Void>{
  
    // private static final String GONFIG_MENU = "\n1) Inserire nuovo volotario\n2) Inserire nuovo luogo\n3) Mostra volontari\n4) Mostra luoghi";
    private static final String CONFIG_PLACE_KEY_DESC = "placesFirtsConfigured";
    private static final String CONFIG_ACTIVITY_KEY_DESC = "activitiesFirtsConfigured";
    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";
    
    private static final String ACTIVITY_PATH = "JF/activities.json";
    private static final String ACTIVITY_MEMBER_NAME = "activities";
    private static final String MONTHLY_PLAN_PATH = "JF/monthlyPlan.json";
    private static final String MONTHLY_PLAN_MEMBER_NAME = "monthlyPlan";
    private static final String PLACES_PATH = "JF/places.json";
    private static final String PLACES_MEMBER_NAME = "places";
    private static final String PLACES_KEY_DESC = "name";
    private static final String GENERAL_CONFIGS_KEY_DESCRIPTION = "configType";
    private static final String GENERAL_CONFIGS_MEMBER_NAME = "configs";
    private static final String GENERAL_CONFIG_PATH = "JF/configs.json";
    private static final String VOLUNTEER_PATH = "JF/volunteers.json";
    private static final String VOLUNTEER_MEMBER_NAME = "volunteers";
    private static final String VOLUNTEER_KEY_DESC = "name";
    private static final String MONTHLY_CONFIG_KEY_DESC = "type";
    private static final String MONTHLY_CONFIG_KEY = "current";
    private static final String MONTHLY_CONFIG_MEMEBER_NAME = "mc";
    private static final String MONTHLY_CONFIG_PATH = "JF/monthlyConfigs.json";

    private Gson gson;
    private MenuService menu = new ConfiguratorMenu(this);
    private String configType;

    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private IInputOutput ioService = new IOService();
    private IIObjectFormatter<String> formatter= new TerminalObjectFormatter();
    
  

    public ConfigService(Socket socket, Gson gson, String configType) {
        super(socket);
        this.configType = configType;
        this.gson = gson;
    }
    /**
     * apply the logic of the service
     * @return null
     * @throws IOException
     */
    public Void applyLogic() throws IOException {
        
        boolean continuare = true;
        
            /*
             * classe che mi gestisce la valutazione della data odierna qua, in base alla data mi restituisce 
             * la mappa con le varie possibili voci
             */
            if(configType.equalsIgnoreCase("normalFunctionConfigs")){
                if(firstTimeConfiguration()){
                    ioService.writeMessage("Configurazione base completata", false);
                }else{
                    ioService.writeMessage("Errore durante la configurazione", false);
                    return null;
                }
            };

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
     * check if place and max number of subscriptions are configured -> firtst things to configure
     * @return true if the user is already configured
     */
    private boolean checkIfConfigured(String keyDesc) {

        Configs JO = getConfig();
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
    
            //richiesta la configurazone dei luoghi e delle attivita
            //in base a come vanno le due cose sopra, modifico i config in un metodo a parte
            Configs configs = new Configs();
            configs.setUserConfigured(true);
            configs.setAreaOfIntrest(areaOfIntrest);
            configs.setMaxSubscriptions(maxSubscriptions);
    
       
            if(!checkIfConfigured(CONFIG_PLACE_KEY_DESC)){
                ioService.writeMessage("Inizio prima configurazione luoghi", false);
                addPlace();
                configs.setPlacesFirtsConfigured(true);
            }
                //forse devo inglobare anche l'attiità boh io farei unalrtra var nei configs che me lo dice se sono gia configurate
            if(!checkIfConfigured(CONFIG_ACTIVITY_KEY_DESC)){
                ioService.writeMessage("Inizio prima configurazione attività", false);
                addActivity();
                configs.setActivitiesFirtsConfigured(true);
            }
            
            JsonObject JO = jsonFactoryService.createJson(configs);
            
            JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
            locInfo.setPath(GENERAL_CONFIG_PATH);
            locInfo.setMemberName(GENERAL_CONFIGS_MEMBER_NAME);
            locInfo.setKeyDesc(GENERAL_CONFIGS_KEY_DESCRIPTION);
            locInfo.setKey("normalFunctionConfigs");

            
            
            return DataLayerDispatcherService.startWithResult(locInfo, layer->layer.modify(JO, locInfo));
            

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
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
        Integer max = ioService.readIntegerWithMinMax("Inserire numero massimo di iscrizioni contemporanee ad una iniziativa", 1, 50);
        
        assert max != null : "Il valore restituito non deve essere null";
        assert max >= 1 && max <= 50 : "Numero massimo di iscrizioni fuori dai limiti (1-50)";

        return max;

    }

    /**
     * Modifica il numero massimo di iscrizioni contemporanee per una iniziativa.
     *
     * @pre Il sistema deve essere correttamente configurato e l’utente deve inserire un numero valido tra 1 e 50.
     * @post Il nuovo numero massimo è salvato nella configurazione.
     */
    public void modNumMaxSub(){
        ioService.writeMessage(CLEAR,false);
        Integer n = ioService.readIntegerWithMinMax("\nInserire nuovo numero di iscrizioni massime (massimo numero 50)",1,50);

        assert n != null && n >= 1 && n <= 50 : "Valore non valido per il numero massimo di iscrizioni";

        Configs configs = getConfig();

        assert configs != null : "Configurazione non trovata";

        configs.setMaxSubscriptions(n);
        JsonObject newConfigsJO = jsonFactoryService.createJson(configs);

        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(GENERAL_CONFIG_PATH);
        locInfo.setMemberName(GENERAL_CONFIGS_MEMBER_NAME);
        locInfo.setKeyDesc(GENERAL_CONFIGS_KEY_DESCRIPTION);
        locInfo.setKey(configType);
        
        boolean modified = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.modify(newConfigsJO, locInfo));

        assert modified : "Modifica configurazione fallita";

        ioService.writeMessage("\nNumero massimo di iscrizioni modificato", false);
    }

    /**
     * Aggiunge un nuovo volontario al sistema, chiedendo il nome e controllando che non esista già.
     * Se non esiste, lo aggiunge al database dei volontari con password temporanea.
     *
     * @pre L’utente deve inserire un nome non nullo e non vuoto. Il volontario non deve esistere già.
     * @post Se il volontario non esisteva, viene aggiunto al database. In caso contrario, viene mostrato un messaggio.
     */
    public void addVolunteer() {
        ioService.writeMessage(CLEAR,false);
        String name = ioService.readString("\nInserire nome del volontario");

        assert name != null && !name.trim().isEmpty() : "Nome volontario non valido";

        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(VOLUNTEER_PATH);
        locInfo.setMemberName(VOLUNTEER_MEMBER_NAME);
        locInfo.setKeyDesc(VOLUNTEER_KEY_DESC);
        locInfo.setKey(name);

        if (!DataLayerDispatcherService.startWithResult(locInfo, layer->layer.exists(locInfo))) {
            DataLayerDispatcherService.start(locInfo, layer->layer.add(jsonFactoryService.createJson(new Volunteer(name)), locInfo));

            // Post-condizione: ora esiste
            boolean nowExists = DataLayerDispatcherService.startWithResult(locInfo, layer -> layer.exists(locInfo));
            assert nowExists : "Aggiunta volontario fallita";
        } else {
            ioService.writeMessage("\nVolontario già esistente", false);
        }
    }   

    /**
     * Aggiunge un nuovo luogo al sistema dopo aver richiesto il nome, la descrizione e l'indirizzo.
     * Se il luogo non esiste già, viene aggiunto al database.
     *
     * @pre Il nome e la descrizione del luogo devono essere non nulli e non vuoti. Il luogo non deve esistere già.
     * @post Il luogo viene aggiunto al database se non esiste già. In caso contrario, viene mostrato un messaggio.
     */
    public void addPlace(){
        ioService.writeMessage(CLEAR,false);
        boolean continuare = false;
        
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(PLACES_PATH);
        locInfo.setMemberName(PLACES_MEMBER_NAME);
        locInfo.setKeyDesc(PLACES_KEY_DESC);
        

        do{
                String name = ioService.readString("Inserire nome luogo");
                locInfo.setKey(name);

                assert name != null && !name.trim().isEmpty() : "Il nome del luogo non può essere vuoto";

                if(DataLayerDispatcherService.startWithResult(locInfo, layer->layer.exists(locInfo))){
                    ioService.writeMessage("Luogo già esistente", false);
                    return;
                }
                String description = ioService.readString("Inserire descrizione luogo");
                ioService.writeMessage("Inserire indirizzo luogo", false);
                Address address = addNewAddress();

                DataLayerDispatcherService.start(locInfo, layer->layer.add((jsonFactoryService.createJson(new Place(name, address, description))),locInfo));

                // Post-condizione: il luogo è stato aggiunto
        boolean placeAdded = DataLayerDispatcherService.startWithResult(locInfo, layer -> layer.exists(locInfo));
        assert placeAdded : "Aggiunta del luogo fallita";

            continuare = continueChoice("inserimento luoghi");
        }while(continuare);
    }

    /**
     * util method for the previous method to add a new address
     * @return the new address
     * @throws IOException
     */
    private Address addNewAddress() {
        return AMIOUtil.getAddress();
    }

    /**
     * Aggiunge una nuova attività al sistema, chiedendo di selezionare un luogo esistente
     * e quindi inserire i dettagli dell'attività.
     * Se il luogo selezionato non ha già un'attività associata, l'attività viene aggiunta.
     *
     * @pre Deve esserci almeno un luogo configurato senza attività. Il luogo selezionato deve esistere.
     */
    public void addActivity() {
        ioService.writeMessage(CLEAR,false);
        boolean jump = false;

        //DA TESTARE QUESTO IF
        if(PlacesUtilForConfigService.existPlaceWithNoActivity()){
            ioService.writeMessage("\nSono presenti luoghi senza attività, inserire almeno una attività per ogniuno", false);
            addActivityOnNoConfiguredPlaces();
            if((ioService.readString("Si vogliono inserire nuove attività?: (y/n)")).equalsIgnoreCase("n")){
                return;
            }
        }
       
            do{
                if(jump){
                    continue;
                }
                showPlaces();
                String placeName = ioService.readString("\nInserire luogo per l'attività");

                JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
                locInfo.setPath(PLACES_PATH);
                locInfo.setMemberName(PLACES_MEMBER_NAME);
                locInfo.setKeyDesc(PLACES_KEY_DESC);
                locInfo.setKey(placeName);

                while(!DataLayerDispatcherService.startWithResult(locInfo, layer->layer.exists(locInfo))){
                    ioService.writeMessage("Luogo non esistente, riprovare", false);
                        placeName = ioService.readString("\nInserire luogo per l'attività");

                }
                    
                Place place = jsonFactoryService.createObject(DataLayerDispatcherService.startWithResult(locInfo
                ,layer->layer.get(locInfo)), Place.class);   

                // Pre-condizione: il luogo selezionato deve esistere
                assert place != null : "Il luogo selezionato non esiste";

                addActivityWithPlace(place);

    
            }while(continueChoice("aggiunta attività"));
        

    }

    /**
     * show places where there is no activity related
     */
    private void addActivityOnNoConfiguredPlaces() {
        PlacesUtilForConfigService placesUtilForConfigService = new PlacesUtilForConfigService();
        List<Place> places = placesUtilForConfigService.getCustomList();
        for (Place place : places) {
            ioService.writeMessage("Inserire attività per il luogo:\n " + formatter.formatPlace(place), false);
              addActivityWithPlace(place);
        }
    }
              
    /**
     * creat an activity on the place passed in input in the method
     * @param place place to relate the activity
     */
    private void addActivityWithPlace(Place place) {
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(ACTIVITY_PATH);
        locInfo.setMemberName(ACTIVITY_MEMBER_NAME);

        Activity activity = AMIOUtil.getActivity(place);

        DataLayerDispatcherService.start(locInfo, layer->layer.add(jsonFactoryService.createJson(activity), locInfo));
        
    }

    /**
     * Mostra tutti i volontari registrati nel sistema.
     */
    public void showVolunteers() {
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(VOLUNTEER_PATH);
        locInfo.setMemberName(VOLUNTEER_MEMBER_NAME);
        List<JsonObject> volunteersJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.getAll(locInfo));
        List<Volunteer> volunteers = jsonFactoryService.createObjectList(volunteersJO, Volunteer.class);
        ioService.writeMessage(formatter.formatListVolunteer(volunteers), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * method to show all places
     */
    public void showPlaces() {
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(PLACES_PATH);
        locInfo.setMemberName(PLACES_MEMBER_NAME);
        List<JsonObject> placesJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.getAll(locInfo));
        List<Place> places = jsonFactoryService.createObjectList(placesJO, Place.class);

        ioService.writeMessage(formatter.formatListPlace(places), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * method to show all activities
     */
    public void showActivities() {
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(ACTIVITY_PATH);
        locInfo.setMemberName(ACTIVITY_MEMBER_NAME);
        List<JsonObject> activitiesJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.getAll(locInfo));
        List<Activity> activities = jsonFactoryService.createObjectList(activitiesJO, Activity.class);

        ioService.writeMessage(formatter.formatListActivity(activities), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * method to generate a monthly plan
     * 
     */

    public void generateMonthlyPlan() {
        MonthlyPlanService monthlyPlanService = new MonthlyPlanService();
        monthlyPlanService.buldMonthlyPlan();
        ioService.writeMessage("Piano mensile generato", false);
        showMonthlyPlan();
    }

    /**
     * method to show monthly plan
     * UNIMPLEMENTED
     */
    private void showMonthlyPlan() {
       
    }

    /**
     * method to add a non usable date for the next monthly plan
     */
    public void addNonUsableDate(){
        ioService.writeMessage(CLEAR,false);

        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(MONTHLY_CONFIG_PATH);
        locInfo.setMemberName(MONTHLY_CONFIG_MEMEBER_NAME);
        locInfo.setKeyDesc(MONTHLY_CONFIG_KEY_DESC);
        locInfo.setKey(MONTHLY_CONFIG_KEY);

        JsonObject mcJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.get(locInfo));
        MonthlyConfig mc = jsonFactoryService.createObject(mcJO, MonthlyConfig.class);

        int maxNumDay = mc.getMonthAndYear().getMonth().length(mc.getMonthAndYear().isLeapYear());
        int minNumDay = 1;
            

        
        int day = ioService.readIntegerWithMinMax("Inserire giorno non disponibile", minNumDay, maxNumDay);

        int month = setMonthOnPrecludeDay(mc, day);
        int year = setYearOnPrecludeDay(mc, day);

        mc.getPrecludeDates().add(LocalDate.of(year, month, day));
        JsonObject newConfigsJO = jsonFactoryService.createJson(mc);

        DataLayerDispatcherService.startWithResult(locInfo, layer->layer.modify(newConfigsJO, locInfo));

    }

    /*
     * setta l'anno in base al giorno
     */
    private int setYearOnPrecludeDay(MonthlyConfig mc, int day) {
        int year = mc.getMonthAndYear().getYear();

        if(day>=17 && day<=31){
            return year;
        }else{
            if( mc.getMonthAndYear().getMonthValue() == 12){
                return year +1;
            }
            return year;
        }
    }

    /*
     * asssegna al giorno il mese,
     * da 17 a 31 assegna il mese dello sviluppo del piano
     * da 1 a 16 assegna il mese successivo
     */
    private int setMonthOnPrecludeDay(MonthlyConfig mc, int day) {
        int monthOfPlan = mc.getMonthAndYear().getMonthValue();
        
        if(day>=17 && day<=31){
            return monthOfPlan;
        }else{
            return monthOfPlan+1;
        }
    }

    private Configs getConfig(){
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(GENERAL_CONFIG_PATH);
        locInfo.setMemberName(GENERAL_CONFIGS_MEMBER_NAME);
        locInfo.setKeyDesc(GENERAL_CONFIGS_KEY_DESCRIPTION);
        locInfo.setKey(configType);

        JsonObject cJO = DataLayerDispatcherService.startWithResult(locInfo, layer -> layer.get(locInfo));
        return jsonFactoryService.createObject(cJO, Configs.class);
    }
    
}
