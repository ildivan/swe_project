import com.google.gson.JsonObject;

import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datareadwrite.IJsonReadWrite;
import org.junit.*;
import static org.junit.Assert.*;
import java.io.File;
import java.util.*;

public class JsonDataLayerTest {

    static class StubJsonReadWrite implements IJsonReadWrite {
        public Map<String, List<JsonObject>> fileData = new HashMap<>();
        public Set<String> createdFiles = new HashSet<>();
        public String lastWritePath = null;
        public String lastWriteMember = null;
        public List<JsonObject> lastWriteList = null;

        @Override
        public List<JsonObject> readFromFile(String path, String memberName) {
            return fileData.getOrDefault(path + ":" + memberName, new ArrayList<>());
        }

        @Override
        public Boolean writeToFile(String path, List<JsonObject> list, String memberName) {
            fileData.put(path + ":" + memberName, new ArrayList<>(list));
            lastWritePath = path;
            lastWriteMember = memberName;
            lastWriteList = new ArrayList<>(list);
            return true;
        }

        @Override
        public boolean createJSONEmptyFile(String path) {
            createdFiles.add(path);
            return true;
        }
    }

    private StubJsonReadWrite stub;
    private JsonDataLayer dataLayer;
    private JsonDataLocalizationInformation info;

    @Before
    public void setUp() {
        stub = new StubJsonReadWrite();
        dataLayer = new JsonDataLayer(stub);

        info = new JsonDataLocalizationInformation();
        info.setPath("test.json");
        info.setMemberName("items");
        info.setKeyDesc("id");
        info.setKey("1");
    }

    @After
    public void tearDown() {
        File file = new File("test.json");
        if (file.exists()) file.delete();
    }

    @Test
    public void testAddCreatesFileIfNotExists() {
        File file = new File("test.json");
        file.delete();
        dataLayer.add(createObj("1", "foo"), info);
        assertTrue(stub.createdFiles.contains("test.json"));
        assertEquals(1, stub.lastWriteList.size());
        assertEquals("1", stub.lastWriteList.get(0).get("id").getAsString());
    }

    @Test
    public void testAddAppendsToExisting() {
        stub.fileData.put("test.json:items", new ArrayList<>(Arrays.asList(createObj("1", "foo"))));
        dataLayer.add(createObj("2", "bar"), info);
        assertEquals(2, stub.lastWriteList.size());
        assertEquals("2", stub.lastWriteList.get(1).get("id").getAsString());
    }

    @Test
    public void testModifyUpdatesCorrectObject() {
        stub.fileData.put("test.json:items", new ArrayList<>(Arrays.asList(createObj("1", "foo"), createObj("2", "bar"))));
        JsonObject updated = createObj("1", "baz");
        boolean result = dataLayer.modify(updated, info);
        assertTrue(result);
        assertEquals("baz", stub.lastWriteList.get(0).get("name").getAsString());
        assertEquals("bar", stub.lastWriteList.get(1).get("name").getAsString());
    }

    @Test
    public void testModifyReturnsFalseIfNotFound() {
        stub.fileData.put("test.json:items", new ArrayList<>(Arrays.asList(createObj("2", "bar"))));
        JsonObject updated = createObj("1", "baz");
        boolean result = dataLayer.modify(updated, info);
        assertFalse(result);
        // Should not change the list
        assertEquals("bar", stub.fileData.get("test.json:items").get(0).get("name").getAsString());
    }

    @Test
    public void testDeleteRemovesCorrectObject() {
        stub.fileData.put("test.json:items", new ArrayList<>(Arrays.asList(createObj("1", "foo"), createObj("2", "bar"))));
        dataLayer.delete(info);
        assertEquals(1, stub.lastWriteList.size());
        assertEquals("2", stub.lastWriteList.get(0).get("id").getAsString());
    }

    @Test
    public void testDeleteWhenNoMatch() {
        stub.fileData.put("test.json:items", new ArrayList<>(Arrays.asList(createObj("2", "bar"))));
        dataLayer.delete(info);
        assertEquals(1, stub.lastWriteList.size());
        assertEquals("2", stub.lastWriteList.get(0).get("id").getAsString());
    }

    @Test
    public void testGetReturnsCorrectObject() {
        stub.fileData.put("test.json:items", new ArrayList<>(Arrays.asList(createObj("1", "foo"), createObj("2", "bar"))));
        JsonObject result = dataLayer.get(info);
        assertNotNull(result);
        assertEquals("foo", result.get("name").getAsString());
    }

    @Test
    public void testGetReturnsNullIfNotFound() {
        stub.fileData.put("test.json:items", new ArrayList<>(Arrays.asList(createObj("2", "bar"))));
        JsonObject result = dataLayer.get(info);
        assertNull(result);
    }

