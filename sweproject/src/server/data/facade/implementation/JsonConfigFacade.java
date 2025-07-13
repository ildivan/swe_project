package server.data.facade.implementation;

import com.google.gson.JsonObject;
import server.data.facade.interfaces.IConfigFacade;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.data.json.datalayer.datareadwrite.IJsonReadWrite;
import server.jsonfactoryservice.JsonFactoryService;
import server.utils.ConfigType;
import server.utils.Configs;

public class JsonConfigFacade implements IConfigFacade{

    private JsonDataLayer dataLayer;
    private final IJsonLocInfoFactory locInfoFactory;
    private final JsonFactoryService jsonFactoryService = new JsonFactoryService();

    public JsonConfigFacade(IJsonReadWrite readWrite, 
                            IJsonLocInfoFactory locInfoFactory) {
        this.dataLayer = new JsonDataLayer(readWrite);
        this.locInfoFactory = locInfoFactory;
    }


    /**
     * method to initialize configs.json
     */
    @Override
    public void initializeConfig() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getConfigLocInfo();
        Configs configs = new Configs();

        dataLayer.add(jsonFactoryService.createJson(configs), locInfo);
    }

    /**
     * metodo per ottenere i config
     * @return
     */
    @Override
    public Configs getConfig(ConfigType configType){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getConfigLocInfo();

        locInfo.setKey(configType.getValue());

        JsonObject cJO = dataLayer.get(locInfo);
        return jsonFactoryService.createObject(cJO, Configs.class);
    }

    /**
     * metodo per salvare i config
     * @param configs
     */
    @Override
    public boolean save(Configs configs, ConfigType configType) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getConfigLocInfo();
    
        locInfo.setKey(configType.getValue());

        JsonObject JO = jsonFactoryService.createJson(configs);

        return dataLayer.modify(JO, locInfo);
            
    }

    /**
     * verifico se è il primo piano mensile generato
     * @return
     */
    public boolean checkIfFirstMonthlyPlan(ConfigType configType) {
        Configs configs = getConfig(configType);

        if(!configs.getFirstPlanConfigured()){
            return true;
        }

        return false;
    }

    @Override
    public void firstTimeConfigurationServerConfig(){

        JsonDataLocalizationInformation locInfo = locInfoFactory.getConfigLocInfo();

        if(!dataLayer.checkFileExistance(locInfo)){
            dataLayer.createJSONEmptyFile(locInfo);
        }

        JsonObject JO = jsonFactoryService.createJson(new Configs());
        
        locInfo.setKey(ConfigType.NORMAL.getValue());
    
        dataLayer.modify(JO, locInfo);
    }
    
}
