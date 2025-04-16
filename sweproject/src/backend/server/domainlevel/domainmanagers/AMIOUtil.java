package backend.server.domainlevel.domainmanagers;

import java.io.IOException;
import java.util.ArrayList;

import backend.server.domainlevel.Activity;
import backend.server.domainlevel.Address;
import backend.server.domainlevel.Manager;
import backend.server.domainlevel.Place;
import backend.server.domainlevel.Volunteer;
import backend.server.genericservices.IOUtil;
import backend.server.genericservices.datalayer.DataLayer;
import backend.server.genericservices.datalayer.JSONDataContainer;
import backend.server.genericservices.datalayer.JSONDataManager;
import backend.server.genericservices.datalayer.JSONUtil;

public class AMIOUtil extends IOUtil {

    private static Manager volunteerManager = new VolunteerManager();
    private static DataLayer dataLayer = new JSONDataManager();

    public static Address getAddress(){
        String street = readString("Inserire via");
        String city = readString("Inserire città");
        String nation = readString("Inserire nazione");
        String zipCode = readString("Inserire CAP");
        return new Address(street, city, nation, zipCode);
    }

    public static Activity getActivity(Place place){
            String title = readString("\nInserire titolo attività");
            String description = readString("\nInserire descrizione attività");
            Address meetingPoint = getMeetingPoint(place);
            //DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            //LocalDate firstProgrammableDate = LocalDate.parse(read(), formatterDate);
            String firstProgrammableDate = readString("\nInserire data inizio attività (dd-mm-yyyy)");
            //LocalDate lastProgrammableDate = LocalDate.parse(read(), formatterDate);
            String lastProgrammableDate = readString("\nInserire data fine attività (dd-mm-yyyy)");
            String[] programmableDays = readString("\nInserire giorni della settimana programmabili separati da una virgola").split(",");
            //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            //LocalTime programmableHour = LocalTime.parse(read(), formatter);
            String programmableHour = readString("\nInserire ora programmabile (HH:mm)");
            //LocalTime duration = LocalTime.parse(read(), formatter);
            String duration = readString("\nInserire durata attività (HH:mm)");
            boolean bigliettoNecessario = readBoolean("\nInserire se è necessatio il biglietto: (true/false)");
            int maxPartecipanti = readInteger("\nInserire numero massimo partecipanti");
            int minPartecipanti = readInteger("\nInserire numero minimo partecipanti");
            
            return new Activity(place.getName(), title, description, meetingPoint, firstProgrammableDate, lastProgrammableDate, programmableDays, programmableHour, duration, bigliettoNecessario, maxPartecipanti, minPartecipanti, null);
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
            String vol = IOUtil.readString("\nInserire volontario da aggiungere all'attività");
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
}
