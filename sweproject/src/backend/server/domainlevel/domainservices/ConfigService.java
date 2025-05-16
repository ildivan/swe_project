package backend.server.domainlevel.domainservices;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import backend.server.Configs;
import backend.server.domainlevel.*;
import backend.server.domainlevel.domainmanagers.*;
import backend.server.domainlevel.domainmanagers.menumanager.ConfiguratorMenu;
import backend.server.domainlevel.domainmanagers.menumanager.IMenuManager;
import backend.server.domainlevel.monthlydomain.MonthlyConfig;
import backend.server.genericservices.*;
import backend.server.genericservices.datalayer.*;
import backend.server.utils.IOUtil;

public class ConfigService extends Service<Void>{
   // private static final String GONFIG_MENU = "\n1) Inserire nuovo volotario\n2) Inserire nuovo luogo\n3) Mostra volontari\n4) Mostra luoghi";
    private static final String PLACE_KEY_DESC = "placesFirtsConfigured";
    private static final String ACTIVITY_KEY_DESC = "activitiesFirtsConfigured";
    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";
    private Gson gson;
    private DataLayer dataLayer; 
    private Manager placesManager; 
    private Manager volunteerManager; 
    private Manager activityManager; 
    private Manager configManager;
    private Manager monthlyManager;

    private IMenuManager menu = new ConfiguratorMenu(this);
    private String configType;
    
  

