package server.firstleveldomainservices.configuratorservice;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonObject;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Place;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuService;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.menus.EditMenu;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.PlanState;
import server.firstleveldomainservices.volunteerservice.VMIOUtil;
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

public class EditPossibilitiesService extends MainService<Void>{

    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";
    private static final String CONFIG_PLACE_KEY_DESC = "placesFirtsConfigured";
    private static final String CONFIG_ACTIVITY_KEY_DESC = "activitiesFirtsConfigured";


    private final MenuService menu = new EditMenu(this);

    private final ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory;    
    private final IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private final IInputOutput ioService = new IOService();
    private final IDataLayer<JsonDataLocalizationInformation> dataLayer;
    private final MonthlyConfigService monthlyConfigService;
    private final IIObjectFormatter<String> formatter= new TerminalObjectFormatter();
    private final ActivityUtil activityUtil;
    private final ConfigsUtil configsUtil;
    private final PlacesUtilForConfigService placesUtilForConfigService;
    private final ConfigType configType;

    public EditPossibilitiesService(Socket socket, ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory,
    ConfigType configType, IDataLayer<JsonDataLocalizationInformation> dataLayer,
    ConfigsUtil configsUtil) {
        super(socket);

        this.configsUtil = configsUtil;
        this.dataLayer = dataLayer;
        this.configType = configType;
        this.locInfoFactory = locInfoFactory;
        this.placesUtilForConfigService = new PlacesUtilForConfigService(locInfoFactory, dataLayer);
        this.monthlyConfigService = new MonthlyConfigService(locInfoFactory,dataLayer);
        this.activityUtil = new ActivityUtil(locInfoFactory, configType, dataLayer);
    }


