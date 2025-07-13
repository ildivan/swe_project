package server.data.facade.implementation;

import server.data.facade.interfaces.IFacadeAbstractFactory;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;

public abstract class JsonFacadeAbstractFactory implements IFacadeAbstractFactory {

    private final IJsonLocInfoFactory locInfoFactory;

    public JsonFacadeAbstractFactory(IJsonLocInfoFactory locInfoFactory) {
        this.locInfoFactory = locInfoFactory;
    }

    @Override
    public JsonPlacesFacade createPlacesFacade() {
        return new JsonPlacesFacade(new JsonReadWrite(), locInfoFactory);
    }
    
    @Override
    public  JsonActivitiesFacade createActivitiesFacade(){
        return new JsonActivitiesFacade(new JsonReadWrite(), locInfoFactory);
    }

    @Override
    public JsonUsersFacade createUsersFacade() {
        return new JsonUsersFacade(new JsonReadWrite(), locInfoFactory);
    }

    @Override
    public JsonVolunteersFacade createVolunteersFacade() {
        return new JsonVolunteersFacade(new JsonReadWrite(), locInfoFactory);
    }

    @Override
    public JsonConfigFacade createConfigsFacade() {
        return new JsonConfigFacade(new JsonReadWrite(), locInfoFactory);
    }

    @Override
    public JsonMonthlyConfigFacade createMonthlyConfigsFacade() {
        return new JsonMonthlyConfigFacade(new JsonReadWrite(), locInfoFactory);
    }

    @Override
    public JsonMonthlyPlanFacade createMonthlyPlanFacade() {
        return new JsonMonthlyPlanFacade(new JsonReadWrite(), locInfoFactory);
    }

    @Override
    public JsonSubscriptionFacade createSubscriptionFacade() {
        return new JsonSubscriptionFacade(new JsonReadWrite(), locInfoFactory);
    }

    @Override
    public JsonPrecludeDateFacade createPrecludeDateFacade() {
        return new JsonPrecludeDateFacade(new JsonReadWrite(), locInfoFactory);
    }
}
