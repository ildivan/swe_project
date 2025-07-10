package server.utils;

import com.google.gson.JsonObject;

import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;

/**
 * da inserire nella facade del datalayer
 */
public class ConfigsUtil {

    private final ConfigType configType;
    private final IJsonLocInfoFactory locInfoFactory;
    private final IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private final JsonDataLayer dataLayer;

    
  

    public ConfigsUtil(IJsonLocInfoFactory locInfoFactory, ConfigType configType,
    JsonDataLayer dataLayer) {
        this.dataLayer = dataLayer;
        this.configType = configType;
        this.locInfoFactory = locInfoFactory;
    }

    /**
     * metodo per ottenere i config
     * @return
     */
    public Configs getConfig(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getConfigLocInfo();

        locInfo.setKey(configType.getValue());

        JsonObject cJO = dataLayer.get(locInfo);
        return jsonFactoryService.createObject(cJO, Configs.class);
    }

    /**
     * metodo per salvare i config
     * @param configs
     */
    public boolean save(Configs configs, ConfigType configType) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getConfigLocInfo();
    
        locInfo.setKey(configType.getValue());

        JsonObject JO = jsonFactoryService.createJson(configs);

        return dataLayer.modify(JO, locInfo);
            
    }
    
}
