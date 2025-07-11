package server.data.facade.interfaces;

public interface IFacadeAbstractFactory {
    public IPlacesFacade createPlacesFacade();

    public IUserFacade createUserFacade();

    public IActivitiesFacade createActivitiesFacade();
}