    @Override
    protected Void applyLogic() throws IOException {

        MonthlyConfig monthlyConfig = monthlyConfigService.getMonthlyConfig();

        if(!monthlyConfig.getPlanStateMap().get(PlanState.MODIFICHE_APERTE)){
            ioService.writeMessage("\n\nFase di modifica non disponibile, piano corrente non ancora generato\n\n", false);
            return null;
        }

        doOperations();
        ioService.writeMessage("\nFase modifica dati conclusa\n", false);
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

        setIsBeingConfigured(PlanState.MODIFICHE_APERTE, false);
        setIsBeingConfigured(PlanState.DISPONIBILITA_APERTE, true);
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

        if(!volUtil.checkVolunteerExistance(name)) {
            volUtil.addVolunteer(name);

            // Post-condizione: ora esiste
            boolean nowExists = volUtil.checkVolunteerExistance(name);
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
        MonthlyConfig monthlyConfig = monthlyConfigService.getMonthlyConfig();

        if(checkIfConfigured(CONFIG_PLACE_KEY_DESC)){
            if(!monthlyConfig.getPlanStateMap().get(PlanState.MODIFICHE_APERTE)){
                ioService.writeMessage("\n\nFase di modifica non disponibile, piano corrente non ancora generato\n\n", false);
                return;
            }
        }

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
     * check if place and max number of subscriptions are configured -> firtst things to configure
     * @return true if the user is already configured
     */
    private boolean checkIfConfigured(String keyDesc) {

        Configs JO = configsUtil.getConfig();
        return JO.getUserConfigured();
    }


    /**
     * Aggiunge una nuova attività al sistema, chiedendo di selezionare un luogo esistente
     * e quindi inserire i dettagli dell'attività.
     * Se il luogo selezionato non ha già un'attività associata, l'attività viene aggiunta.
     *
     * @pre Deve esserci almeno un luogo configurato senza attività. Il luogo selezionato deve esistere.
     */
    public void addActivity() {

        MonthlyConfig monthlyConfig = monthlyConfigService.getMonthlyConfig();

        if(checkIfConfigured(CONFIG_ACTIVITY_KEY_DESC)){
                if(!monthlyConfig.getPlanStateMap().get(PlanState.MODIFICHE_APERTE)){
                ioService.writeMessage("\n\nFase di modifica non disponibile, piano corrente non ancora generato\n\n", false);
                return;
            }
        }
            

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

            ConfigService cf = new ConfigService(socket, locInfoFactory, configType, dataLayer);
            cf.showPlaces();
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
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        if (dataLayer.getAll(locInfo) == null) {
            return true;
        }

        return false;
    }



    /**
     * metodo epr modificare il fatto che si sta iniziando a modificare dati
     * @param isBeingConfigured
     */
    private void setIsBeingConfigured(PlanState isBeingConfigured, Boolean value) {
        MonthlyConfig mc = monthlyConfigService.getMonthlyConfig();
        Map<PlanState, Boolean> stateMap = mc.getPlanStateMap();
        stateMap.put(isBeingConfigured, value);
        mc.setPlanStateMap(stateMap);
       
        monthlyConfigService.saveMonthlyConfig(mc);

    }

        /**
     * Modifica il numero massimo di iscrizioni contemporanee per una iniziativa.
     *
     * @pre Il sistema deve essere correttamente configurato e l’utente deve inserire un numero valido tra 1 e 50.
     * @post Il nuovo numero massimo è salvato nella configurazione.
     */
    public void modNumMaxSub(){
        ioService.writeMessage(CLEAR,false);
        int n = ioService.readIntegerWithMinMax("\nInserire nuovo numero di iscrizioni massime (massimo numero 50)",1,50);

        assert n >= 1 && n <= 50 : "Valore non valido per il numero massimo di iscrizioni";

        Configs configs = getConfig();

        assert configs != null : "Configurazione non trovata";

        configs.setMaxSubscriptions(n);
        JsonObject newConfigsJO = jsonFactoryService.createJson(configs);

        JsonDataLocalizationInformation locInfo = locInfoFactory.getConfigLocInfo();
        locInfo.setKey(configType.getValue());
        
        boolean modified = dataLayer.modify(newConfigsJO, locInfo);

        assert modified : "Modifica configurazione fallita";

        ioService.writeMessage("\nNumero massimo di iscrizioni modificato", false);
    }

    private Configs getConfig(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getConfigLocInfo();

        locInfo.setKey(configType.getValue());

        JsonObject cJO = dataLayer.get(locInfo);
        return jsonFactoryService.createObject(cJO, Configs.class);
    }

    /**
     * metodo per modificare una attivita
     */
    public void modActivity(){

        Activity activity = getActivity();//viene scelta l'attività da modificare

        if (activity == null) {
            ioService.writeMessage("\nAttività non trovata, impossibile modificare", false);
            return;
        }

        //necessito il vecchio titolo per effettuare le modifiche
        String oldTitle = activity.getTitle(); 
        


        String title = ioService.readString("\nTitolo attuale: " + activity.getTitle() + "\nInserisci nuovo titolo (lascia vuoto per mantenere):");
        if (!title.isBlank()) {
            activity.setTitle(title);
        }

        String description = ioService.readString("\nDescrizione attuale: " + activity.getDescription() + "\nInserisci nuova descrizione (lascia vuoto per mantenere):");
        if (!description.isBlank()) {
            activity.setDescription(description);
        }

        boolean changeMeetingPoint = ioService.readBoolean("\nPunto di ritrovo attuale: " + activity.getMeetingPoint() +"\nVuoi modificare il punto di ritrovo? (true/false)");
        if (changeMeetingPoint) {
            Place place = getPlace(activity.getPlaceName());
            if (place == null) {
                ioService.writeMessage("\nLuogo non trovato, impossibile modificare il punto di ritrovo", false);
                return;
            }
            Address newMeetingPoint = activityUtil.getMeetingPoint(place); 
            activity.setMeetingPoint(newMeetingPoint);
        }

        LocalDate firstDate = getOptionalDate("\nData inizio attuale: " + activity.getFirstProgrammableDate() + "\nNuova data inizio (dd-mm-yyyy) o lascia vuoto:");
        if (firstDate != null) activity.setFirstProgrammableDate(firstDate);

        LocalDate lastDate = getOptionalDate("\nData fine attuale: " + activity.getLastProgrammableDate() + "\nNuova data fine (dd-mm-yyyy) o lascia vuoto:");
        if (lastDate != null) activity.setLastProgrammableDate(lastDate);

        boolean changeDays = ioService.readBoolean("\nVuoi modificare i giorni programmabili? (true/false)");
        if (changeDays) {
            String[] newDays = activityUtil.insertDays();
            activity.setProgrammableDays(newDays);
        }

        LocalTime newHour = getOptionalTime("\nOrario attuale: " + activity.getProgrammableHour() + "\nNuovo orario (HH:mm) o lascia vuoto:");
        if (newHour != null) activity.setProgrammableHour(newHour);

        LocalTime newDuration = getOptionalTime("\nDurata attuale: " + activity.getDurationAsLocalTime() + "\nNuova durata (HH:mm) o lascia vuoto:");
        if (newDuration != null) activity.setDurationAsLocalTime(newDuration);

        boolean changeTicket = ioService.readBoolean("\nVuoi modificare il requisito del biglietto? (true/false)");
        if (changeTicket) {
            boolean newTicket = ioService.readBoolean("È necessario il biglietto? (true/false)");
            activity.setBigliettoNecessario(newTicket);
        }

        int newMax = ioService.readIntegerWithMinMax("\nNumero massimo attuale: " + activity.getMaxPartecipanti() + "\nNuovo massimo (o stesso numero):", 1, 1000);
        int newMin = ioService.readIntegerWithMinMax("\nNumero minimo attuale: " + activity.getMinPartecipanti() + "\nNuovo minimo (o stesso numero):", 1, newMax);
        activity.setMaxPartecipanti(newMax);
        activity.setMinPartecipanti(newMin);

        saveActivity(activity, oldTitle);
    }

    /**
     * metodo helper modifica attività,
     * metodo per salvare l'attività modificata
     * @param activity
     * @param oldTitle
     */
    private void saveActivity(Activity activity, String oldTitle) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedActivitiesLocInfo();
        locInfo.setKey(oldTitle);
        
        JsonObject activityJO = jsonFactoryService.createJson(activity);
        boolean modified = dataLayer.modify(activityJO, locInfo);
        
        if (modified) {
            ioService.writeMessage("\nAttività modificata con successo", false);
        } else {
            ioService.writeMessage("\nErrore nella modifica dell'attività", false);
        }
    }


    /**
     * metodo helper modifica attività
     * metodo per ottenere il luogo
     * @param placeName
     * @return
     */
    private Place getPlace(String placeName) {
       JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();
        locInfo.setKey(placeName);
    
        while (true) {
            locInfo.setKey(placeName);
            JsonObject placeJO = dataLayer.get(locInfo);

            if (placeJO != null) {
                return jsonFactoryService.createObject(placeJO, Place.class);
            }

            ioService.writeMessage("\nLuogo non trovato con il nome: " + placeName, false);

        
            boolean retry = ioService.readBoolean("Vuoi riprovare con un altro nome? (true/false)");
            if (!retry) {
                return null;
            }

            placeName = ioService.readString("Inserisci un nuovo nome per il luogo:");
    }
    }


    /**
     * metodo helper del metodo modifica attività,
     * per ottenere l'attività da modificare
     * @return
     */
    private Activity getActivity() {
        
        showChangableActivities();
        String activityTitle = ioService.readString("\nScegli l'attività da modificare (inserisci il titolo):");

        Activity chosenActivity = localizeActivity(activityTitle);
        return chosenActivity;
    }

    /**
     * metodo helper del metodo getActivity,
     * per ottenere l'attività da modificare
     * @return
     */
    private void showChangableActivities() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedActivitiesLocInfo();

        List<JsonObject> activitiesJO = dataLayer.getAll(locInfo);
        List<Activity> activities = jsonFactoryService.createObjectList(activitiesJO, Activity.class);

        ioService.writeMessage(formatter.formatListActivity(activities), false);
        ioService.writeMessage(SPACE,false);
    }


