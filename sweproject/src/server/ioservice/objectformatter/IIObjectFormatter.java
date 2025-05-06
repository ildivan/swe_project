package server.ioservice.objectformatter;

import java.util.List;

import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.Place;
import server.firstleveldomainservices.volunteerservice.Volunteer;

public interface IIObjectFormatter <T> {
    public T formatActivity(Activity a);
    public T formatListActivity(List<Activity> aList);
    public T formatListVolunteer(List<Volunteer> vList);
    public T formatVolunteer(Volunteer v);
    public T formatListPlace(List<Place> pList);
    public T formatPlace(Place p);
}
