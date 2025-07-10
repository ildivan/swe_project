package server.data.json;

import server.data.json.datalayer.datalocalizationinformations.NoFirstConfigJsonLocInfoFactory;

public class NoFirstConfigJsonFacadeAbstractFactory extends JsonFacadeAbstractFactory {

    public NoFirstConfigJsonFacadeAbstractFactory() {
        super(new NoFirstConfigJsonLocInfoFactory());
    }
}
