package server.data.json;

import server.data.facade.IFacadeAbstractFactory;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.NormalFunctionJsonLocInfoFactory;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;

public class NormalFunctionJsonFacadeAbstractFactory implements IFacadeAbstractFactory{

    @Override
    public JsonPlacesFacade createPlacesFacade() {
        JsonReadWrite readWrite = new JsonReadWrite();
        IJsonLocInfoFactory locInfoFactory = new NormalFunctionJsonLocInfoFactory();
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
