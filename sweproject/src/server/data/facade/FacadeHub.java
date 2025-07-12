package server.data.facade;

import server.data.facade.interfaces.IActivitiesFacade;
import server.data.facade.interfaces.IFacadeAbstractFactory;
import server.data.facade.interfaces.IPlacesFacade;
import server.data.facade.interfaces.IUserFacade;

public class FacadeHub {
    private final IPlacesFacade placesFacade;
    private final IUserFacade userFacade;
    private final IActivitiesFacade activityFacade;

    public FacadeHub(IFacadeAbstractFactory facadeFactory) {
        this.placesFacade = facadeFactory.createPlacesFacade();
        //this.userFacade = facadeFactory.createUserFacade();
        this.userFacade = null;
        this.activityFacade = facadeFactory.createActivitiesFacade();
    }
    
    public IPlacesFacade getPlacesFacade() {
        return placesFacade;
    }

    public IUserFacade getUserFacade() {
        return userFacade;
    }

    public IActivitiesFacade getActivitiesFacade() {
        return activityFacade;
    }
}
