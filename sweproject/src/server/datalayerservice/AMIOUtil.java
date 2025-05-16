package server.datalayerservice;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Place;
import server.ioservice.IOService;
import server.ioservice.ReadWrite;
import server.GsonFactoryService;


public class AMIOUtil{
    private static final Gson gson = GsonFactoryService.getGson();
    private static Manager volunteerManager = new VolunteerManager(gson);

    public static Address getAddress(){
        String street = (String) IOService.Service.READ_STRING.start("Inserire via");
        String city = (String) IOService.Service.READ_STRING.start("Inserire città");
        String nation = (String) IOService.Service.READ_STRING.start("Inserire nazione");
        String zipCode = (String) IOService.Service.READ_STRING.start("Inserire CAP");
        while(!isNumeric(zipCode)){
            IOService.Service.WRITE.start("CAP non valido, inserire solo numeri", false);
            zipCode = (String) IOService.Service.READ_STRING.start("Inserire CAP");
        }
        return new Address(street, city, nation, zipCode);
    }

    public static Activity getActivity(Place place){
            String title = (String) IOService.Service.READ_STRING.start("\nInserire titolo attività");
            String description = (String) IOService.Service.READ_STRING.start("\nInserire descrizione attività");
            Address meetingPoint = getMeetingPoint(place);
            LocalDate firstProgrammableDate = getDate("\nInserire data inizio attività (dd-mm-yyyy)");
            LocalDate lastProgrammableDate = getDate("\nInserire data fine attività (dd-mm-yyyy)");
            String[] programmableDays = insertDays();
            LocalTime programmableHour = getTime("\nInserire ora programmabile (HH:mm)");
            LocalTime duration = getTime("\nInserire durata attività (HH:mm)");
            boolean bigliettoNecessario = (Boolean) IOService.Service.READ_BOOLEAN.start("\nInserire se è necessatio il biglietto: (true/false)");
            int maxPartecipanti = (Integer) IOService.Service.READ_INTEGER_WITH_BOUNDARIES.start("\nInserire numero massimo partecipanti",1, 1000);
            int minPartecipanti = (Integer) IOService.Service.READ_INTEGER_WITH_BOUNDARIES.start("\nInserire numero minimo partecipanti",1,maxPartecipanti);
            
            return new Activity(place.getName(), title, description, meetingPoint, firstProgrammableDate, lastProgrammableDate, programmableDays, programmableHour, duration, bigliettoNecessario, maxPartecipanti, minPartecipanti, null);
    }

    /**
     * inserire tempo nel formato hh:mm
     * @param message
     * @return
     */
    private static LocalTime getTime(String message) {
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime time = null;
        boolean validTime = false;
    
        while (!validTime) {
            try {
                String input = (String) IOService.Service.READ_STRING.start(message);
                time = LocalTime.parse(input, formatterTime);
                validTime = true;
            } catch (DateTimeParseException e) {
                IOService.Service.WRITE.start("Orario non valido! Assicurati di usare il formato HH:mm (es. 14:30)", false);
            }
        }
        return time;
    }
    

    /**
     * permette di inserire una data nel formato dd-mm-yyyy
     * @param message
     * @return
     */
    private static LocalDate getDate(String message){
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = null;
            boolean validDate = false;

            while (!validDate) {
                try {
                    String input = (String) IOService.Service.READ_STRING.start(message);
                    date = LocalDate.parse(input, formatterDate);
                    validDate = true;
                } catch (DateTimeParseException e) {
                    IOService.Service.WRITE.start("Data non valida! Assicurati di usare il formato dd-MM-yyyy", false);
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
    private static Address getMeetingPoint(Place p){
        if(((String) IOService.Service.READ_STRING.start("\nInserire punto di ritrovo (indirizzo): (d-indirizzo luogo/altro inserire)")).equals("d")){
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
    public static String[] addVolunteersToActivity(){
        ArrayList<String> volunteers = new ArrayList<>();
        do{
            IOService.Service.WRITE.start(volunteerManager.getAll(),false);
            String vol = (String) IOService.Service.READ_STRING.start("\nInserire volontario da aggiungere all'attività");
            if(checkIfVolunteersExist(vol)){
               volunteers.add(vol);
            }else{
                IOService.Service.WRITE.start("Volontario non esistente, aggiungere un volontario corretto", false);
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
    private static boolean checkIfVolunteersExist(String name) {
        if(volunteerManager.exists(name)){
            return true;
        }
        return false;
    }

    /**
     * method to check if a string is numeric
     * @return
     */
    private static boolean isNumeric(String str) {
        return str != null && str.matches("\\d+");
    }

    /**
     * method to read a day from the user, until he stops
     * @param message
     * @return
     */
    public static String[] insertDays() {
        
        List<String> days = new ArrayList<>();

        boolean cont = true;
        while (cont) {
          
            String day = (String) IOService.Service.READ_STRING.start("Inserisci un giorno della settimana in cui la visita si può programmare (es Lunedi): ");
            days.add(day);
            String answer = (String) IOService.Service.READ_STRING.start("Vuoi inserire un altro giorno? (s/n): ");
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
    protected static boolean continueChoice(String message) {
        String choice = (String) IOService.Service.READ_STRING.start(String.format("\nProseguire con %s? (s/n)", message));
       
        if(choice.equals("n")){
            return false;
        }
        return true;
    }

}
