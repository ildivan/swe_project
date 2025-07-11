package server.utils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import server.data.facade.FacadeHub;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Place;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityRecord;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlanService;
import server.firstleveldomainservices.volunteerservice.VMIOUtil;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;


public class ActivityUtil{
   
    private final IJsonLocInfoFactory locInfoFactory;
    private final MonthlyPlanService monthlyPlanService;
    private final JsonDataLayer dataLayer;
    private final VMIOUtil volUtil;
    private final FacadeHub data;

    public ActivityUtil(IJsonLocInfoFactory locInfoFactory, ConfigType configType,
    JsonDataLayer dataLayer, FacadeHub data) {
        this.locInfoFactory = locInfoFactory;
        this.dataLayer = dataLayer;
        this.monthlyPlanService = new MonthlyPlanService(locInfoFactory, configType, dataLayer, data);
        this.volUtil = new VMIOUtil(locInfoFactory, dataLayer);
        this.data = data;
    }
    public Address getAddress(){
        IInputOutput ioService = getIOService();
        String street = ioService.readString("Inserire via");
        String city = ioService.readString("Inserire città");
        String nation = ioService.readString("Inserire nazione");
        String zipCode = ioService.readString("Inserire CAP");
        while(!isNumeric(zipCode)){
            ioService.writeMessage("CAP non valido, inserire solo numeri", false);
            zipCode = ioService.readString("Inserire CAP");
        }
        return new Address(street, city, nation, zipCode);
    }

    public Activity addActivity(Place place){
        IInputOutput ioService = getIOService();
        String title = ioService.readString("\nInserire titolo attività");
        String description = ioService.readString("\nInserire descrizione attività");
        Address meetingPoint = getMeetingPoint(place);
        LocalDate firstProgrammableDate = getDate("\nInserire data inizio attività (dd-mm-yyyy)");
        LocalDate lastProgrammableDate = getDate("\nInserire data fine attività (dd-mm-yyyy)");
        String[] programmableDays = insertDays();
        LocalTime programmableHour = getTime("\nInserire ora programmabile (HH:mm)");
        LocalTime duration = getTime("\nInserire durata attività (HH:mm)");
        boolean bigliettoNecessario = ioService.readBoolean("\nInserire se è necessatio il biglietto: (true/false)");
        int maxPartecipanti = ioService.readIntegerWithMinMax("\nInserire numero massimo partecipanti",1, 1000);
        int minPartecipanti = ioService.readIntegerWithMinMax("\nInserire numero minimo partecipanti",1,maxPartecipanti);
        String[] volunteers = choseVolunteers();
            
        return data.getActivitiesFacade().addActivity(
            place,
            title,
            description,
            meetingPoint,
            firstProgrammableDate,
            lastProgrammableDate,
            programmableDays,
            programmableHour,
            duration,
            bigliettoNecessario,
            maxPartecipanti,
            minPartecipanti,
            volunteers
        );
    }

    private String[] choseVolunteers() {
        IInputOutput ioService = getIOService();
        boolean finished = true;
        List<String> out = new ArrayList<>();
        do{

            String name = choseVolunteer();
            out.add(name);
            String continuare = ioService.readString("Inserire altro volontario? (y si altro no)");
            if(!continuare.equalsIgnoreCase("y")){
                finished=false;
            }
        }while(finished);
        return out.toArray(new String[0]);
    }
    
    
    private String choseVolunteer() {
       IInputOutput ioService = getIOService();
       boolean finished = false;
       String name;
       do{
            name = ioService.readString("Inserire volontario nell'attività");
            if(!volunteerExist(name)){
                ioService.writeMessage("Volontario inesistente, riprovare",false);
            }else{
                finished = true;
            }
        }while(!finished);

        return name;
    }


