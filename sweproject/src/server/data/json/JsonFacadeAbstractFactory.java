package server.data.json;

import server.data.facade.IFacadeAbstractFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonLocInfoFactory;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;

public class JsonFacadeAbstractFactory implements IFacadeAbstractFactory{

    @Override
    public JsonPlacesFacade createPlacesFacade() {
        JsonReadWrite readWrite = new JsonReadWrite();
        JsonLocInfoFactory locInfoFactory = new JsonLocInfoFactory();
        return new JsonPlacesFacade(readWrite, locInfoFactory);
    }

    @Override
    public JsonUserFacade createUserFacade() {
        // To be implemented
    }

    @Override
    public JsonActivitiesFacade createActivitiesFacade() {
        // To be implemented
    }
}
