package server.data.facade;

import server.data.facade.interfaces.IActivitiesFacade;
import server.data.facade.interfaces.IFacadeAbstractFactory;
import server.data.facade.interfaces.IPlacesFacade;
import server.data.facade.interfaces.IUsersFacade;

public class FacadeHub {
    private final IPlacesFacade placesFacade;
    private final IUsersFacade userFacade;
    private final IActivitiesFacade activityFacade;

    public FacadeHub(IFacadeAbstractFactory facadeFactory) {
        this.placesFacade = facadeFactory.createPlacesFacade();
        this.userFacade = facadeFactory.createUsersFacade();
        this.activityFacade = facadeFactory.createActivitiesFacade();
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
}
