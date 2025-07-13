package server.data.facade;

import server.data.facade.interfaces.IActivitiesFacade;
import server.data.facade.interfaces.IFacadeAbstractFactory;
import server.data.facade.interfaces.IPlacesFacade;
import server.data.facade.interfaces.IUsersFacade;
import server.data.facade.interfaces.IVolunteersFacade;

public class FacadeHub {
    private final IPlacesFacade placesFacade;
    private final IUsersFacade userFacade;
    private final IActivitiesFacade activityFacade;
    private final IVolunteersFacade volunteersFacade;

    public FacadeHub(IFacadeAbstractFactory facadeFactory) {
        this.placesFacade = facadeFactory.createPlacesFacade();
        this.userFacade = facadeFactory.createUsersFacade();
        this.activityFacade = facadeFactory.createActivitiesFacade();
        this.volunteersFacade = facadeFactory.createVolunteersFacade();
    }
    
    public IPlacesFacade getPlacesFacade() {
        return placesFacade;
    }

    public IUsersFacade getUsersFacade() {
        return userFacade;
    }

    public IActivitiesFacade getActivitiesFacade() {
        return activityFacade;
    }

    public IVolunteersFacade getVolunteersFacade() {
        return  volunteersFacade;
    }
}
