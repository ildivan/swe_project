package server.data.facade;

import java.util.List;
import java.util.Optional;

import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Place;

public interface PlacesFacade {
    public List<Place> getPlaces();

    public List<Place> getChangeablePlaces();

    public Place getPlace(String placeName);

    public Place getChangedPlace(String placeName);

    public boolean addPlace(String name, String description, Address address);

    public boolean modifyPlace(String placeName, String newName, String newDescription, Optional<Address> newAddress);

    public boolean deletePlace(String placeName);

    public boolean doesPlaceExist(String placeName);

    public boolean existPlaceWithNoActivity();

    public List<Place> getCustomList();
}
