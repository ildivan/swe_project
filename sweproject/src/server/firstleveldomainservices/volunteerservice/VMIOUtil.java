package server.firstleveldomainservices.volunteerservice;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import server.authservice.User;
import server.data.facade.FacadeHub;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;


public class VMIOUtil{
    private static final String ROLE = "volontario";
    private final FacadeHub data;

    public VMIOUtil(FacadeHub data) {
        this.data = data;
    }


    /**
     * method to add a volunteer
     * @param name
     * @return
     */
    public void addVolunteer(String name){
        assert name != null && !name.trim().isEmpty() : "Nome volontario non valido";
        boolean added = data.getVolunteersFacade().addVolunteer(name);
        assert added;
        addNewVolunteerUserProfile(name);
    }

     /**
     * method to add a new user profile to user database creating a new random password
     * @param name
     */
    private void addNewVolunteerUserProfile(String name) {
        IInputOutput ioService = new IOService();
        String tempPass = "temp_" + Math.random();
        ioService.writeMessage(String.format("Nuova password temporanea per volontario: %s\n%s", name, tempPass), false);
        User u = new User(name, tempPass, ROLE, false);

        data.getUsersFacade().addUser(u);
    }

    

    /**
     * method used to deactivate volunteer
     * @param name
     */
    public void deactivateVolunteer(String name) {

       User user = data.getUsersFacade().getUser(name);
       assert user != null : "User not found";

       user.setActive(false);
       user.setIsDeleted(true);

       boolean modified = data.getUsersFacade().modifyUser(
            user.getName(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(false),
            Optional.of(true)
        );
        assert modified;
    }

    /**
     * method to add disponibility date
     * @param firstPlanConfigured
     * @param name
     */
    public void addDisponibilityDate(boolean firstPlanConfigured, String name, String formattedDate){
        Volunteer volunteer = data.getVolunteersFacade().getVolunteer(name);
        assert volunteer != null : "Volontario non trovato";
        Set<String> disponibilityDays;

        disponibilityDays = volunteer.getDisponibilityDaysCurrent();

        if(disponibilityDays == null){
            disponibilityDays = new LinkedHashSet<String>();
        }
        
        disponibilityDays.add(formattedDate);

        volunteer.setDisponibilityDaysCurrent(disponibilityDays);

        data.getVolunteersFacade().saveVolunteer(name, volunteer);

    }
}
