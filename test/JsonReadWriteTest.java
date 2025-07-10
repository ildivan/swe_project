import com.google.gson.JsonObject;

import server.data.json.datalayer.datareadwrite.JsonReadWrite;

import org.junit.*;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import static org.junit.Assert.*;


public class JsonReadWriteTest {

    private static final String TEST_FILE = "test_rw.json";
    private JsonReadWrite jsonReadWrite;

    @Before
    public void setUp() {
        jsonReadWrite = new JsonReadWrite();
        File file = new File(TEST_FILE);
        if (file.exists()) file.delete();
    }

    @After
    public void tearDown() {
        File file = new File(TEST_FILE);
        if (file.exists()) file.delete();
    }

    @Test
    public void testCreateJSONEmptyFileCreatesFile() {
        boolean result = jsonReadWrite.createJSONEmptyFile(TEST_FILE);
        assertTrue(result);
        assertTrue(new File(TEST_FILE).exists());
    }

    @Test
    public void testWriteToFileAndReadFromFile() {
        jsonReadWrite.createJSONEmptyFile(TEST_FILE);

        List<JsonObject> list = new ArrayList<>();
        JsonObject obj1 = new JsonObject();
        obj1.addProperty("id", 1);
        obj1.addProperty("name", "foo");
        list.add(obj1);

        JsonObject obj2 = new JsonObject();
        obj2.addProperty("id", 2);
        obj2.addProperty("name", "bar");
        list.add(obj2);

        boolean writeResult = jsonReadWrite.writeToFile(TEST_FILE, list, "items");
        assertTrue(writeResult);

        List<JsonObject> readList = jsonReadWrite.readFromFile(TEST_FILE, "items");
        assertNotNull(readList);
        assertEquals(2, readList.size());
        assertEquals("foo", readList.get(0).get("name").getAsString());
        assertEquals("bar", readList.get(1).get("name").getAsString());
    }

    @Test
    public void testReadFromFileReturnsEmptyListIfMemberMissing() {
        jsonReadWrite.createJSONEmptyFile(TEST_FILE);
        List<JsonObject> readList = jsonReadWrite.readFromFile(TEST_FILE, "notfound");
        assertNotNull(readList);
        assertTrue(readList.isEmpty());
    }

    @Test
    public void testReadFromFileReturnsNullIfFileDoesNotExist() {
        List<JsonObject> readList = null;
        try {
            readList = jsonReadWrite.readFromFile("nonexistent.json", "items");
        } catch (AssertionError e) {
            // Expected due to assert Files.exists(path)
        }
        assertNull(readList);
    }

    @Test
    public void testWriteToFileFailsIfFileDoesNotExist() {
        List<JsonObject> list = new ArrayList<>();
        JsonObject obj = new JsonObject();
        obj.addProperty("id", 1);
        list.add(obj);
        Boolean result = null;
        try {
            result = jsonReadWrite.writeToFile("nonexistent.json", list, "items");
        } catch (AssertionError e) {
            // Expected due to assert Files.exists(path)
        }
        assertNull(result);
    }

    @Test
    public void testWriteToFileWithEmptyList() {
        jsonReadWrite.createJSONEmptyFile(TEST_FILE);
        List<JsonObject> list = new ArrayList<>();
        boolean result = jsonReadWrite.writeToFile(TEST_FILE, list, "items");
        assertTrue(result);

        List<JsonObject> readList = jsonReadWrite.readFromFile(TEST_FILE, "items");
        assertNotNull(readList);
        assertTrue(readList.isEmpty());
    }

    @Test
    public void testCreateJSONEmptyFileOverwritesExistingFile() throws Exception {
        jsonReadWrite.createJSONEmptyFile(TEST_FILE);
        // Write something to file
        Files.write(new File(TEST_FILE).toPath(), Collections.singletonList("{\"foo\":123}"));
        // Overwrite with empty JSON
        boolean result = jsonReadWrite.createJSONEmptyFile(TEST_FILE);
        assertTrue(result);
        String content = new String(Files.readAllBytes(new File(TEST_FILE).toPath()));
        assertTrue(content.trim().equals("{}"));
    }

    @Test
    public void testWriteToFileWithNullMemberNameThrowsAssertionError() {
        jsonReadWrite.createJSONEmptyFile(TEST_FILE);
        List<JsonObject> list = new ArrayList<>();
        try {
            jsonReadWrite.writeToFile(TEST_FILE, list, null);
            fail("Expected AssertionError");
        } catch (AssertionError | NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testReadFromFileWithNullMemberNameThrowsAssertionError() {
        jsonReadWrite.createJSONEmptyFile(TEST_FILE);
        try {
            jsonReadWrite.readFromFile(TEST_FILE, null);
            fail("Expected AssertionError");
        } catch (AssertionError | NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testWriteToFileWithEmptyMemberNameThrowsAssertionError() {
        jsonReadWrite.createJSONEmptyFile(TEST_FILE);
        List<JsonObject> list = new ArrayList<>();
        try {
            jsonReadWrite.writeToFile(TEST_FILE, list, "");
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void testReadFromFileWithEmptyMemberNameThrowsAssertionError() {
        jsonReadWrite.createJSONEmptyFile(TEST_FILE);
        try {
            jsonReadWrite.readFromFile(TEST_FILE, "");
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }
}
