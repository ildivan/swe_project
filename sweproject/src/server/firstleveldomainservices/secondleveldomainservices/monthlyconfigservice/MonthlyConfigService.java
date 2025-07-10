package server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice;

import com.google.gson.JsonObject;

import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;

public class MonthlyConfigService {

    private static final String MONTHLY_CONFIG_KEY = "current";
    
    private final IJsonLocInfoFactory locInfoFactory;
    private final IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private final JsonDataLayer dataLayer;

    public MonthlyConfigService(IJsonLocInfoFactory locInfoFactory, JsonDataLayer dataLayer) {
        this.locInfoFactory = locInfoFactory;
        this.dataLayer = dataLayer;
    }

    /**
     * method to obtain monthly config
     * @return
     */
    public MonthlyConfig getMonthlyConfig(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyConfigLocInfo();
        locInfo.setKey(MONTHLY_CONFIG_KEY);

        JsonObject mcJO = dataLayer.get(locInfo);
        MonthlyConfig mc = jsonFactoryService.createObject(mcJO, MonthlyConfig.class);
        assert mc != null;
        return mc;

    }

    /**
     * method to save monthly config, to put in datalayer facade
     * @param monthlyConfig
     */
    public void saveMonthlyConfig(MonthlyConfig monthlyConfig) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyConfigLocInfo();
        locInfo.setKey(MONTHLY_CONFIG_KEY);

        JsonObject mcJO = jsonFactoryService.createJson(monthlyConfig);
        dataLayer.modify(mcJO, locInfo);
    }

    /**
     * metodo test and set per il subcode corrente, sincronizzato per evitare race conditions
     * @return
     */
    public synchronized int getCurrentSubCode(){
        MonthlyConfig mc = getMonthlyConfig();

        int subcode = mc.getSequenceSubscriptionNumber();
        mc.setSequenceSubscriptionNumber(subcode + 1);
        saveMonthlyConfig(mc);
        return subcode;
    }
}
