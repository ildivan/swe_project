import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

import server.data.Volunteer;
import server.data.facade.implementation.JsonVolunteersFacade;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.TestJsonLocInfoFactory;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;

public class JsonVolunteersFacadeTest {

    private JsonVolunteersFacade volunteersFacade;

    @Before
    public void setUp() {
        IJsonLocInfoFactory locInfoFactory = new TestJsonLocInfoFactory();
        volunteersFacade = new JsonVolunteersFacade(new JsonReadWrite(), locInfoFactory);
        // Clean up volunteers before each test
        List<Volunteer> existing = volunteersFacade.getVolunteers();
        for (Volunteer v : existing) {
            volunteersFacade.deleteVolunteer(v.getName());
        }
    }

    @Test
    public void testAddVolunteerAndGetVolunteer() {
        boolean added = volunteersFacade.addVolunteer("alice");
        assertTrue(added);

        Volunteer fetched = volunteersFacade.getVolunteer("alice");
        assertNotNull(fetched);
        assertEquals("alice", fetched.getName());
    }

    @Test
    public void testAddVolunteerDuplicate() {
        assertTrue(volunteersFacade.addVolunteer("bob"));
        // Adding again should still return true (since add always returns exists after add)
        assertFalse(volunteersFacade.addVolunteer("bob"));
        // But only one volunteer with that name should exist
        List<Volunteer> all = volunteersFacade.getVolunteers();
        int count = 0;
        for (Volunteer v : all) {
            if ("bob".equals(v.getName())) count++;
        }
        assertEquals(1, count);
    }

    @Test
    public void testGetVolunteersReturnsAll() {
        volunteersFacade.addVolunteer("user1");
        volunteersFacade.addVolunteer("user2");
        List<Volunteer> volunteers = volunteersFacade.getVolunteers();
        Set<String> names = new HashSet<>();
        for (Volunteer v : volunteers) names.add(v.getName());
        assertTrue(names.contains("user1"));
        assertTrue(names.contains("user2"));
    }

    @Test
    public void testSaveVolunteerUpdatesFields() {
        volunteersFacade.addVolunteer("carol");
        volunteersFacade.modifyVolunteer(
            "carol",
            Optional.of("carol_new"),
            Optional.empty(),
            Optional.empty()
        );
        Volunteer updated = volunteersFacade.getVolunteer("carol_new");
        assertNotNull(updated);
        assertEquals("carol_new", updated.getName());
    }

    @Test
    public void testGetVolunteerReturnsNullIfNotFound() {
        Volunteer v = volunteersFacade.getVolunteer("ghost");
        assertNull(v);
    }

    @Test
    public void testDeleteVolunteerRemovesVolunteer() {
        volunteersFacade.addVolunteer("dave");
        assertTrue(volunteersFacade.deleteVolunteer("dave"));
        assertNull(volunteersFacade.getVolunteer("dave"));
        assertFalse(volunteersFacade.doesVolunteerExist("dave"));
    }

    @Test
    public void testDeleteVolunteerReturnsTrueIfVolunteerDidNotExist() {
        assertTrue(volunteersFacade.deleteVolunteer("ghost"));
    }

    @Test
    public void testDoesVolunteerExistTrueAndFalse() {
        volunteersFacade.addVolunteer("frank");
        assertTrue(volunteersFacade.doesVolunteerExist("frank"));
        assertFalse(volunteersFacade.doesVolunteerExist("notexist"));
    }

    @Test(expected = AssertionError.class)
    public void testGetVolunteerNullThrowsAssertionError() {
        volunteersFacade.getVolunteer(null);
    }
}
