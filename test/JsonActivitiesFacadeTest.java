import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;

import static org.junit.Assert.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import server.data.Activity;
import server.data.Address;
import server.data.Place;
import server.data.facade.implementation.JsonActivitiesFacade;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.data.json.datalayer.datalocalizationinformations.TestJsonLocInfoFactory;
import server.data.json.datalayer.datareadwrite.IJsonReadWrite;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.*;
import server.jsonfactoryservice.JsonFactoryService;

public class JsonActivitiesFacadeTest {

    private JsonActivitiesFacade activitiesFacade;

    @Before
    public void setUp() {
        Path path = Path.of("test/JFTest");
        try {
            if (Files.exists(path)) {
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
            }
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Setup failed: " + e.getMessage());
        }
        IJsonReadWrite readWrite = new JsonReadWrite();
        IJsonLocInfoFactory locInfoFactory = new TestJsonLocInfoFactory();
        activitiesFacade = new JsonActivitiesFacade(readWrite, locInfoFactory);
    }

    private Place createPlace() {
        Address address = new Address("Street", "City", "Country", "12345");
        return new Place("TestPlace", address, "A test place");
    }

    @Test
    public void testAddActivity() {
        Place place = createPlace();
        Address meetingPoint = new Address("Meet", "City", "Country", "54321");
        String[] days = {"Monday", "Tuesday"};
        String[] volunteers = {"Alice", "Bob"};
        Activity activity = activitiesFacade.addActivity(
                place, "TestActivity", "desc", meetingPoint,
                LocalDate.now(), LocalDate.now().plusDays(1),
                days, LocalTime.NOON, LocalTime.of(1, 0),
                true, 10, 2, volunteers
        );
        assertNotNull(activity);
        assertEquals("TestActivity", activity.getTitle());
        assertTrue(activitiesFacade.doesActivityExist("TestActivity"));
    }

    @Test
    public void testAddActivityWithExistingName() {
        Place place = createPlace();
        Address meetingPoint = new Address("Meet", "City", "Country", "54321");
        String[] days = {"Monday"};
        String[] volunteers = {"Alice"};
        activitiesFacade.addActivity(
                place, "TestActivity", "desc", meetingPoint,
                LocalDate.now(), LocalDate.now().plusDays(1),
                days, LocalTime.NOON, LocalTime.of(1, 0),
                true, 10, 2, volunteers
        );
        // Should allow adding another activity with a different name
        Activity activity2 = activitiesFacade.addActivity(
                place, "TestActivity2", "desc2", meetingPoint,
                LocalDate.now(), LocalDate.now().plusDays(2),
                days, LocalTime.NOON, LocalTime.of(2, 0),
                false, 20, 5, volunteers
        );
        assertNotNull(activity2);
        assertEquals("TestActivity2", activity2.getTitle());
    }

    @Test
    public void testGetActivity() {
        Place place = createPlace();
        Address meetingPoint = new Address("Meet", "City", "Country", "54321");
        String[] days = {"Monday"};
        String[] volunteers = {"Alice"};
        activitiesFacade.addActivity(
                place, "TestActivity", "desc", meetingPoint,
                LocalDate.now(), LocalDate.now().plusDays(1),
                days, LocalTime.NOON, LocalTime.of(1, 0),
                true, 10, 2, volunteers
        );
        Activity activity = activitiesFacade.getChangedActivity("TestActivity");
        assertNotNull(activity);
        assertEquals("TestActivity", activity.getTitle());
        assertEquals("desc", activity.getDescription());
    }

    @Test
    public void testGetActivityNotFound() {
        try{
            activitiesFacade.getActivity("NonExistent");
            fail("Expected AssertionError for non-existent activity");
        } catch (AssertionError e) {
            // Expected, since the activity does not exist
        }
    }

    @Test
    public void testGetActivities() {
        Place place = createPlace();
        Address meetingPoint = new Address("Meet", "City", "Country", "54321");
        String[] days = {"Monday"};
        String[] volunteers = {"Alice"};
        activitiesFacade.addActivity(
                place, "TestActivity1", "desc1", meetingPoint,
                LocalDate.now(), LocalDate.now().plusDays(1),
                days, LocalTime.NOON, LocalTime.of(1, 0),
                true, 10, 2, volunteers
        );
        activitiesFacade.addActivity(
                place, "TestActivity2", "desc2", meetingPoint,
                LocalDate.now(), LocalDate.now().plusDays(2),
                days, LocalTime.NOON, LocalTime.of(2, 0),
                false, 20, 5, volunteers
        );
        List<Activity> activities = activitiesFacade.getChangedActivities();
        assertNotNull(activities);
        assertTrue(activities.size() >= 2);
    }