    private boolean volunteerExist(String name) {
       return volUtil.checkVolunteerExistance(name);
    }

    
    /**
     * inserire tempo nel formato hh:mm
     * @param message
     * @return
     */
    public LocalTime getTime(String message) {
        IInputOutput ioService = getIOService();
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime time = null;
        boolean validTime = false;
    
        while (!validTime) {
            try {
                String input = ioService.readString(message);
                time = LocalTime.parse(input, formatterTime);
                validTime = true;
            } catch (DateTimeParseException e) {
                ioService.writeMessage("Orario non valido! Assicurati di usare il formato HH:mm (es. 14:30)", false);
            }
        }
        return time;
    }
    

    /**
     * permette di inserire una data nel formato dd-mm-yyyy
     * @param message
     * @return
     */
    public LocalDate getDate(String message){
        IInputOutput ioService = getIOService();
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = null;
            boolean validDate = false;

            while (!validDate) {
                try {
                    String input = ioService.readString(message);
                    date = LocalDate.parse(input, formatterDate);
                    validDate = true;
                } catch (DateTimeParseException e) {
                    ioService.writeMessage("Data non valida! Assicurati di usare il formato dd-MM-yyyy", false);
                }
            }
            return date;
    }

    /**
     * util method: make you choose the meeting point
     * @param p place where you are building the activity on
     * @return
     * @throws IOException
     */
    public Address getMeetingPoint(Place p){
        IInputOutput ioService = getIOService();
        if((ioService.readString("\nInserire punto di ritrovo (indirizzo): ('d' per indirizzo luogo vuoto per inserire)")).equals("d")){
            return p.getAddress();
        }else{
            return getAddress();
        }
    }

     /**
     * util method: add a list of volunteers to a specific activity that is being buildt in the previous method
     * @return
     * @throws IOException
     */
    public String[] addVolunteersToActivity(){
        IInputOutput ioService = getIOService();
        ArrayList<String> volunteers = new ArrayList<>();
      
        do{
            String vol = ioService.readString("\nInserire volontario da aggiungere all'attività");
            if(checkIfVolunteersExist(vol)){
               volunteers.add(vol);
            }else{
                ioService.writeMessage("Volontario non esistente, aggiungere un volontario corretto", false);
                continue;
            }
        }while(continueChoice("aggiunta volontari all'attività"));
        return volunteers.toArray(new String[volunteers.size()]);
        
    }
    
    /**
     * method to check if a volunteer already exists
     * @param name name of the volunteer to check
     * @return true if the volunteer already exists
     */
    private boolean checkIfVolunteersExist(String name) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();

        locInfo.setKey(name);
    
        return dataLayer.exists(locInfo);
      
    }

    /**
     * method to check if a string is numeric
     * @return
     */
    private boolean isNumeric(String str) {
        return str != null && str.matches("\\d+");
    }

    /**
     * method to read a day from the user, until he stops
     * @param message
     * @return
     */
    public String[] insertDays() {
        IInputOutput ioService = getIOService();
        
        List<String> days = new ArrayList<>();

        boolean cont = true;
        while (cont) {
          
            String day = ioService.readString("Inserisci un giorno della settimana in cui la visita si può programmare (es Lunedi): ");
            day = day.replace('ì', 'i');
            days.add(day);
            String answer = ioService.readString("Vuoi inserire un altro giorno? (s/n): ");
            if (!answer.equalsIgnoreCase("s")) {
                cont = false;
            }
        }
        return days.toArray(new String[0]);
    }

    /**
     * ask the user if he wants to continue with the operation
     * @param message the operation the user wants to continue
     * @return
     */
    protected boolean continueChoice(String message) {
        IInputOutput ioService = getIOService();
        String choice = ioService.readString(String.format("\nProseguire con %s? (s/n)", message));
       
        if(choice.equals("n")){
            return false;
        }
        return true;
    }

    private IInputOutput getIOService(){
        return new IOService();
    }

    public List<ActivityRecord> getActivitiesByState(ActivityState desiredState){

        MonthlyPlan monthlyPlan = monthlyPlanService.getMonthlyPlan();

        return data.getActivitiesFacade().getActivitiesByState(desiredState, monthlyPlan);
    }
}
