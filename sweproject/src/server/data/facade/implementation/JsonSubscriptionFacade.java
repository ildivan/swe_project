package server.data.facade.implementation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;

import server.data.facade.interfaces.ISubscriptionFacade;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.data.json.datalayer.datareadwrite.IJsonReadWrite;
import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.Subscription;
import server.jsonfactoryservice.JsonFactoryService;

public class JsonSubscriptionFacade implements ISubscriptionFacade{
    private static final String SUBSCRIPTION_KEY_DESC = "subscriptionId";

    private JsonDataLayer dataLayer;
    private final IJsonLocInfoFactory locInfoFactory;
    private final JsonFactoryService jsonFactoryService = new JsonFactoryService();

    public JsonSubscriptionFacade(IJsonReadWrite readWrite, 
                            IJsonLocInfoFactory locInfoFactory) {
        this.dataLayer = new JsonDataLayer(readWrite);
        this.locInfoFactory = locInfoFactory;
    }

    /**
     * metodo per salvare l'iscrizione
     * @param subscription
     */
    @Override
    public void saveSubscription(Subscription subscription) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getSubscriptionLocInfo();
        dataLayer.add(jsonFactoryService.createJson(subscription), locInfo);
    }
    @Override
    public Set<Subscription> getAllSubs(){
        Set<Subscription> subscriptions = new HashSet<>();
        List<JsonObject> subscriptionsJO =  dataLayer.getAll(locInfoFactory.getSubscriptionLocInfo());;

        for (JsonObject jsonObject : subscriptionsJO) {
            Subscription subscription = jsonFactoryService.createObject(jsonObject, Subscription.class);
            subscriptions.add(subscription);
        }

        return subscriptions;
    }

    @Override
    public void deleteSubscription(Subscription subscription){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getSubscriptionLocInfo();
        locInfo.setKeyDesc(SUBSCRIPTION_KEY_DESC);
        locInfo.setKey(String.valueOf(subscription.getSubscriptionId()));

        dataLayer.delete(locInfo);
    }

}
