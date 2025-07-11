package server.data.facade.implementation;

import server.data.json.datalayer.datalocalizationinformations.NormalFunctionJsonLocInfoFactory;

public class NormalFunctionJsonFacadeFactory extends JsonFacadeAbstractFactory {

    public NormalFunctionJsonFacadeFactory() {
        super(new NormalFunctionJsonLocInfoFactory());
    }
}
