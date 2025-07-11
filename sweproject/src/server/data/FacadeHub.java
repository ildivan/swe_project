package server.data;

import server.data.facade.IActivitiesFacade;
import server.data.facade.IFacadeAbstractFactory;
import server.data.facade.IPlacesFacade;
import server.data.facade.IUserFacade;

public class FacadeHub {
    private final IPlacesFacade placesFacade;
    private final IUserFacade userFacade;
    private final IActivitiesFacade activityFacade;

    public FacadeHub(IFacadeAbstractFactory facadeFactory) {
        this.placesFacade = facadeFactory.createPlacesFacade();
        this.userFacade = facadeFactory.createUserFacade();
        this.activityFacade = facadeFactory.createActivitiesFacade();
    }
    
    public IPlacesFacade getPlacesFacade() {
        return placesFacade;
    }

    public IUserFacade getUserFacade() {
        return userFacade;
    }

    public IActivitiesFacade getActivityFacade() {
        return activityFacade;
    }
}
