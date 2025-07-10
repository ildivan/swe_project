package server.data.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import server.data.facade.PlacesFacade;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.data.json.datalayer.datareadwrite.IJsonReadWrite;
import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Place;
import server.gsonfactoryservice.GsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;

public class JsonPlacesFacade implements PlacesFacade{
    private JsonDataLayer dataLayer;
    private final IJsonLocInfoFactory locInfoFactory;
    private final JsonFactoryService jsonFactoryService = new JsonFactoryService();
    private final Gson gson = new GsonFactoryService().getGson();

    public JsonPlacesFacade(IJsonReadWrite readWrite, 
                            IJsonLocInfoFactory locInfoFactory) {
        this.dataLayer = new JsonDataLayer(readWrite);
        this.locInfoFactory = locInfoFactory;
    }

    
    public List<Place> getPlaces() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getPlaceLocInfo();

        List<JsonObject> placesJO = dataLayer.getAll(locInfo);
        List<Place> places = jsonFactoryService.createObjectList(placesJO, Place.class);

        return places;
    }

    public Place getPlace(String placeName) {
        assert placeName != null && !placeName.trim().isEmpty() : "Il nome del luogo non può essere vuoto";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();
        return getPlaceUtil(placeName, locInfo);
    }


    public Place getChangedPlace(String placeName) {
        assert placeName != null && !placeName.trim().isEmpty() : "Il nome del luogo non può essere vuoto";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();
        return getPlaceUtil(placeName, locInfo);
    }


    private Place getPlaceUtil(String placeName, JsonDataLocalizationInformation placesLocInfo) {
        assert placeName != null && !placeName.trim().isEmpty() : "Il nome del luogo non può essere vuoto";

        placesLocInfo.setKey(placeName);
        JsonObject placeJO = dataLayer.get(placesLocInfo);

        if (placeJO != null) {
            return jsonFactoryService.createObject(placeJO, Place.class);
        }
        return null;
    }
    

    public boolean addPlace(String name, String description, Address address){
        
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();
        locInfo.setKey(name);

        assert name != null && !name.trim().isEmpty() : "Il nome del luogo non può essere vuoto";
        assert !dataLayer.exists(locInfo) : "Il luogo con questo nome esiste già";

        dataLayer.add((jsonFactoryService.createJson(new Place(name, address, description))),locInfo);

        boolean placeAdded = dataLayer.exists(locInfo);
        return placeAdded;
    }

    
    public boolean modifyPlace(String placeName, String newName, String newDescription, Optional<Address> newAddress) {
        assert placeName != null && !placeName.trim().isEmpty() : "Il nome del luogo non può essere vuoto";
        assert newName != null && !newName.trim().isEmpty() : "Il nuovo nome del luogo non può essere vuoto";
        assert newDescription != null && !newDescription.trim().isEmpty() : "La nuova descrizione del luogo non può essere vuota";

        Place place = getPlace(placeName);
        place.setName(newName);
        place.setDescription(newDescription);
        if (newAddress.isPresent()) {
            place.setAddress(newAddress.get());
        }

        boolean saved = savePlace(placeName, place);
        return saved;
    }


    private boolean savePlace(String placeName, Place place) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();
        locInfo.setKey(placeName);
        
        JsonObject placeJO = jsonFactoryService.createJson(place);
        boolean modified = dataLayer.modify(placeJO, locInfo);
        
        return modified;
    }


    public List<Place> getChangeablePlaces() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();

        List<JsonObject> placesJO = dataLayer.getAll(locInfo);
        List<Place> places = jsonFactoryService.createObjectList(placesJO, Place.class);

        return places;
    }


    
    public boolean deletePlace(String placeName) {
        assert placeName != null && !placeName.trim().isEmpty() : "Nome luogo non valido";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();
        locInfo.setKey(placeName);
        if (dataLayer.exists(locInfo)) {
            dataLayer.delete(locInfo);
            return true;
        }
        return false;
    }

    public boolean doesPlaceExist(String placeName) {
        assert placeName != null && !placeName.trim().isEmpty() : "Il nome del luogo non può essere vuoto";

        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();
        locInfo.setKey(placeName);
        return dataLayer.exists(locInfo);
    }

    /**
     * la condizione è che ci sia almeno un luogo senza attivita associata se non c'è ritorna vero
     * @return
     */
    public boolean existPlaceWithNoActivity(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();

        locInfo.setKeyDesc("atLeastOneActivityRelated");
        locInfo.setKey("false");

        if(dataLayer.get(locInfo)==null){
            return false;
        } else {
            return true;
        }
    }

    public List<Place> getCustomList(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getChangedPlacesLocInfo();

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
