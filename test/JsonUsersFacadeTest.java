import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import server.data.facade.implementation.JsonUsersFacade;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.TestJsonLocInfoFactory;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;
import server.authservice.User;

public class JsonUsersFacadeTest {

    private JsonUsersFacade usersFacade;

    @Before
    public void setUp() {
        IJsonLocInfoFactory locInfoFactory = new TestJsonLocInfoFactory();
        usersFacade = new JsonUsersFacade(new JsonReadWrite(), locInfoFactory);
        // Clean up users before each test
        List<User> existing = usersFacade.getUsers();
        for (User u : existing) {
            usersFacade.deleteUser(u.getName());
        }
    }

    @Test
    public void testAddUserAndGetUser() {
        User user = new User("alice", "pass", "fruitore");
        boolean added = usersFacade.addUser(user);
        assertTrue(added);

        User fetched = usersFacade.getUser("alice");
        assertNotNull(fetched);
        assertEquals("alice", fetched.getName());
        assertEquals("fruitore", fetched.getRole());
    }

    @Test
    public void testAddUserDuplicateReturnsFalse() {
        User user = new User("bob", "pass", "configuratore");
        assertTrue(usersFacade.addUser(user));
        assertFalse(usersFacade.addUser(user));
    }

    @Test
    public void testGetUsersReturnsAll() {
        usersFacade.addUser(new User("user1", "p1", "fruitore"));
        usersFacade.addUser(new User("user2", "p2", "configuratore"));
        List<User> users = usersFacade.getUsers();
        Set<String> names = new HashSet<>();
        for (User u : users) names.add(u.getName());
        assertTrue(names.contains("user1"));
        assertTrue(names.contains("user2"));
    }

    @Test
    public void testModifyUserUpdatesFields() {
        usersFacade.addUser(new User("carol", "oldpass", "fruitore"));
        boolean modified = usersFacade.modifyUser(
            "carol",
            Optional.of("carol_new"),
            Optional.of("newpass"),
            Optional.of("configuratore"),
            Optional.of(true),
            Optional.of(false)
        );
        assertTrue(modified);
        User updated = usersFacade.getUser("carol_new");
        assertNotNull(updated);
        assertEquals("carol_new", updated.getName());
        assertEquals("newpass", updated.getPassword());
        assertEquals("configuratore", updated.getRole());
        assertTrue(updated.isActive());
        assertFalse(updated.isDeleted());
    }

    @Test
    public void testModifyUserReturnsFalseIfNotFound() {
        boolean modified = usersFacade.modifyUser(
            "notfound",
            Optional.of("newname"),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
        assertFalse(modified);
    }

    @Test
    public void testDeleteUserRemovesUser() {
        usersFacade.addUser(new User("dave", "pass", "fruitore"));
        assertTrue(usersFacade.deleteUser("dave"));
        assertNull(usersFacade.getUser("dave"));
        assertFalse(usersFacade.doesUserExist("dave"));
    }

    @Test
    public void testDeleteUserReturnsTrueIfUserExisted() {
        usersFacade.addUser(new User("eve", "pass", "fruitore"));
        assertTrue(usersFacade.deleteUser("eve"));
    }

    @Test
    public void testDeleteUserReturnsTrueIfUserDidNotExist() {
        assertTrue(usersFacade.deleteUser("ghost"));
    }

    @Test
    public void testDoesUserExistTrueAndFalse() {
        usersFacade.addUser(new User("frank", "pass", "fruitore"));
        assertTrue(usersFacade.doesUserExist("frank"));
        assertFalse(usersFacade.doesUserExist("notexist"));
    }

    @Test(expected = AssertionError.class)
    public void testAddUserNullThrowsAssertionError() {
        usersFacade.addUser(null);
    }

    @Test(expected = AssertionError.class)
    public void testGetUserNullThrowsAssertionError() {
        usersFacade.getUser(null);
    }

    @Test(expected = AssertionError.class)
    public void testModifyUserNullUsernameThrowsAssertionError() {
        usersFacade.modifyUser(null, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Test(expected = AssertionError.class)
    public void testDeleteUserNullThrowsAssertionError() {
        usersFacade.deleteUser(null);
    }

    @Test(expected = AssertionError.class)
    public void testDoesUserExistNullThrowsAssertionError() {
        usersFacade.doesUserExist(null);
    }

    @Test
    public void testAddUsersBulk() {
        List<User> users = Arrays.asList(
            new User("bulk1", "p1", "fruitore"),
            new User("bulk2", "p2", "configuratore")
        );
        usersFacade.addUsers(users);
        assertNotNull(usersFacade.getUser("bulk1"));
        assertNotNull(usersFacade.getUser("bulk2"));
    }
}
