package server.firstleveldomainservices.configuratorservice;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import server.datalayerservice.DataLayerDispatcherService;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.firstleveldomainservices.Place;
import server.gsonfactoryservice.GsonFactoryService;
import server.gsonfactoryservice.IGsonFactory;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;

public class PlacesUtilForConfigService {

    private IGsonFactory gsonFactoryService = new GsonFactoryService();
    private static final ILocInfoFactory locInfoFactory = new JsonLocInfoFactory();
    private final Gson gson = gsonFactoryService.getGson();

    /**
     * la condizione è che ci sia almeno luogo senza attivita associata se non c'è ritorna vero
     * @return
     */
    public static boolean existPlaceWithNoActivity(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getPlaceLocInfo();

        locInfo.setKeyDesc("atLeastOneActivityRelated");
        locInfo.setKey("false");

        if(DataLayerDispatcherService.startWithResult(locInfo, layer->layer.get(locInfo))==null){
            return false;
        } else {
            return true;
        }
    }

    public List<Place> getCustomList(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getPlaceLocInfo();

        locInfo.setKeyDesc("atLeastOneActivityRelated");
        locInfo.setKey("false");
        List<JsonObject> pJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.getList(locInfo));
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