    /**
     * metodo helepr del metodo modifica attività,
     * per ottenere l'attività dato il titolo
     * @param activityTitle
     * @return
     */
    private Activity localizeActivity(String activityTitle) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedActivitiesLocInfo();
        locInfo.setKey(activityTitle);
        JsonObject activityJO = dataLayer.get(locInfo);
        
        if (activityJO == null) {
            ioService.writeMessage("\nAttività non trovata con il titolo: " + activityTitle, false);
            return null;
        }
        
        return jsonFactoryService.createObject(activityJO, Activity.class);
    }


    /**
     * metodo helper per modifica attività, ottiene una data
     */
    private LocalDate getOptionalDate(String prompt) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    while (true) {
        String input = ioService.readString(prompt);
        if (input.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(input, formatter);
        } catch (DateTimeParseException e) {
            ioService.writeMessage("\nData non valida. Assicurati di usare il formato dd-MM-yyyy (es. 01-04-2025)", false);
        }
    }
}


    /**
     * metodo helper modifica luogo, ottiene un orario
     */
    private LocalTime getOptionalTime(String prompt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        while (true) {
            String input = ioService.readString(prompt);
            if (input.isBlank()) {
                return null;
            }
            try {
                return LocalTime.parse(input, formatter);
            } catch (DateTimeParseException e) {
                ioService.writeMessage("\nOra non valida. Usa il formato HH:mm (es. 09:00)", false);
            }
        }
    }


    /**
     * metodo per modificare un luogo
     */
    public void modPlace() {

        Place place = getPlace();//viene scelto il luogo da modificare

        if (place == null) {
            ioService.writeMessage("\nLuogo non trovato, impossibile modificare", false);
            return;
        }

        //necessito il vecchio titolo per effettuare le modifiche
        String oldName = place.getName(); 

        String newName = ioService.readString("\nNome attuale: " + place.getName() + "\nInserisci nuovo nome (lascia vuoto per mantenere):");
        if (!newName.isBlank()) {
            place.setName(newName);
        }


        String newDesc = ioService.readString("\nDescrizione attuale: " + place.getDescription() + "\nInserisci nuova descrizione (lascia vuoto per mantenere):");
        if (!newDesc.isBlank()) {
            place.setDescription(newDesc);
        }

        boolean changeAddress = ioService.readBoolean("\nVuoi modificare l'indirizzo? (true/false)");
        if (changeAddress) {
            Address currentAddress = place.getAddress();
            String street = ioService.readString("Via attuale: " + currentAddress.getStreet() + "\nNuova via (lascia vuoto per mantenere):");
            String city = ioService.readString("Città attuale: " + currentAddress.getCity() + "\nNuova città (lascia vuoto per mantenere):");
            String state = ioService.readString("Stato attuale: " + currentAddress.getState() + "\nNuovo stato (lascia vuoto per mantenere):");
            String zip = "";
            while (true) {
                zip = ioService.readString("CAP attuale: " + currentAddress.getZipCode() + "\nNuovo CAP (lascia vuoto per mantenere):");
                if (zip.isBlank()) {
                    break; // lascia invariato
                }
                if (zip.matches("\\d+")) {
                    break; // valido, tutto numerico
                } else {
                    ioService.writeMessage("Il CAP deve contenere solo cifre numeriche.", false);
                }
            }

            if (!street.isBlank()) currentAddress.setStreet(street);
            if (!city.isBlank()) currentAddress.setCity(city);
            if (!state.isBlank()) currentAddress.setState(state);
            if (!zip.isBlank()) currentAddress.setZipCode(zip);
        }

        savePlace(place, oldName);
    }

    /**
     * metodo helper del metodo modifca luogo, 
     * metodo per salvare il luogo modificato
     * @param place
     * @param oldTitle
     */
    private void savePlace(Place place, String oldName) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();
        locInfo.setKey(oldName);
        
        JsonObject placeJO = jsonFactoryService.createJson(place);
        boolean modified = dataLayer.modify(placeJO, locInfo);
        
        if (modified) {
            ioService.writeMessage("\nLuogo modificata con successo", false);
        } else {
            ioService.writeMessage("\nErrore nella modifica del luogo", false);
        }
    }

    /**
     * metodo helper modifica luogo,
     * metodo per ottenere il luogo da modificare
     * @return
     */
    private Place getPlace() {
        showChangeblePlaces();
        String placeName = ioService.readString("\nScegli il luogo da modificare (inserisci il nome):");

        Place place = localizePlace(placeName);
        return place;
    }

    /**
     * metodo helper di getPlace,
     * metodo per ottenere il luogo da modificare
     * @return
     */
    private void showChangeblePlaces() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();

        List<JsonObject> placesJO = dataLayer.getAll(locInfo);
        List<Place> places = jsonFactoryService.createObjectList(placesJO, Place.class);

        ioService.writeMessage(formatter.formatListPlace(places), false);
        ioService.writeMessage(SPACE,false);
    }


    private Place localizePlace(String placeName) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();
        locInfo.setKey(placeName);
        JsonObject placeJO = dataLayer.get(locInfo);
        
        if (placeJO == null) {
            ioService.writeMessage("\nPlace non trovata con il titolo: " + placeName, false);
            return null;
        }
        
        return jsonFactoryService.createObject(placeJO, Place.class);
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

        MonthlyConfig monthlyConfig = monthlyConfigService.getMonthlyConfig();

        if(!monthlyConfig.getPlanStateMap().get(PlanState.MODIFICHE_APERTE)){
            ioService.writeMessage("\n\nFase di modifica non disponibile, piano corrente non ancora generato\n\n", false);
            return;
        }
    

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

            volUtil.deactivateVolunteer(name);
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

        MonthlyConfig monthlyConfig = monthlyConfigService.getMonthlyConfig();

        if(!monthlyConfig.getPlanStateMap().get(PlanState.MODIFICHE_APERTE)){
            ioService.writeMessage("\n\nFase di modifica non disponibile, piano corrente non ancora generato\n\n", false);
            return;
        }

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

        MonthlyConfig monthlyConfig = monthlyConfigService.getMonthlyConfig();

        if(!monthlyConfig.getPlanStateMap().get(PlanState.MODIFICHE_APERTE)){
            ioService.writeMessage("\n\nFase di modifica non disponibile, piano corrente non ancora generato\n\n", false);
            return;
        }

        
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
}
