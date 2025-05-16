package server.firstleveldomainservices.configuratorservice;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import server.datalayerservice.*;
import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Place;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.ConfiguratorMenu;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlanService;
import server.firstleveldomainservices.volunteerservice.Volunteer;
import server.ioservice.IOService;
import server.objects.Configs;
import server.objects.Service;


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

    private MenuService menu = new ConfiguratorMenu(this);
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
                    IOService.Service.WRITE.start("Configurazione base completata", false);
                }else{
                    IOService.Service.WRITE.start("Errore durante la configurazione", false);
                    return null;
                }
            };

        do{
            IOService.Service.WRITE.start(CLEAR,false);
            Runnable toRun = menu.startMenu();
            IOService.Service.WRITE.start(SPACE, false);
            if(toRun==null){
                continuare = false;
            }else{
                toRun.run();
                continuare = continueChoice("scelta operazioni");
            }
                
        }while(continuare);
        IOService.Service.WRITE.start("\nArrivederci!\n", false);

        return null;
    }
   
    /**
     * check if place and max number of subscriptions are configured -> firtst things to configure
     * @return true if the user is already configured
     */
    private boolean checkIfConfigured(String keyDesc) {
        JsonObject JO = new JsonObject();
        JO = dataLayer.get(new DataContainer("JF/configs.json", "configs", configType, "configType"));
        return JO.get(keyDesc).getAsBoolean();
    }

    /**
     * starts the first configuration of areaofintrest maxsubscriptions, places and activities related
     * @return true if ended correctly
     */
    private boolean firstTimeConfiguration(){
        try {
            IOService.Service.WRITE.start("Prima configurazione necessaria:", false);
            String areaOfIntrest = configureArea();
            Integer maxSubscriptions = configureMaxSubscriptions();
    
            //richiesta la configurazone dei luoghi e delle attivita
            //in base a come vanno le due cose sopra, modifico i config in un metodo a parte
            Configs configs = new Configs();
            configs.setUserConfigured(true);
            configs.setAreaOfIntrest(areaOfIntrest);
            configs.setMaxSubscriptions(maxSubscriptions);
    
       
            if(!checkIfConfigured(PLACE_KEY_DESC)){
                IOService.Service.WRITE.start("Inizio prima configurazione luoghi", false);
                addPlace();
                configs.setPlacesFirtsConfigured(true);
            }
                //forse devo inglobare anche l'attiità boh io farei unalrtra var nei configs che me lo dice se sono gia configurate
            if(!checkIfConfigured(ACTIVITY_KEY_DESC)){
                IOService.Service.WRITE.start("Inizio prima configurazione attività", false);
                addActivity();
                configs.setActivitiesFirtsConfigured(true);
            }
            
            String StringJO = new String();
            StringJO = gson.toJson(configs);
            JsonObject JO = gson.fromJson(StringJO, JsonObject.class);
            
            DataContainer dataContainer = new DataContainer("JF/configs.json", JO, "configs", "normalFunctionConfigs", "configType");
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
        return (String) IOService.Service.READ_STRING.start("Inserire luogo di esercizio");
      
    }
    
    /**
     * method to configure the max number of subscriptions
     * @return the max number of subscriptions
     * @throws IOException
     */
    private Integer configureMaxSubscriptions() throws IOException{
        return (Integer) IOService.Service.READ_INTEGER_WITH_BOUNDARIES.start("Inserire numero massimo di iscrizioni contemporanee ad una iniziativa", 1, 50);
    }

    /**
     * method to modify the max number of subscriptions
     */
    public void modNumMaxSub(){
        IOService.Service.WRITE.start(CLEAR,false);
        Integer n = (Integer) IOService.Service.READ_INTEGER_WITH_BOUNDARIES.start("\nInserire nuovo numero di iscrizioni massime (massimo numero 50)",1,50);
        Configs configs = JSONUtil.createObject(configManager.get(configType), Configs.class);
        configs.setMaxSubscriptions(n);
        JsonObject newConfigsJO = JSONUtil.createJson(configs);
        configManager.update(newConfigsJO, configType);
        IOService.Service.WRITE.start("\nNumero massimo di iscrizioni modificato", false);
    }

    /**
     * method to add a new volunteer, asks the name, then checks if it already exists, if not it adds it to the user and to the voluteer database
     * in the user database it creates a temporarly password to be changed by the volunteer the first time he logs in
     * ideally the configurator adds the volunteer and then he tells the volunteer the temporarly password generated
     */
    public void addVolunteer() {
        IOService.Service.WRITE.start(CLEAR,false);
        String name = (String) IOService.Service.READ_STRING.start("\nInserire nome del volontario");
        if (!volunteerManager.exists(name)) {
            volunteerManager.add(JSONUtil.createJson(new Volunteer(name)));
        } else {
            IOService.Service.WRITE.start("\nVolontario già esistente", false);
        }
    }   

    /**
     * method to add a new place to the database
     */
    public void addPlace(){
        IOService.Service.WRITE.start(CLEAR,false);
        boolean continuare = false;
        
        do{
                String name = (String) IOService.Service.READ_STRING.start("Inserire nome luogo");
                if(placesManager.exists(name)){
                    IOService.Service.WRITE.start("Luogo già esistente", false);
                    return;
                }
                String description = (String) IOService.Service.READ_STRING.start("Inserire descrizione luogo");
                IOService.Service.WRITE.start("Inserire indirizzo luogo", false);
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
        IOService.Service.WRITE.start(CLEAR,false);
        boolean jump = false;
        if(placesManager.checkIfThereIsSomethingWithCondition()){
            IOService.Service.WRITE.start("\nSono presenti luoghi senza attività, inserire almeno una attività per ogniuno", false);
            addActivityOnNoConfiguredPlaces();
            jump = true;
        }
            do{
                if(jump){
                    continue;
                }
                showPlaces();
                String placeName = (String) IOService.Service.READ_STRING.start("\nInserire luogo per l'attività");

                while(!placesManager.exists(placeName)){
                    IOService.Service.WRITE.start("Luogo non esistente, riprovare", false);
                        placeName = (String) IOService.Service.READ_STRING.start("\nInserire luogo per l'attività");

                }
                    
                Place place = JSONUtil.createObject(placesManager.get(placeName), Place.class);      
                addActivityWithPlace(place);
            }while(continueChoice("aggiunta attività"));

    }

    /**
     * show places where there is no activity related
     */
    private void addActivityOnNoConfiguredPlaces() {

        IOService.Service.WRITE.start("enter",false);
        List<Place> places = (List<Place>) placesManager.getCustomList();
        for (Place place : places) {
            IOService.Service.WRITE.start("Inserire attività per il luogo:\n " + place.toString(), false);
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
        IOService.Service.WRITE.start(volunteerManager.getAll(), false);
        IOService.Service.WRITE.start(SPACE,false);
    }

    /**
     * method to show all places
     */
    public void showPlaces() {
        IOService.Service.WRITE.start(placesManager.getAll(), false);
        IOService.Service.WRITE.start(SPACE,false);
    }

    /**
     * method to show all activities
     */
    public void showActivities() {
        IOService.Service.WRITE.start(activityManager.getAll(), false);
        IOService.Service.WRITE.start(SPACE,false);
    }

    /**
     * method to generate a monthly plan
     * 
     */

    public void generateMonthlyPlan() {
        MonthlyPlanService.Service.BUILD_PLAN.start();
        IOService.Service.WRITE.start("Piano mensile generato", false);
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
        IOService.Service.WRITE.start(CLEAR,false);
        MonthlyConfig mc = JSONUtil.createObject(dataLayer.get(new DataContainer("JF/monthlyConfigs.json", "mc", "current","type")), MonthlyConfig.class);

        int maxNumDay = mc.getMonthAndYear().getMonth().length(mc.getMonthAndYear().isLeapYear());
        int minNumDay = 1;
            

        
        int day = (Integer) IOService.Service.READ_INTEGER_WITH_BOUNDARIES.start("Inserire giorno non disponibile", minNumDay, maxNumDay);

        int month = setMonthOnPrecludeDay(mc, day);
        int year = setYearOnPrecludeDay(mc, day);

        mc.getPrecludeDates().add(LocalDate.of(year, month, day));
        JsonObject newConfigsJO = JSONUtil.createJson(mc);
        dataLayer.modify(new DataContainer("JF/monthlyConfigs.json", newConfigsJO, "mc", "current", "type"));

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