    @Test
    public void testModifyActivity() {
        Place place = createPlace();
        Address meetingPoint = new Address("Meet", "City", "Country", "54321");
        String[] days = {"Monday"};
        String[] volunteers = {"Alice"};
        activitiesFacade.addActivity(
                place, "TestActivity", "desc", meetingPoint,
                LocalDate.now(), LocalDate.now().plusDays(1),
                days, LocalTime.NOON, LocalTime.of(1, 0),
                true, 10, 2, volunteers
        );
        Address newMeetingPoint = new Address("NewMeet", "NewCity", "NewCountry", "99999");
        boolean modified = activitiesFacade.modifyActivity(
                "TestActivity",
                Optional.of("NewTitle"),
                Optional.of("NewDesc"),
                Optional.of(newMeetingPoint),
                Optional.of(LocalDate.now().plusDays(2)),
                Optional.of(LocalDate.now().plusDays(3)),
                Optional.of(new String[]{"Friday"}),
                Optional.of(LocalTime.of(15, 0)),
                Optional.of(LocalTime.of(2, 30)),
                Optional.of(false),
                Optional.of(50),
                Optional.of(5),
                Optional.of(new String[]{"Bob", "Charlie"})
        );
        assertTrue(modified);
        Activity modifiedActivity = activitiesFacade.getChangedActivity("NewTitle");
        assertNotNull(modifiedActivity);
        assertEquals("NewTitle", modifiedActivity.getTitle());
        assertEquals("NewDesc", modifiedActivity.getDescription());
        assertEquals("NewMeet", modifiedActivity.getMeetingPoint().getStreet());
        assertEquals(50, modifiedActivity.getMaxPartecipanti());
        assertEquals(5, modifiedActivity.getMinPartecipanti());
        assertArrayEquals(new String[]{"Bob", "Charlie"}, modifiedActivity.getVolunteers());
    }

    @Test
    public void testModifyActivityNotFound() {
        boolean modified = activitiesFacade.modifyActivity(
                "NonExistent",
                Optional.of("Title"),
                Optional.of("Desc"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        assertFalse(modified);
    }

    @Test
    public void testDeleteActivity() {
        Place place = createPlace();
        Address meetingPoint = new Address("Meet", "City", "Country", "54321");
        String[] days = {"Monday"};
        String[] volunteers = {"Alice"};
        activitiesFacade.addActivity(
                place, "TestActivity", "desc", meetingPoint,
                LocalDate.now(), LocalDate.now().plusDays(1),
                days, LocalTime.NOON, LocalTime.of(1, 0),
                true, 10, 2, volunteers
        );
        boolean deleted = activitiesFacade.deleteActivity("TestActivity");
        assertTrue(deleted);
        assertFalse(activitiesFacade.doesActivityExist("TestActivity"));
    }

    @Test
    public void testDeleteNonExistentActivity() {
        boolean deleted = activitiesFacade.deleteActivity("NonExistent");
        assertFalse(deleted);
    }

    @Test
    public void testDoesActivityExist() {
        Place place = createPlace();
        Address meetingPoint = new Address("Meet", "City", "Country", "54321");
        String[] days = {"Monday"};
        String[] volunteers = {"Alice"};
        activitiesFacade.addActivity(
                place, "ExistActivity", "desc", meetingPoint,
                LocalDate.now(), LocalDate.now().plusDays(1),
                days, LocalTime.NOON, LocalTime.of(1, 0),
                true, 10, 2, volunteers
        );
        assertTrue(activitiesFacade.doesActivityExist("ExistActivity"));
        assertFalse(activitiesFacade.doesActivityExist("NonExistent"));
    }

    @Test
    public void testGetChangedActivities() {
        Place place = createPlace();
        Address meetingPoint = new Address("Meet", "City", "Country", "54321");
        String[] days = {"Monday"};
        String[] volunteers = {"Alice"};
        activitiesFacade.addActivity(
                place, "ChangedActivity", "desc", meetingPoint,
                LocalDate.now(), LocalDate.now().plusDays(1),
                days, LocalTime.NOON, LocalTime.of(1, 0),
                true, 10, 2, volunteers
        );
        List<Activity> changed = activitiesFacade.getChangedActivities();
        assertNotNull(changed);
        boolean found = false;
        for (Activity a : changed) {
            if ("ChangedActivity".equals(a.getTitle())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testGetActivitiesByState() {
        MonthlyPlan monthlyPlan = getMonthlyPlan();

        List<ActivityRecord> result = activitiesFacade.getActivitiesByState(ActivityState.CONFERMATA, monthlyPlan);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Visita Guidata al Duomo Vecchio", result.get(0).getName());
    }

    @Test
    public void testGetActivitiesByStateReturnsNullIfMonthlyPlanNull() {
        List<ActivityRecord> result = activitiesFacade.getActivitiesByState(ActivityState.CONFERMATA, null);
        assertNull(result);
    }

    @Test(expected = AssertionError.class)
    public void testGetActivityWithNullNameThrowsAssertion() {
        activitiesFacade.getActivity(null);
    }

    @Test(expected = AssertionError.class)
    public void testDeleteActivityWithNullNameThrowsAssertion() {
        activitiesFacade.deleteActivity(null);
    }

    @Test(expected = AssertionError.class)
    public void testDoesActivityExistWithNullNameThrowsAssertion() {
        activitiesFacade.doesActivityExist(null);
    }

    public synchronized MonthlyPlan getMonthlyPlan(){
        JsonDataLayer dataLayer = new JsonDataLayer(new JsonReadWrite());
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath("test/MonthlyPlanJsonFileTest/monthlyPlan.json");
        locInfo.setMemberName("monthlyPlan");
        locInfo.setKeyDesc("date");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        locInfo.setKey(formatter.format(LocalDate.of(2025,8,16)));
        JsonObject mpJO = dataLayer.get(locInfo);

        if(mpJO == null){
            return null;
        }
        JsonFactoryService jsonFactoryService = new JsonFactoryService();
        return jsonFactoryService.createObject(mpJO, MonthlyPlan.class);
    }
}
