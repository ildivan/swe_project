package server.firstleveldomainservices.configuratorservice;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.firstleveldomainservices.Place;
import server.gsonfactoryservice.GsonFactoryService;
import server.gsonfactoryservice.IGsonFactory;


public class PlacesUtilForConfigService {

    private IGsonFactory gsonFactoryService = new GsonFactoryService();
    private final ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory;
    private final Gson gson = gsonFactoryService.getGson();
    private static final IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();

    public PlacesUtilForConfigService(ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory) {
        this.locInfoFactory = locInfoFactory;
    }

    /**
     * la condizione è che ci sia almeno luogo senza attivita associata se non c'è ritorna vero
     * @return
     */
    public boolean existPlaceWithNoActivity(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getPlaceLocInfo();

        locInfo.setKeyDesc("atLeastOneActivityRelated");
        locInfo.setKey("false");

        if(dataLayer.get(locInfo)==null){
            return false;
        } else {
            return true;
        }
    }

    public List<Place> getCustomList(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getPlaceLocInfo();

        locInfo.setKeyDesc("atLeastOneActivityRelated");
        locInfo.setKey("false");
        List<JsonObject> pJO = dataLayer.getList(locInfo);
        List<Place> places = new ArrayList<>();
        
        for(JsonObject jo : pJO){
            Place p = gson.fromJson(jo, Place.class);
            if(!p.getAtLeastOneActivityRelated()){
                places.add(p);
            }
        }
        return places;
    }
}