    public ConfigService(Socket socket, Gson gson, String configType) {
        super(socket);
        this.configType = configType;
        this.gson = gson;
        this.dataLayer = new JSONDataManager(gson);
        this.placesManager = new PlacesManager(gson);
        this.volunteerManager = new VolunteerManager(gson);
        this.activityManager = new ActivityManager(gson); 
        this.configManager = new ConfigManager(gson); 
        this.monthlyManager = new MonthlyPlanManager(gson);
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
                    write("Configurazione base completata", false);
                }else{
                    write("Errore durante la configurazione", false);
                    return null;
                }
            };

        do{
            write(CLEAR,false);
            Runnable toRun = menu.startMenu();
            write(SPACE, false);
            if(toRun==null){
                continuare = false;
            }else{
                toRun.run();
                continuare = continueChoice("scelta operazioni");
            }
                
        }while(continuare);
        write("\nArrivederci!\n", false);

        return null;
    }
   
    /**
     * check if place and max number of subscriptions are configured -> firtst things to configure
     * @return true if the user is already configured
     */
    private boolean checkIfConfigured(String keyDesc) {
        JsonObject JO = new JsonObject();
        JO = dataLayer.get(new JSONDataContainer("JF/configs.json", "configs", configType, "configType"));
        return JO.get(keyDesc).getAsBoolean();
    }

    /**
     * starts the first configuration of areaofintrest maxsubscriptions, places and activities related
     * @return true if ended correctly
     */
    private boolean firstTimeConfiguration(){
        try {
            write("Prima configurazione necessaria:", false);
            String areaOfIntrest = configureArea();
            Integer maxSubscriptions = configureMaxSubscriptions();
    
            //richiesta la configurazone dei luoghi e delle attivita
            //in base a come vanno le due cose sopra, modifico i config in un metodo a parte
            Configs configs = new Configs();
            configs.setUserConfigured(true);
            configs.setAreaOfIntrest(areaOfIntrest);
            configs.setMaxSubscriptions(maxSubscriptions);
    
       
            if(!checkIfConfigured(PLACE_KEY_DESC)){
                write("Inizio prima configurazione luoghi", false);
                addPlace();
                configs.setPlacesFirtsConfigured(true);
            }
                //forse devo inglobare anche l'attiità boh io farei unalrtra var nei configs che me lo dice se sono gia configurate
            if(!checkIfConfigured(ACTIVITY_KEY_DESC)){
                write("Inizio prima configurazione attività", false);
                addActivity();
                configs.setActivitiesFirtsConfigured(true);
            }
            
            String StringJO = new String();
            StringJO = gson.toJson(configs);
            JsonObject JO = gson.fromJson(StringJO, JsonObject.class);
            
            JSONDataContainer dataContainer = new JSONDataContainer("JF/configs.json", JO, "configs", "normalFunctionConfigs", "configType");
            dataLayer.modify(dataContainer);
            
            return true;
            

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
    }

    /**
     * method to configure the area of intrest
     * @return  the area of intrest
     * @throws IOException
     */
    private String configureArea() throws IOException{
        return IOUtil.readString("Inserire luogo di esercizio");
    }
    
    /**
     * method to configure the max number of subscriptions
     * @return the max number of subscriptions
     * @throws IOException
     */
    private Integer configureMaxSubscriptions() throws IOException{
        return IOUtil.readInteger("Inserire numero massimo di iscrizioni contemporanee ad una iniziativa");
    }

    /**
     * method to modify the max number of subscriptions
     */
    public void modNumMaxSub(){
        write(CLEAR,false);
        Integer n = IOUtil.readInteger("\nInserire nuovo numero di iscrizioni massime");
        Configs configs = JSONUtil.createObject(configManager.get(configType), Configs.class);
        configs.setMaxSubscriptions(n);
        JsonObject newConfigsJO = JSONUtil.createJson(configs);
        configManager.update(newConfigsJO, configType);
    }

    /**
     * method to add a new volunteer, asks the name, then checks if it already exists, if not it adds it to the user and to the voluteer database
     * in the user database it creates a temporarly password to be changed by the volunteer the first time he logs in
     * ideally the configurator adds the volunteer and then he tells the volunteer the temporarly password generated
     */
    public void addVolunteer() {
        write(CLEAR,false);
        String name = IOUtil.readString("\nInserire nome del volontario");
        if (!volunteerManager.exists(name)) {
            volunteerManager.add(JSONUtil.createJson(new Volunteer(name)));
        } else {
            write("\nVolontario già esistente", false);
        }
    }   

    /**
     * method to add a new place to the database
     */
    public void addPlace(){
        write(CLEAR,false);
        boolean continuare = false;
        
        do{
                String name = IOUtil.readString("Inserire nome luogo");
                if(placesManager.exists(name)){
                    write("Luogo già esistente", false);
                    return;
                }
                String description = IOUtil.readString("Inserire descrizione luogo");
                write("Inserire indirizzo luogo", false);
                Address address = addNewAddress();
                placesManager.add(JSONUtil.createJson(new Place(name, address, description)));
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
     * method to add a new activity to the database
     */
    public void addActivity() {
        write(CLEAR,false);
        boolean jump = false;
        if(placesManager.checkIfThereIsSomethingWithCondition()){
            write("\nSono presenti luoghi senza attività, inserire almeno una attività per ogniuno", false);
            addActivityOnNoConfiguredPlaces();
            jump = true;
        }
            do{
                if(jump){
                    continue;
                }
                showPlaces();
                String placeName = IOUtil.readString("\nInserire luogo per l'attività");

                while(!placesManager.exists(placeName)){
                        write("Luogo non esistente, riprovare", false);
                        placeName = IOUtil.readString("\nInserire luogo per l'attività");

                }
                    
                Place place = JSONUtil.createObject(placesManager.get(placeName), Place.class);      
                addActivityWithPlace(place);
            }while(continueChoice("aggiunta attività"));

    }

    /**
     * show places where there is no activity related
     */
    private void addActivityOnNoConfiguredPlaces() {

        write("enter",false);
        List<Place> places = (List<Place>) placesManager.getCustomList();
        for (Place place : places) {
              write("Inserire attività per il luogo:\n " + place.toString(), false);
              addActivityWithPlace(place);
        }
    }
              
    /**
     * creat an activity on the place passed in input in the method
     * @param place place to relate the activity
     */
    private void addActivityWithPlace(Place place) {
        activityManager.add(JSONUtil.createJson(place));
    }

    /**
     * method to show all volunteers
     */
    public void showVolunteers() {
        write(volunteerManager.getAll(), false);
        write(SPACE,false);
    }

    /**
     * method to show all places
     */
    public void showPlaces() {
        write(placesManager.getAll(), false);
        write(SPACE,false);
    }

    /**
     * method to show all activities
     */
    public void showActivities() {
        write(activityManager.getAll(), false);
        write(SPACE,false);
    }

    /**
     * method to generate a monthly plan
     * UNIMPLEMENTED
     */

    public void generateMonthlyPlan() {
        monthlyManager.add(new JsonObject());
        /*
         * da fara visualizzazione
         */
    }

    public void addNonUsableDate(){
        write(CLEAR,false);
        MonthlyConfig mc = JSONUtil.createObject(dataLayer.get(new JSONDataContainer("JF/monthlyConfigs.json", "mc", "current","type")), MonthlyConfig.class);

        int maxNumDay = mc.getMonthAndYear().getMonth().length(mc.getMonthAndYear().isLeapYear());
        int minNumDay = 1;
            

        
        int day = IOUtil.readInteger("Inserire giorno non disponibile", minNumDay, maxNumDay);

        int month = setMonthOnPrecludeDay(mc, day);
        int year = setYearOnPrecludeDay(mc, day);

        mc.getPrecludeDates().add(LocalDate.of(year, month, day));
        JsonObject newConfigsJO = JSONUtil.createJson(mc);
        dataLayer.modify(new JSONDataContainer("JF/monthlyConfigs.json", newConfigsJO, "mc", "current", "type"));

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
    
}
