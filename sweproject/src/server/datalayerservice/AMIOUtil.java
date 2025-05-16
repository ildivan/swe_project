package server.datalayerservice;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import com.google.gson.Gson;

import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Manager;
import server.firstleveldomainservices.Place;
import server.GsonFactoryService;
import server.IOService;


public class AMIOUtil extends IOService {
    private static final Gson gson = GsonFactoryService.getGson();
    private static Manager volunteerManager = new VolunteerManager(gson);

    public static Address getAddress(){
        String street = readString("Inserire via");
        String city = readString("Inserire città");
        String nation = readString("Inserire nazione");
        String zipCode = readString("Inserire CAP");
        while(!isNumeric(zipCode)){
            write("CAP non valido, inserire solo numeri", false);
            zipCode = readString("Inserire CAP");
        }
        return new Address(street, city, nation, zipCode);
    }

    public static Activity getActivity(Place place){
            String title = readString("\nInserire titolo attività");
            String description = readString("\nInserire descrizione attività");
            Address meetingPoint = getMeetingPoint(place);
            LocalDate firstProgrammableDate = getDate("\nInserire data inizio attività (dd-mm-yyyy)");
            LocalDate lastProgrammableDate = getDate("\nInserire data fine attività (dd-mm-yyyy)");
            String[] programmableDays = readString("\nInserire giorni della settimana programmabili separati da una virgola").split(",");
            LocalTime programmableHour = getTime("\nInserire ora programmabile (HH:mm)");
            LocalTime duration = getTime("\nInserire durata attività (HH:mm)");
            boolean bigliettoNecessario = readBoolean("\nInserire se è necessatio il biglietto: (true/false)");
            int maxPartecipanti = readInteger("\nInserire numero massimo partecipanti",1, 1000);
            int minPartecipanti = readInteger("\nInserire numero minimo partecipanti",1,maxPartecipanti);
            
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
                String input = readString(message);
                time = LocalTime.parse(input, formatterTime);
                validTime = true;
            } catch (DateTimeParseException e) {
                write("Orario non valido! Assicurati di usare il formato HH:mm (es. 14:30)", false);
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
                    String input = readString(message);
                    date = LocalDate.parse(input, formatterDate);
                    validDate = true;
                } catch (DateTimeParseException e) {
                    write("Data non valida! Assicurati di usare il formato dd-MM-yyyy", false);
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
        if(readString("\nInserire punto di ritrovo (indirizzo): (d-indirizzo luogo/altro inserire)").equals("d")){
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
            write(volunteerManager.getAll(),false);
            String vol = IOService.readString("\nInserire volontario da aggiungere all'attività");
            if(checkIfVolunteersExist(vol)){
               volunteers.add(vol);
            }else{
                write("Volontario non esistente, aggiungere un volontario corretto", false);
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

    private static boolean isNumeric(String str) {
        return str != null && str.matches("\\d+");
    }
}
