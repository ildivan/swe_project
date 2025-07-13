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
        //to implement
        throw new UnsupportedOperationException("Unimplemented method 'getname'");
    }
}
