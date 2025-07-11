package server.data.facade.implementation;

import server.data.json.datalayer.datalocalizationinformations.TestJsonLocInfoFactory;

public class TestJsonFacadeFactory extends JsonFacadeAbstractFactory{

    public TestJsonFacadeFactory() {
        super(new TestJsonLocInfoFactory());
      
    }

    
}
