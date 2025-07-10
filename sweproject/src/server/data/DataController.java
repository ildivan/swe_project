package server.data;

import server.data.facade.FacadeAbstractFactory;
import server.data.facade.PlacesFacade;

public class DataController {
    private final PlacesFacade placesFacade;

    public DataController(FacadeAbstractFactory facadeFactory) {
        this.placesFacade = facadeFactory.createPlacesFacade();
    }
    
    public PlacesFacade getPlacesFacade() {
        return placesFacade;
    }
}
