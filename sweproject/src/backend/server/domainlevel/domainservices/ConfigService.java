package backend.server.domainlevel.domainservices;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import backend.server.Configs;
import backend.server.domainlevel.*;
import backend.server.domainlevel.domainmanagers.*;
import backend.server.genericservices.Service;
import backend.server.genericservices.DataLayer.*;

public class ConfigService extends Service<Void>{
   // private static final String GONFIG_MENU = "\n1) Inserire nuovo volotario\n2) Inserire nuovo luogo\n3) Mostra volontari\n4) Mostra luoghi";
    private static final String QUESTION = "\n\nInserire scelta: ";
    private static final String USER_KEY_DESC = "userConfigured";
    private static final String PLACE_KEY_DESC = "placesFirtsConfigured";
    private static final String ACTIVITY_KEY_DESC = "activitiesFirtsConfigured";

    private final Map<String, Boolean> vociVisibili = new LinkedHashMap<>();
    private final Map<String, Runnable> chiamateMetodi = new LinkedHashMap<>();
    private DataLayer dataLayer = new JSONDataManager();
    private Manager placesManager = new PlacesManager();
    private Manager volunteerManager = new VolunteerManager();
    private String configType;
  

    public ConfigService(Socket socket, Gson gson, String configType) {
        super(socket);
        this.configType = configType;
        //TODO avere un json che contiene le varie chiamate hai metodi e se sono visibili o no
        vociVisibili.put("Modifica numero massimo di persone iscrivibili mediante una singola iscrizione", true);
        vociVisibili.put("Aggiungi Volontario", true);
        vociVisibili.put("Aggiungi Luogo", true);
        vociVisibili.put("Mostra Volontari", true);
        vociVisibili.put("Mostra Luoghi", true);
        
        chiamateMetodi.put("Modifica numero massimo di persone iscrivibili mediante una singola iscrizione", this::modNumMaxSub);
        chiamateMetodi.put("Aggiungi Volontario", this::addVolunteer);
        chiamateMetodi.put("Aggiungi Luogo", this::addPlace);
        chiamateMetodi.put("Mostra Volontari", this::showVolunteers);
        chiamateMetodi.put("Mostra Luoghi", this::showPlaces);
    }
    /**
     * apply the logic of the service
     * @return null
     * @throws IOException
     */
    public Void applyLogic() throws IOException {
        
        boolean continuare = true;
        do{
            //ANDR GESTITO IL TEMPO IN QUELCHE MODO QUA
            if(configType.equalsIgnoreCase("normalFunctionConfigs")){
                if(firstTimeConfiguration()){
                    write("Configurazione base completata", false);
                }else{
                    write("Errore durante la configurazione", false);
                    return null;
                }
            };

            startMenu();
            continuare = continueChoice("scelta operazioni");
        }while(continuare);
        write("\nArrivederci!\n", false);

        return null;
    }
    
    /**
     * start the menu keeping the user in a loop until he decides to exit
     * @throws IOException
     */
    private void startMenu() throws IOException {
        //da implemetare il controllo sull'inserimento dell'intero
        List<String> menu = buildMenu();
        String Smenu = menuToString(menu);
        boolean sceltaValida = true;
        
        do{
            write(Smenu + QUESTION, true);
            int choice = Integer.parseInt(read());
    
            if (choice >= 1 && choice <= menu.size()) {
                sceltaValida = true;
                String sceltaStringa = menu.get(choice - 1);
                chiamateMetodi.get(sceltaStringa).run();
                
            } else {
                sceltaValida = false;
                write("Scelta non valida, riprovare", false);
            }
        }while(!sceltaValida);
        
    }

    /**
     * build the menu based on the visibility of the options
     * @return
     */
    private List<String> buildMenu() {
        
        List<String> opzioniVisibili = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : vociVisibili.entrySet()) {
            if (entry.getValue()) {
                //   write(String.valueOf(entry.getValue()), false);
                opzioniVisibili.add(entry.getKey());
            }
        }

