package server.data.facade;

public interface IFacadeAbstractFactory {
    public IPlacesFacade createPlacesFacade();

    public IUserFacade createUserFacade();

    public IActivitiesFacade createActivitiesFacade();
}
