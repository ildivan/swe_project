package server.firstleveldomainservices.configuratorservice;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import com.google.gson.JsonObject;
import server.DateService;
import server.authservice.User;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Place;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuService;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.menus.ConfiguratorMenu;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityRecord;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlanService;
import server.firstleveldomainservices.volunteerservice.VMIOUtil;
import server.firstleveldomainservices.volunteerservice.Volunteer;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.ioservice.objectformatter.IIObjectFormatter;
import server.ioservice.objectformatter.TerminalObjectFormatter;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;
import server.utils.ActivityUtil;
import server.utils.ConfigType;
import server.utils.Configs;
import server.utils.ConfigsUtil;
import server.utils.MainService;


public class ConfigService extends MainService<Void>{
  
    // private static final String GONFIG_MENU = "\n1) Inserire nuovo volotario\n2) Inserire nuovo luogo\n3) Mostra volontari\n4) Mostra luoghi";
    private static final String CONFIG_PLACE_KEY_DESC = "placesFirtsConfigured";
    private static final String CONFIG_ACTIVITY_KEY_DESC = "activitiesFirtsConfigured";
    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";

    private final ConfigType configType;

    private final MenuService menu; 
    private final ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory;
    private final IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private final IInputOutput ioService = new IOService();
    private final IIObjectFormatter<String> formatter= new TerminalObjectFormatter();
    private final IDataLayer<JsonDataLocalizationInformation> dataLayer;
    private final MonthlyConfigService monthlyConfigService;
    private final ActivityUtil activityUtil;
    private final PlacesUtilForConfigService placesUtilForConfigService;
    private final MonthlyPlanService monthlyPlanService;
    private final ConfigsUtil configsUtil;
    
  

