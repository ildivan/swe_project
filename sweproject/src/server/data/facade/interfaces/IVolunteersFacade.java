package server.data.facade.interfaces;

import java.util.List;

import server.firstleveldomainservices.volunteerservice.Volunteer;

public interface IVolunteersFacade {
    public boolean addVolunteer(String name);
    
    public void saveVolunteer(String name, Volunteer volunteer);
    
    public Volunteer getVolunteer(String name);
    
    public boolean doesVolunteerExist(String name);
    
    public List<Volunteer> getVolunteers();
    
    public boolean deleteVolunteer(String name);
}