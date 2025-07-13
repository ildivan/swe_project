package server.data.facade.interfaces;
public interface IFacadeAbstractFactory {

    public IPlacesFacade createPlacesFacade();

    public IUsersFacade createUsersFacade();

    public IVolunteersFacade createVolunteersFacade();

    public IActivitiesFacade createActivitiesFacade();

    public IConfigFacade createConfigsFacade();

    public IMonthlyConfigFacade createMonthlyConfigsFacade();

    public IMonthlyPlanFacade createMonthlyPlanFacade();

    public ISubscriptionFacade createSubscriptionFacade();

    public IPrecludeDateFacade createPrecludeDateFacade();
}
