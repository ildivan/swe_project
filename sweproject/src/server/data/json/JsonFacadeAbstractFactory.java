package server.data.json;

import server.data.facade.FacadeAbstractFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonLocInfoFactory;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;

public class JsonFacadeAbstractFactory implements FacadeAbstractFactory{

    @Override
    public JsonPlacesFacade createPlacesFacade() {
        JsonReadWrite readWrite = new JsonReadWrite();
        JsonLocInfoFactory locInfoFactory = new JsonLocInfoFactory();
        return new JsonPlacesFacade(readWrite, locInfoFactory);
    }
}
