package server.data.json;

import server.data.json.datalayer.datalocalizationinformations.NormalFunctionJsonLocInfoFactory;

public class NormalFunctionJsonFacadeAbstractFactory extends JsonFacadeAbstractFactory {

    public NormalFunctionJsonFacadeAbstractFactory() {
        super(new NormalFunctionJsonLocInfoFactory());
    }
}
