package backend.server.domainlevel.domainservices;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import backend.server.Configs;
import backend.server.domainlevel.Place;
import backend.server.domainlevel.User;
import backend.server.domainlevel.VolunteerData;
import backend.server.domainlevel.domainmanagers.PlacesManager;
import backend.server.domainlevel.domainmanagers.VolunteerManager;
import backend.server.domainlevel.Activity;
import backend.server.domainlevel.Address;
import backend.server.domainlevel.Manager;
import backend.server.genericservices.Service;
import backend.server.genericservices.DataLayer.DataLayer;
import backend.server.genericservices.DataLayer.JSONDataContainer;
import backend.server.genericservices.DataLayer.JSONDataManager;
import backend.server.genericservices.DataLayer.JSONUtil;

public class ConfigService extends Service<Void>{
   // private static final String GONFIG_MENU = "\n1) Inserire nuovo volotario\n2) Inserire nuovo luogo\n3) Mostra volontari\n4) Mostra luoghi";
    private static final String QUESTION = "\n\nInserire scelta: ";
    private final Map<String, Boolean> vociVisibili = new LinkedHashMap<>();
    private final Map<String, Runnable> chiamateMetodi = new LinkedHashMap<>();
    private DataLayer dataLayer = new JSONDataManager();
    private Manager placesManager = new PlacesManager();
    private Manager volunteerManager = new VolunteerManager();
  

    public ConfigService(Socket socket, Gson gson){
        super(socket);
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

    public Void applyLogic() throws IOException {
        
        boolean continuare = true;
        do{
            //ANDR GESTITO IL TEMPO IN QUELCHE MODO QUA
            if(!checkIfUserConfigured()){
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

    //todo fare una classe menu che mi gestisce il JSON del menu
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

    private String menuToString(List<String> menu) {
        String menuOut = "";
        for (int i = 0; i < menu.size(); i++) {
            menuOut += ((i + 1) + ") " + menu.get(i)+"\n");
        }
        return menuOut;
    }


    private boolean continueChoice(String message) {
        write(String.format("Proseguire con %s? (s/n)", message),true);
        String choice = "";
        try {
            choice = read();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(choice.equals("n")){
            return false;
        }
        return true;
    }


    private boolean checkIfUserConfigured() {
       // write("primo metodo", false);
        JsonObject JO = new JsonObject();
        JO = dataLayer.get(new JSONDataContainer("JF/configs.json", "configs", "normalFunctionConfigs", "configType"));
        // if(JO.isEmpty()){
        //    // return false;
        // }
        //write(String.format("%b",JO.get("userConfigured").getAsBoolean() ), false);
        return JO.get("userConfigured").getAsBoolean();
    }

    private boolean checkIfPlacesConfigured() {
    
        JsonObject JO = new JsonObject();
        JO = dataLayer.get(new JSONDataContainer("JF/configs.json", "configs", "false", "placesFirtsConfigured"));
         if(JO==null){
           // write("true", false);
           return true;
         }
        return false;
    }

    private boolean checkIfActivityConfigured() {
    
        JsonObject JO = new JsonObject();
        JO = dataLayer.get(new JSONDataContainer("JF/configs.json", "configs", "false", "activitiesFirtsConfigured"));
         if(JO==null){
           // write("true", false);
           return true;
         }
        return false;
    }

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

   
       if(!checkIfPlacesConfigured()){
            write("Inizio prima configurazione luoghi", false);
            addPlace();
            configs.setPlacesFirtsConfigured(true);
       }
       //forse devo inglobare anche l'attiità boh io farei unalrtra var nei configs che me lo dice se sono gia configurate
        if(!checkIfActivityConfigured()){
            write("Inizio prima configurazione attività", false);
            addActivity();
            configs.setActivitiesFirtsConfigured(true);
        }

        String StringJO = new String();
        StringJO = gson.toJson(configs);
        JsonObject JO = gson.fromJson(StringJO, JsonObject.class);

        JSONDataContainer dataContainer = new JSONDataContainer("JF/configs.json", JO, "configs","normalFunctionConfigs", "configType");
        dataLayer.modify(dataContainer);

        return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
    }

    private String configureArea() throws IOException{
        write("Inserire luogo di esercizio", true);
        String areaOfIntrest = null;
        areaOfIntrest = read();
        return areaOfIntrest;
    }
    
    private Integer configureMaxSubscriptions() throws IOException{
        write("Inserire numero massimo di iscrizioni contemporanee ad una iniziativa", true);
        Integer maxSubscriptions = Integer.parseInt(read());
        return maxSubscriptions;
    }


    private void modNumMaxSub(){
        write("\nInserire nuovo numero di iscrizioni massime",true);
        Integer n = 0;
        try {
            n = Integer.parseInt(read());
        } catch (NumberFormatException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JsonObject oldConfigsJO = dataLayer.get(new JSONDataContainer("JF/configs.json", "configs", "normalFunctionConfigs", "configType"));
        Configs configs = gson.fromJson(oldConfigsJO, Configs.class);
        configs.setMaxSubscriptions(n);
        JsonObject newConfigsJO = JSONUtil.createJson(configs);

        dataLayer.modify(new JSONDataContainer("JF/configs.json", newConfigsJO, "configs","normalFunctionConfigs", "configType"));


    }

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

    private boolean checkIfVolunteersExist(String name) {
        if(volunteerManager.exists(name)){
            return true;
        }
        return false;
    }

    private void addVolunteerWithName(String name) {
        VolunteerData volunteer = new VolunteerData(name);
        dataLayer.add(new JSONDataContainer("JF/volunteers.json", JSONUtil.createJson(volunteer), "volunteers"));
        addNewVolunteerUserProfile(name);
    }

    private void addNewVolunteerUserProfile(String name) {
        String tempPass = "temp" + Math.random();
        User u = new User(name, tempPass, "volontario");
        dataLayer.add(new JSONDataContainer("JF/users.json", JSONUtil.createJson(u), "users"));
    }

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

    private void addActivity() {
        if(placesManager.checkIfThereIsSomethingWithCondition()){
            write("\nSono presenti luoghi senza attività, inserire almeno una attività per ogniuno", false);
            addActivityOnNoConfiguredPlaces();
        }

        //altrimenti ciclo while che chiede se vuoli continuare ad inserire attivita o no (scegli il luogo e chiami addactivitywithplace)
    }

    private void addActivityOnNoConfiguredPlaces() {

        write("enter",false);
       List<Place> places = (List<Place>) placesManager.getCustomList();
         for (Place place : places) {
              write("Inserire attività per il luogo:\n " + place.toString(), false);
              addActivityWithPlace(place);
        }
    }
              
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

    private Address getMeetingPoint(Place p) throws IOException {
        write("\nInserire punto di ritrovo (indirizzo): (d-indirizzo luogo/altro inserire)", true);
        if(read().equals("d")){
            return p.getAddress();
        }else{
            return addNewAddress();
        }
    }
              
    private void showVolunteers() {
        write("showVol",false);
    }

    private void showPlaces() {
        write("showPla",false);
    }
    
}