        return opzioniVisibili;
        
    }

    /**
     * convert the menu to a string
     * @param menu recive the list of the menu option to print on the terminal for the user
     * @return
     */
    private String menuToString(List<String> menu) {
        String menuOut = "";
        for (int i = 0; i < menu.size(); i++) {
            menuOut += ((i + 1) + ") " + menu.get(i)+"\n");
        }
        return menuOut;
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
     * check if the places are already configured - only the first configuration
     * @return true if the places are already configured - for the first configuration
     */
    // private boolean checkIfPlacesConfigured() {
    
    //     JsonObject JO = new JsonObject();
    //     JO = dataLayer.get(new JSONDataContainer("JF/configs.json", "configs", "false", "placesFirtsConfigured"));
    //      if(JO==null){
    //        return true;
    //      }
    //     return false;
    // }

    /**
     * check if the places are already configured - only the first configuration
     * @return true if the places are already configured - for the first configuration
     */
    // private boolean checkIfActivityConfigured() {
    
    //     JsonObject JO = new JsonObject();
    //     JO = dataLayer.get(new JSONDataContainer("JF/configs.json", "configs", "false", "activitiesFirtsConfigured"));
    //      if(JO==null){
    //        return true;
    //      }
    //     return false;
    // }

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
        write("Inserire luogo di esercizio", true);
        String areaOfIntrest = null;
        areaOfIntrest = read();
        return areaOfIntrest;
    }
    
    /**
     * method to configure the max number of subscriptions
     * @return the max number of subscriptions
     * @throws IOException
     */
    private Integer configureMaxSubscriptions() throws IOException{
        write("Inserire numero massimo di iscrizioni contemporanee ad una iniziativa", true);
        Integer maxSubscriptions = Integer.parseInt(read());
        return maxSubscriptions;
    }

    /**
     * method to modify the max number of subscriptions
     */
    private void modNumMaxSub(){
        write("\nInserire nuovo numero di iscrizioni massime",true);
        Integer n = 0;
        try {
            n = Integer.parseInt(read());
        } catch (NumberFormatException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JsonObject oldConfigsJO = dataLayer.get(new JSONDataContainer("JF/configs.json", "configs", configType, "configType"));
       // Configs configs = gson.fromJson(oldConfigsJO, Configs.class);
        Configs configs;
        configs = JSONUtil.createObject(oldConfigsJO, Configs.class);
        configs.setMaxSubscriptions(n);
        JsonObject newConfigsJO = JSONUtil.createJson(configs);

        dataLayer.modify(new JSONDataContainer("JF/configs.json", newConfigsJO, "configs",configType, "configType"));
    }

    /**
     * method to add a new volunteer, asks the name, then checks if it already exists, if not it adds it to the user and to the voluteer database
     * in the user database it creates a temporarly password to be changed by the volunteer the first time he logs in
     * ideally the configurator adds the volunteer and then he tells the volunteer the temporarly password generated
     */
    private void addVolunteer() {
        write("\nInserire nome del volontario", true);
        String name = "";
        try {
            name = read();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (!checkIfVolunteersExist(name)) {
            addVolunteerWithName(name);
            
        } else {
            write("Volontario già esistente", false);
        }
    }   

    /**
     * method to check if a volunteer already exists
     * @param name name of the volunteer to check
     * @return true if the volunteer already exists
     */
    private boolean checkIfVolunteersExist(String name) {
        if(volunteerManager.exists(name)){
            return true;
        }
        return false;
    }

    /**
     * method to add a volunteer to the database both volunteers and users calling the method to add the user profile
     * @param name
     */
    private void addVolunteerWithName(String name) {
        VolunteerData volunteer = new VolunteerData(name);
        dataLayer.add(new JSONDataContainer("JF/volunteers.json", JSONUtil.createJson(volunteer), "volunteers"));
        addNewVolunteerUserProfile(name);
    }

    /**
     * method to add a new user profile to user database creating a new random password
     * @param name
     */
    private void addNewVolunteerUserProfile(String name) {
        String tempPass = "temp_" + Math.random();
        write(String.format("Nova password temporanea per volontario: %s\n%s", name, tempPass), false);
        User u = new User(name, tempPass, "volontario");
        dataLayer.add(new JSONDataContainer("JF/users.json", JSONUtil.createJson(u), "users"));
    }

    /**
     * method to add a new place to the database
     */
    private void addPlace(){
        boolean continuare = false;
        
        do{
            try{
                write("Inserire nome luogo", true);
                String name = read();
                if(placesManager.exists(name)){
                    write("Luogo già esistente", false);
                    return;
                }
                write("Inserire descrizione luogo", true);
                String description = read();
                write("Inserire indirizzo luogo", false);
                Address address = addNewAddress();
                placesManager.add(JSONUtil.createJson(new Place(name, address, description)));
            }catch(IOException e){
                e.printStackTrace();
            }
            continuare = continueChoice("inserimento luoghi");
        }while(continuare);
    }

    /**
     * util method for the previous method to add a new address
     * @return the new address
     * @throws IOException
     */
    private Address addNewAddress() throws IOException {
        write("Inserire via", true);
        String street = read();
        write("Inserire città", true);
        String city = read();
        write("Inserire nazione", true);
        String nation = read();
        write("Inserire CAP", true);
        String zipCode = read();
        return new Address(street, city, nation, zipCode);
    }

    /**
     * method to add a new activity to the database
     */
    private void addActivity() {
        boolean jump = false;
        if(placesManager.checkIfThereIsSomethingWithCondition()){
            write("\nSono presenti luoghi senza attività, inserire almeno una attività per ogniuno", false);
            addActivityOnNoConfiguredPlaces();
            jump = true;
        }

        try{
            do{
                if(jump){
                    continue;
                }
                showPlaces();
                write("\nInserire luogo per l'attività", true);
                String placeName = read();

                while(!placesManager.exists(placeName)){
                        write("Luogo non esistente, riprovare", false);
                        write("\nInserire luogo per l'attività", true);
                        placeName = read();
                }
                    
                Place place = JSONUtil.createObject(placesManager.get(placeName), Place.class);      
                addActivityWithPlace(place);
            }while(continueChoice("aggiunta attività"));
        }catch(IOException e){
            e.printStackTrace();
        }
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
        Activity activity;
        try {
            write("\nInserire titolo attività", true);
            String title = read();
            write("\nInserire descrizione attività", true);
            String description = read();
            Address meetingPoint = getMeetingPoint(place);
            write("\nInserire data inizio attività (dd-mm-yyyy)", true);
            //DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            //LocalDate firstProgrammableDate = LocalDate.parse(read(), formatterDate);
            String firstProgrammableDate = read();
            write("\nInserire data fine attività (dd-mm-yyyy)", true);
            //LocalDate lastProgrammableDate = LocalDate.parse(read(), formatterDate);
            String lastProgrammableDate = read();
            write("\nInserire giorni della settimana programmabili separati da una virgola", true);
            String[] programmableDays = read().split(",");
            write("\nInserire ora programmabile (HH:mm)", true);
            //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            //LocalTime programmableHour = LocalTime.parse(read(), formatter);
            String programmableHour = read();
            write("\nInserire durata attività (HH:mm)", true);
            //LocalTime duration = LocalTime.parse(read(), formatter);
            String duration = read();
            write("\nInserire se è necessario un biglietto", true);
            boolean bigliettoNecessario = Boolean.parseBoolean(read());
            write("\nInserire numero massimo partecipanti", true);
            int maxPartecipanti = Integer.parseInt(read());
            write("\nInserire numero minimo partecipanti", true);
            int minPartecipanti = Integer.parseInt(read());
            String[] volunteers = addVolunteersToActivity();
            activity = new Activity(place.getName(), title, description, meetingPoint, firstProgrammableDate, lastProgrammableDate, programmableDays, programmableHour, duration, bigliettoNecessario, maxPartecipanti, minPartecipanti, volunteers);
            dataLayer.add(new JSONDataContainer("JF/activities.json", JSONUtil.createJson(activity), "activities"));
        } catch (IOException e) {
            e.printStackTrace();
        }       
      
    }

    /**
     * util method: add a list of volunteers to a specific activity that is being buildt in the previous method
     * @return
     * @throws IOException
     */
    private String[] addVolunteersToActivity() throws IOException {
        ArrayList<String> volunteers = new ArrayList<>();
        do{
            showVolunteers();
            write("\nInserire volontario da agggiungere all'attività", true);
            String vol = read();
            if(checkIfVolunteersExist(vol)){
               volunteers.add(vol);
            }else{
                write("Volontario non esistente, si vuole creare un nuovo volontario? (s/n)", true);
                if(read().equals("s")){
                    addVolunteerWithName(vol);
                }
            }
        }while(continueChoice("aggiunta volontari all'attività"));
        return volunteers.toArray(new String[volunteers.size()]);
        
    }
    
    /**
     * util method: make you choose the meeting point
     * @param p place where you are building the activity on
     * @return
     * @throws IOException
     */
    private Address getMeetingPoint(Place p) throws IOException {
        write("\nInserire punto di ritrovo (indirizzo): (d-indirizzo luogo/altro inserire)", true);
        if(read().equals("d")){
            return p.getAddress();
        }else{
            return addNewAddress();
        }
    }

    /**
     * method to show all volunteers
     * UNIMPLEMENTED
     */
    private void showVolunteers() {
        write("showVol",false);
    }

    /**
     * method to show all places
     * UNIMPLEMENTED
     */
    private void showPlaces() {
        write("showPla",false);
    }
    
}
