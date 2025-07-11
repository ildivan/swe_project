package server.data.facade.implementation;

import server.data.json.datalayer.datalocalizationinformations.NoFirstConfigJsonLocInfoFactory;

public class NoFirstConfigJsonFacadeFactory extends JsonFacadeAbstractFactory {

    public NoFirstConfigJsonFacadeFactory() {
        super(new NoFirstConfigJsonLocInfoFactory());
    }
}