    public ConfigService(Socket socket, ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory,
    ConfigType configType, IDataLayer<JsonDataLocalizationInformation> dataLayer) {

        super(socket);
        this.dataLayer = dataLayer;
        this.configType = configType;
        this.locInfoFactory = locInfoFactory;
        this.placesUtilForConfigService = new PlacesUtilForConfigService(locInfoFactory, dataLayer);
        this.monthlyPlanService = new MonthlyPlanService(locInfoFactory, configType, dataLayer);
        this.monthlyConfigService = new MonthlyConfigService(locInfoFactory,dataLayer);
        this.activityUtil = new ActivityUtil(locInfoFactory, configType, dataLayer);
        this.configsUtil = new ConfigsUtil(locInfoFactory, configType, dataLayer);
        this.menu = new ConfiguratorMenu(this, configType, monthlyConfigService, locInfoFactory, dataLayer);
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
            if(configType == ConfigType.NORMAL){
                if(firstTimeConfiguration()){
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

        Configs JO = configsUtil.getConfig();
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
                addPlace();
                configs.setPlacesFirtsConfigured(true);
            }
                //forse devo inglobare anche l'attiità boh io farei unalrtra var nei configs che me lo dice se sono gia configurate
            if(!checkIfConfigured(CONFIG_ACTIVITY_KEY_DESC)){
                ioService.writeMessage("Inizio prima configurazione attività", false);
                addActivity();
                configs.setActivitiesFirtsConfigured(true);
            }
            
            //creo anche i file di sola lettura
            copyToReadOnlyFiles();

            return configsUtil.save(configs, configType);
            
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        
    }

    /**
     * istanzio anche i file di sola lettura
     */
   private void copyToReadOnlyFiles() {

        copyToReadOnlyPlace();
        copyToReadOnlyActivity();
        copyToReadOnlyVolunteer();
    }

    /**
     * istanzio file sola lettura luoghi
     */
    private void copyToReadOnlyPlace() {
        Path changedPlacesPath = Paths.get(locInfoFactory.getChangedPlacesLocInfo().getPath());
        Path originalPlacesPath = Paths.get(locInfoFactory.getPlaceLocInfo().getPath());

        try {
            Files.copy(changedPlacesPath, originalPlacesPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * istanzio file sola lettura luoghi
     */
    private void copyToReadOnlyActivity() {
        Path changedActivitiesPath = Paths.get(locInfoFactory.getChangedActivitiesLocInfo().getPath());
        Path originalActivitiesPath = Paths.get(locInfoFactory.getActivityLocInfo().getPath());

        try {
            Files.copy(changedActivitiesPath, originalActivitiesPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * istanzio file sola lettura luoghi
     */
    private void copyToReadOnlyVolunteer() {
        Path changedVolunteerPath = Paths.get(locInfoFactory.getChangedVolunteersLocInfo().getPath());
        Path originalVolunteerPath = Paths.get(locInfoFactory.getVolunteerLocInfo().getPath());

        try {
            Files.copy(changedVolunteerPath, originalVolunteerPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
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
        int max = ioService.readIntegerWithMinMax("Inserire numero massimo di iscrizioni contemporanee ad una iniziativa", 1, 50);
        
        assert max >= 1 && max <= 50 : "Numero massimo di iscrizioni fuori dai limiti (1-50)";

        return max;

    }

    /**
     * Aggiunge un nuovo volontario al sistema, chiedendo il nome e controllando che non esista già.
     * Se non esiste, lo aggiunge al database dei volontari con password temporanea.
     *
     * @pre L’utente deve inserire un nome non nullo e non vuoto. Il volontario non deve esistere già.
     * @post Se il volontario non esisteva, viene aggiunto al database. In caso contrario, viene mostrato un messaggio.
     */
    public void addVolunteer(boolean first) {
        VMIOUtil volUtil = new VMIOUtil(locInfoFactory, dataLayer);
        if(!first){
            ioService.writeMessage(CLEAR,false);
        }
        
        String name = ioService.readString("\nInserire nome del volontario");

        assert name != null && !name.trim().isEmpty() : "Nome volontario non valido";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedVolunteersLocInfo();
        locInfo.setKey(name);

        if(!volUtil.checkVolunteerExistance(name)) {
            volUtil.addVolunteer(name);

            // Post-condizione: ora esiste
            boolean nowExists = dataLayer.exists(locInfo);
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
        boolean continuare;
        
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();

        do{
                String name = ioService.readString("Inserire nome luogo");
                locInfo.setKey(name);

                assert name != null && !name.trim().isEmpty() : "Il nome del luogo non può essere vuoto";

                if(dataLayer.exists(locInfo)){
                    ioService.writeMessage("Luogo già esistente", false);
                    return;
                }
                String description = ioService.readString("Inserire descrizione luogo");
                ioService.writeMessage("Inserire indirizzo luogo", false);
                Address address = addNewAddress();

                dataLayer.add((jsonFactoryService.createJson(new Place(name, address, description))),locInfo);

                // Post-condizione: il luogo è stato aggiunto
        boolean placeAdded = dataLayer.exists(locInfo);
        assert placeAdded : "Aggiunta del luogo fallita";

            continuare = continueChoice("inserimento luoghi");
        }while(continuare);
    }

    /**
     * util method for the previous method to add a new address
     * @return the new address
     */
    private Address addNewAddress() {
        return activityUtil.getAddress();
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

        //controllo se ci sono luoghi senza attvità
        if(placesUtilForConfigService.existPlaceWithNoActivity()){
            ioService.writeMessage("\nSono presenti luoghi senza attività, inserire almeno una attività per ogniuno", false);
            addActivityOnNoConfiguredPlaces();
            if((ioService.readString("Si vogliono inserire nuove attività?: (y/n)")).equalsIgnoreCase("n")){
                return;
            }
        }
       
        //aggiungo nuove attività
        do{
            if(jump){
                continue;
            }
            showPlaces();
            String placeName = ioService.readString("\nInserire luogo per l'attività");

            JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();
    
            locInfo.setKey(placeName);

            while(!dataLayer.exists(locInfo)){
                ioService.writeMessage("Luogo non esistente, riprovare", false);
                placeName = ioService.readString("\nInserire luogo per l'attività");

            }
                
            Place place = jsonFactoryService.createObject(dataLayer.get(locInfo), Place.class);   

            // Pre-condizione: il luogo selezionato deve esistere
            assert place != null : "Il luogo selezionato non esiste";

            addActivityWithPlace(place);


        }while(continueChoice("aggiunta attività"));
    

    }

    /**
     * show places where there is no activity related
     */
    private void addActivityOnNoConfiguredPlaces() {
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
        assert place != null;
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedActivitiesLocInfo();

        if(noVolunteersExists()){
            //entro qua solo nella prima iterazione quando non ho nessun volontario, oppure se li elimino e non ho piu volontari
            ioService.writeMessage("\nNecessario inserire almeno un volontario per procedere", false);
            addVolunteer(true);
        }

        Activity activity = activityUtil.getActivity(place);

        dataLayer.add(jsonFactoryService.createJson(activity), locInfo);
    
    }


    /**
     * method that checks if there is at leat one volunteer declared
     * 
     * poiche il metodo è triggereato solo nella prima configurazione, o quando non ho piu volontari lavoro sul file changed
     * @return
     */
    private boolean noVolunteersExists() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedVolunteersLocInfo();
        if (dataLayer.get(locInfo) == null) {
            return true;
        }

        return false;
    }

    /**
     * Mostra tutti i volontari registrati nel sistema.
     */
    public void showVolunteers() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();

        List<JsonObject> volunteersJO = dataLayer.getAll(locInfo);
        List<Volunteer> volunteers = jsonFactoryService.createObjectList(volunteersJO, Volunteer.class);
        ioService.writeMessage(formatter.formatListVolunteer(volunteers), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * method to show all places
     */
    public void showPlaces() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getPlaceLocInfo();

        List<JsonObject> placesJO = dataLayer.getAll(locInfo);
        List<Place> places = jsonFactoryService.createObjectList(placesJO, Place.class);

        ioService.writeMessage(formatter.formatListPlace(places), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * method to show all activities
     */
    public void showActivities() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getActivityLocInfo();

        List<JsonObject> activitiesJO = dataLayer.getAll(locInfo);
        List<Activity> activities = jsonFactoryService.createObjectList(activitiesJO, Activity.class);

        ioService.writeMessage(formatter.formatListActivity(activities), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * mostra le visite in base allo stato richiesto
     * @param desiredState stato delle visite che vuoi visualizzare
     */
    public void showActivitiesWithCondition(ActivityState desiredState) {
        
        List<ActivityRecord> result = activityUtil.getActiviyByState(desiredState);
    
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
        MonthlyConfig mc = monthlyConfigService.getMonthlyConfig();
        mc.setDaysBeforeActivityConfirmation(daysBeforeActivityConfirmation);
        monthlyConfigService.saveMonthlyConfig(mc);
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

        MonthlyPlan monthlyPlan = monthlyPlanService.getMonthlyPlan();

        if(monthlyPlan == null){
            ioService.writeMessage("Piano Mensile non ancora generato", false);
            return;
        }

        ioService.writeMessage(CLEAR,false);
    
        ioService.writeMessage(formatter.formatMonthlyPlan(monthlyPlan), false);
    }

    /**
     * method to add a non usable date for the next monthly plan
     */
    public void addNonUsableDate(){
        DateService dateService = new DateService();

        ioService.writeMessage(CLEAR,false);

        MonthlyConfig mc = monthlyConfigService.getMonthlyConfig();

        int maxNumDay = mc.getMonthAndYear().getMonth().length(mc.getMonthAndYear().isLeapYear());
        int minNumDay = 1;
        
        int day = ioService.readIntegerWithMinMax("Inserire giorno non disponibile", minNumDay, maxNumDay);

        int month = dateService.setMonthOnPrecludeDay(mc, day);
        int year = dateService.setYearOnPrecludeDay(mc, day);

        mc.getPrecludeDates().add(LocalDate.of(year, month, day));
        monthlyConfigService.saveMonthlyConfig(mc);

    }

    /**
     * Metodo per eliminare un volontario e tutte le attività orfane in cui è coinvolto.
     *
     * @pre Il nome inserito deve essere non nullo e non vuoto.
     * @post
     * - Il volontario viene eliminato dal sistema.
     * - Tutte le attività in cui era presente vengono aggiornate.
     * - Le attività che restano senza volontari vengono eliminate.
     */
    public void deleteVolunteer() {
        VMIOUtil volUtil = new VMIOUtil(locInfoFactory, dataLayer);

        ioService.writeMessage(CLEAR, false);
        String name = ioService.readString("\nInserire nome del volontario da eliminare");

        assert name != null && !name.trim().isEmpty() : "Nome volontario non valido";

        if (volUtil.checkVolunteerExistance(name)) {

            JsonDataLocalizationInformation activityLoc = locInfoFactory.getChangedActivitiesLocInfo();
            List<JsonObject> allActivities = dataLayer.getAll(activityLoc);

            for (JsonObject activity : allActivities) {
                if (activity.has("volunteers")) {
                    var volunteers = activity.getAsJsonArray("volunteers");

                    // Rimuovi il volontario dalla lista
                    for (int i = 0; i < volunteers.size(); i++) {
                        if (volunteers.get(i).getAsString().equalsIgnoreCase(name)) {
                            volunteers.remove(i);
                            break;
                        }
                    }

                    String activityName = activity.get("title").getAsString();
                    JsonDataLocalizationInformation singleActLoc = locInfoFactory.getChangedActivitiesLocInfo();
                    singleActLoc.setKey(activityName);

                    if (volunteers.size() == 0) {
                        // Nessun volontario rimasto → elimina l’attività
                        dataLayer.delete(singleActLoc);
                    } else {
                        // Altrimenti salva la versione aggiornata dell’attività
                        dataLayer.modify(activity, singleActLoc);
                    }
                }
            }

            volUtil.deleteVolunteer(name);
            ioService.writeMessage("\nVolontario e attività orfane aggiornate/eliminate.", false);
        
        } else {
            ioService.writeMessage("\nVolontario non esistente", false);
        }
    }

    /**
     * Metodo per eliminare un luogo dal sistema e tutte le attività che lo usano.
     *
     * @pre Il nome del luogo è non nullo e non vuoto.
     * @post
     * - Il luogo viene eliminato.
     * - Tutte le attività che si svolgono in quel luogo vengono eliminate.
     */
    public void deletePlace() {
        ioService.writeMessage(CLEAR, false);
        String name = ioService.readString("\nInserire nome del luogo da eliminare");

        assert name != null && !name.trim().isEmpty() : "Nome luogo non valido";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();
        locInfo.setKey(name);

        if (dataLayer.exists(locInfo)) {
            // Elimina tutte le attività che usano questo luogo
            JsonDataLocalizationInformation activityLoc = locInfoFactory.getChangedActivitiesLocInfo();
            List<JsonObject> allActivities = dataLayer.getAll(activityLoc);

            for (JsonObject activity : allActivities) {
                if (activity.has("placeName")) {
                    String activityPlace = activity.get("placeName").getAsString();
                    if (activityPlace.equalsIgnoreCase(name)) {
                        JsonDataLocalizationInformation singleActLoc = locInfoFactory.getChangedActivitiesLocInfo();
                        singleActLoc.setKey(activity.get("title").getAsString());
                        dataLayer.delete(singleActLoc);
                    }
                }
            }

            dataLayer.delete(locInfo);
            ioService.writeMessage("\nLuogo e attività collegate eliminate", false);
        } else {
            ioService.writeMessage("\nLuogo non esistente", false);
        }
    }


    /**
     * Metodo per eliminare un'attività dal sistema.
     *
     * @pre Il nome dell’attività è non nullo e valido.
     * @post L’attività, se presente, viene eliminata dal sistema.
     */
    public void deleteActivity() {
        ioService.writeMessage(CLEAR, false);
        String name = ioService.readString("\nInserire nome dell'attività da eliminare");

        assert name != null && !name.trim().isEmpty() : "Nome attività non valido";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedActivitiesLocInfo();
        locInfo.setKey(name);

        if (dataLayer.exists(locInfo)) {
            dataLayer.delete(locInfo);
            ioService.writeMessage("\nAttività eliminata", false);
        } else {
            ioService.writeMessage("\nAttività non esistente", false);
        }
    }

    public void modifyData(ConfigType configType) {
        EditPossibilitiesService editPossibilitiesService = new EditPossibilitiesService(socket, locInfoFactory, configType, dataLayer);
        editPossibilitiesService.run();
    }
    
}
