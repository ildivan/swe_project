package server.data.json;

import server.data.facade.FacadeAbstractFactory;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.NormalFunctionJsonLocInfoFactory;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;

public class NormalFunctionJsonFacadeAbstractFactory implements FacadeAbstractFactory{

    @Override
    public JsonPlacesFacade createPlacesFacade() {
        JsonReadWrite readWrite = new JsonReadWrite();
        IJsonLocInfoFactory locInfoFactory = new NormalFunctionJsonLocInfoFactory();
        return new JsonPlacesFacade(readWrite, locInfoFactory);
    }
}
