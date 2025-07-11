package server.data.facade.implementation;

import server.data.json.datalayer.datalocalizationinformations.TestJsonLocInfoFactory;

public class TestFacadeFactory extends JsonFacadeAbstractFactory{

    public TestFacadeFactory() {
        super(new TestJsonLocInfoFactory());
      
    }

    
}
