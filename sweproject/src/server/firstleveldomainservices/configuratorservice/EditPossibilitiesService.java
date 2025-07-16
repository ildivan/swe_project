package server.firstleveldomainservices.configuratorservice;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

import com.google.gson.JsonObject;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Place;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuService;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.menus.EditMenu;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.PlanState;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;
import server.utils.ActivityUtil;
import server.utils.ConfigType;
import server.utils.Configs;
import server.utils.MainService;

public class EditPossibilitiesService extends MainService<Void>{

    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";


    private final MenuService menu = new EditMenu(this);

    private final ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory;    
    private final IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private final IInputOutput ioService = new IOService();
    private final IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();
    private final MonthlyConfigService monthlyConfigService;
    private final ActivityUtil activityUtil;
    private final ConfigType configType;

    public EditPossibilitiesService(Socket socket, ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory, ConfigType configType) {
        super(socket);

        this.configType = configType;
        this.locInfoFactory = locInfoFactory;
        this.monthlyConfigService = new MonthlyConfigService(locInfoFactory);
        this.activityUtil = new ActivityUtil(locInfoFactory, configType);
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
        JsonDataLocalizationInformation locInfo = locInfoFactory.getActivityLocInfo();
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
       JsonDataLocalizationInformation locInfo = locInfoFactory.getPlaceLocInfo();
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
        ConfigService configService = new ConfigService(socket, locInfoFactory, configType);
        configService.showActivities();
        String activityTitle = ioService.readString("\nScegli l'attività da modificare (inserisci il titolo):");

        Activity chosenActivity = localizeActivity(activityTitle);
        return chosenActivity;
    }

    /**
     * metodo helepr del metodo modifica attività,
     * per ottenere l'attività dato il titolo
     * @param activityTitle
     * @return
     */
    private Activity localizeActivity(String activityTitle) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getActivityLocInfo();
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
        JsonDataLocalizationInformation locInfo = locInfoFactory.getPlaceLocInfo();
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
        ConfigService configService = new ConfigService(socket,locInfoFactory, configType);
        configService.showPlaces();
        String placeName = ioService.readString("\nScegli il luogo da modificare (inserisci il nome):");

        Place place = localizePlace(placeName);
        return place;
    }

    private Place localizePlace(String placeName) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getPlaceLocInfo();
        locInfo.setKey(placeName);
        JsonObject placeJO = dataLayer.get(locInfo);
        
        if (placeJO == null) {
            ioService.writeMessage("\nPlace non trovata con il titolo: " + placeName, false);
            return null;
        }
        
        return jsonFactoryService.createObject(placeJO, Place.class);
    }


    
}
