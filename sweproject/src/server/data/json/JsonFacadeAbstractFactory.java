package server.data.json;

import server.data.facade.IFacadeAbstractFactory;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;

public abstract class JsonFacadeAbstractFactory implements IFacadeAbstractFactory {

    private final IJsonLocInfoFactory locInfoFactory;

    public JsonFacadeAbstractFactory(IJsonLocInfoFactory locInfoFactory) {
        this.locInfoFactory = locInfoFactory;
    }

    @Override
    public JsonPlacesFacade createPlacesFacade() {
        JsonReadWrite readWrite = new JsonReadWrite();
        return new JsonPlacesFacade(readWrite, locInfoFactory);
    }

    @Override
    public  JsonUserFacade createUserFacade() {
        //to implement
        throw new UnsupportedOperationException("Unimplemented method 'getname'");
    }

    @Override
    public  JsonActivitiesFacade createActivitiesFacade(){
        //to implement
        throw new UnsupportedOperationException("Unimplemented method 'getname'");
    }
}