    @Test
    public void testExistsTrueAndFalse() {
        stub.fileData.put("test.json:items", new ArrayList<>(Arrays.asList(createObj("1", "foo"))));
        assertTrue(dataLayer.exists(info));
        info.setKey("notfound");
        assertFalse(dataLayer.exists(info));
    }

    @Test
    public void testCheckFileExistanceFalseAndTrue() {
        File file = new File("test.json");
        file.delete();
        assertFalse(dataLayer.checkFileExistance(info));
        try {
            file.createNewFile();
            assertTrue(dataLayer.checkFileExistance(info));
        } catch (Exception e) {
            fail("File creation failed");
        } finally {
            file.delete();
        }
    }

    @Test
    public void testCreateJSONEmptyFile() {
        dataLayer.createJSONEmptyFile(info);
        assertTrue(stub.createdFiles.contains("test.json"));
    }

    @Test
    public void testGetListReturnsMatching() {
        stub.fileData.put("test.json:items", new ArrayList<>(Arrays.asList(createObj("1", "foo"), createObj("2", "bar"))));
        List<JsonObject> result = dataLayer.getList(info);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("foo", result.get(0).get("name").getAsString());
    }

    @Test
    public void testGetListReturnsNullIfNoneMatch() {
        stub.fileData.put("test.json:items", new ArrayList<>(Arrays.asList(createObj("2", "bar"))));
        List<JsonObject> result = dataLayer.getList(info);
        assertNull(result);
    }

    @Test
    public void testGetAllReturnsAll() {
        stub.fileData.put("test.json:items", new ArrayList<>(Arrays.asList(createObj("1", "foo"), createObj("2", "bar"))));
        List<JsonObject> result = dataLayer.getAll(info);
        assertEquals(2, result.size());
    }

    @Test
    public void testEraseEmptiesList() {
        stub.fileData.put("test.json:items", new ArrayList<>(Arrays.asList(createObj("1", "foo"))));
        dataLayer.erase(info);
        assertNotNull(stub.lastWriteList);
        assertTrue(stub.lastWriteList.isEmpty());
    }

    // --- Additional edge and negative tests ---

    @Test
    public void testAddNullObjectThrowsAssertionError() {
        try {
            dataLayer.add(null, info);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void testAddNullInfoThrowsAssertionError() {
        try {
            dataLayer.add(createObj("1", "foo"), null);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void testModifyNullObjectThrowsAssertionError() {
        try {
            dataLayer.modify(null, info);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void testModifyNullInfoThrowsAssertionError() {
        try {
            dataLayer.modify(createObj("1", "foo"), null);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void testDeleteNullInfoThrowsAssertionError() {
        try {
            dataLayer.delete(null);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void testGetNullInfoThrowsAssertionError() {
        try {
            dataLayer.get(null);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void testExistsNullInfoThrowsAssertionError() {
        try {
            dataLayer.exists(null);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void testCheckFileExistanceNullInfoThrowsAssertionError() {
        try {
            dataLayer.checkFileExistance(null);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void testCreateJSONEmptyFileNullInfoThrowsAssertionError() {
        try {
            dataLayer.createJSONEmptyFile(null);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void testGetListNullInfoThrowsAssertionError() {
        try {
            dataLayer.getList(null);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void testGetAllNullInfoThrowsAssertionError() {
        try {
            dataLayer.getAll(null);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    public void testEraseNullInfoThrowsAssertionError() {
        try {
            dataLayer.erase(null);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            // expected
        }
    }

    // --- New edge cases ---

    @Test
    public void testAddWithEmptyList() {
        dataLayer.add(createObj("3", "baz"), info);
        assertEquals(1, stub.lastWriteList.size());
        assertEquals("3", stub.lastWriteList.get(0).get("id").getAsString());
    }

    @Test
    public void testModifyWithEmptyListReturnsFalse() {
        boolean result = dataLayer.modify(createObj("1", "foo"), info);
        assertFalse(result);
    }

    @Test
    public void testDeleteWithEmptyList() {
        dataLayer.delete(info);
        assertNotNull(stub.lastWriteList);
        assertTrue(stub.lastWriteList.isEmpty());
    }

    @Test
    public void testGetWithEmptyListReturnsNull() {
        JsonObject result = dataLayer.get(info);
        assertNull(result);
    }

    @Test
    public void testGetListWithEmptyListReturnsNull() {
        List<JsonObject> result = dataLayer.getList(info);
        assertNull(result);
    }

    @Test
    public void testGetAllWithEmptyListReturnsEmptyList() {
        List<JsonObject> result = dataLayer.getAll(info);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Helper
    private JsonObject createObj(String id, String name) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        return obj;
    }
}
