package server.data.facade.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import server.data.Volunteer;

public interface IVolunteersFacade {
    public boolean addVolunteer(String name);
    
    public boolean modifyVolunteer(
        String name, 
        Optional<String> newName,
        Optional<Set<String>> disponibilityDaysCurrent,
        Optional<Set<String>> disponibilityDaysOld);
    
    public Volunteer getVolunteer(String name);
    
    public boolean doesVolunteerExist(String name);
    
    public List<Volunteer> getVolunteers();
    
    public boolean deleteVolunteer(String name);
}