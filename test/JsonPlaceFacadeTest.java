import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import server.data.facade.implementation.JsonPlacesFacade;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.TestJsonLocInfoFactory;
import server.data.json.datalayer.datareadwrite.IJsonReadWrite;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;
import server.firstleveldomainservices.Address;
import server.firstleveldomainservices.Place;


public class JsonPlaceFacadeTest {

    private JsonPlacesFacade placesFacade;

    @Before
    public void setUp() {
        Path path = Path.of("test/JFTest");
        try {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        e.printStackTrace();
                        fail("Setup failed: " + e.getMessage());
                    }
                });
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Setup failed: " + e.getMessage());
        }
        IJsonReadWrite readWrite = new JsonReadWrite();
        IJsonLocInfoFactory locInfoFactory = new TestJsonLocInfoFactory();
        placesFacade = new JsonPlacesFacade(readWrite, locInfoFactory);
    }

    @Test
    public void testAddPlace() {
        Address address = new Address("Street", "City", "Country", "12345");
        boolean added = placesFacade.addPlace("TestPlace", "A test place", address);
        assertTrue(added);
        assertTrue(placesFacade.doesPlaceExist("TestPlace"));
    }

    @Test
    public void testAddPlaceWithExistingName() {
        Address address = new Address("Street", "City", "Country", "12345");
        placesFacade.addPlace("TestPlace", "A test place", address);
        try {
            placesFacade.addPlace("TestPlace", "Another test place", address);
            fail("Should throw AssertionError for duplicate place name");
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void testGetPlace() {
        Address address = new Address("Street", "City", "Country", "12345");
        placesFacade.addPlace("TestPlace", "A test place", address);
        Place place = placesFacade.getChangedPlace("TestPlace");
        assertNotNull(place);
        assertEquals("TestPlace", place.getName());
        assertEquals("A test place", place.getDescription());
        assertEquals("Street", place.getAddress().getStreet());
    }

    @Test
    public void testGetPlaceNotFound() {
        Place place = placesFacade.getPlace("NonExistent");
        assertNull(place);
    }

    @Test
    public void testModifyPlace() {
        Address address = new Address("Street", "City", "Country", "12345");
        placesFacade.addPlace("TestPlace", "A test place", address);
        Address newAddress = new Address("NewStreet", "NewCity", "NewCountry", "54321");
        boolean modified = placesFacade.modifyPlace("TestPlace", "NewName", "New description", Optional.of(newAddress));
        assertTrue(modified);
        Place modifiedPlace = placesFacade.getChangedPlace("NewName");
        assertNotNull(modifiedPlace);
        assertEquals("NewName", modifiedPlace.getName());
        assertEquals("New description", modifiedPlace.getDescription());
        assertEquals("NewStreet", modifiedPlace.getAddress().getStreet());
    }

    @Test
    public void testModifyPlaceWithNullAddress() {
        Address address = new Address("Street", "City", "Country", "12345");
        placesFacade.addPlace("TestPlace", "A test place", address);
        boolean modified = placesFacade.modifyPlace("TestPlace", "NewName", "New description", Optional.<Address>empty());
        assertTrue(modified);
        Place modifiedPlace = placesFacade.getChangedPlace("NewName");
        assertNotNull(modifiedPlace);
        assertEquals("NewName", modifiedPlace.getName());
        assertEquals("New description", modifiedPlace.getDescription());
        // Address should remain unchanged
        assertEquals("Street", modifiedPlace.getAddress().getStreet());
    }

    @Test
    public void testDeletePlace() {
        Address address = new Address("Street", "City", "Country", "12345");
        placesFacade.addPlace("TestPlace", "A test place", address);
        boolean deleted = placesFacade.deletePlace("TestPlace");
        assertTrue(deleted);
        assertFalse(placesFacade.doesPlaceExist("TestPlace"));
    }

    @Test
    public void testDeleteNonExistentPlace() {
        boolean deleted = placesFacade.deletePlace("NonExistent");
        assertFalse(deleted);
    }

    @Test
    public void testGetPlaces() {
        Address address1 = new Address("Street1", "City1", "Country1", "11111");
        Address address2 = new Address("Street2", "City2", "Country2", "22222");
        placesFacade.addPlace("Place1", "Description1", address1);
        placesFacade.addPlace("Place2", "Description2", address2);
        List<Place> places = placesFacade.getChangeablePlaces();
        assertNotNull(places);
        assertTrue(places.size() >= 2);
    }

    @Test
    public void testGetChangeablePlaces() {
        Address address = new Address("Street", "City", "Country", "12345");
        placesFacade.addPlace("ChangeablePlace", "Changeable", address);
        List<Place> changeablePlaces = placesFacade.getChangeablePlaces();
        assertNotNull(changeablePlaces);
        boolean found = false;
        for (Place p : changeablePlaces) {
            if ("ChangeablePlace".equals(p.getName())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testDoesPlaceExist() {
        Address address = new Address("Street", "City", "Country", "12345");
        placesFacade.addPlace("ExistPlace", "desc", address);
        assertTrue(placesFacade.doesPlaceExist("ExistPlace"));
        assertFalse(placesFacade.doesPlaceExist("NonExistent"));
    }

    @Test
    public void testExistPlaceWithNoActivity() {
        boolean exists = placesFacade.existPlaceWithNoActivity();
        assertNotNull(exists);
    }

    @Test
    public void testGetCustomList() {
        Address address = new Address("Street", "City", "Country", "12345");
        placesFacade.addPlace("TestPlace", "A test place", address);
        List<Place> customList = placesFacade.getCustomList();
        assertNotNull(customList);
    }

    @Test
    public void testGetCustomListFail() {
        Address address = new Address("Street", "City", "Country", "12345");

        placesFacade.addPlace("TestPlace", "A test place", address);
        Place place = placesFacade.getChangedPlace("TestPlace");
        placesFacade.modifyPlace(
            place.getName(), place.getName(), place.getDescription(), 
            Optional.of(place.getAddress()), true
        );
        List<Place> customList = placesFacade.getCustomList();
        assertNull(customList);
    }
}
