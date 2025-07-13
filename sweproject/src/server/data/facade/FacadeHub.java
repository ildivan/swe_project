package server.data.facade;

import server.data.facade.interfaces.IActivitiesFacade;
import server.data.facade.interfaces.IConfigFacade;
import server.data.facade.interfaces.IFacadeAbstractFactory;
import server.data.facade.interfaces.IMonthlyConfigFacade;
import server.data.facade.interfaces.IMonthlyPlanFacade;
import server.data.facade.interfaces.IPlacesFacade;
import server.data.facade.interfaces.IPrecludeDateFacade;
import server.data.facade.interfaces.ISubscriptionFacade;
import server.data.facade.interfaces.IUsersFacade;
import server.data.facade.interfaces.IVolunteersFacade;

public class FacadeHub {
    private final IPlacesFacade placesFacade;
    private final IUsersFacade userFacade;
    private final IActivitiesFacade activityFacade;
    private final IVolunteersFacade volunteersFacade;
    private final IConfigFacade configFacade;
    private final IMonthlyConfigFacade monthlyConfigFacade;
    private final IMonthlyPlanFacade monthlyPlanFacade;
    private final ISubscriptionFacade subscriptionFacade;
    private final IPrecludeDateFacade precludeDateFacade;

    public FacadeHub(IFacadeAbstractFactory facadeFactory) {
        this.placesFacade = facadeFactory.createPlacesFacade();
        this.userFacade = facadeFactory.createUsersFacade();
        this.activityFacade = facadeFactory.createActivitiesFacade();
        this.volunteersFacade = facadeFactory.createVolunteersFacade();
        this.configFacade = facadeFactory.createConfigsFacade();
        this.monthlyConfigFacade = facadeFactory.createMonthlyConfigsFacade();
        this.monthlyPlanFacade = facadeFactory.createMonthlyPlanFacade();
        this.subscriptionFacade = facadeFactory.createSubscriptionFacade();
        this.precludeDateFacade = facadeFactory.createPrecludeDateFacade();
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

    public IConfigFacade getConfigFacade() {
        return  configFacade;
    }

    public IMonthlyConfigFacade getMonthlyConfigFacade() {
        return  monthlyConfigFacade;
    }

    public IMonthlyPlanFacade getMonthlyPlanFacade() {
        return  monthlyPlanFacade;
    }

    public ISubscriptionFacade getSubscriptionFacade() {
        return  subscriptionFacade;
    }

    public IPrecludeDateFacade getPrecludeDateFacade(){
        return precludeDateFacade;
    }
}
