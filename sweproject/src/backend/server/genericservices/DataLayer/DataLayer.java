package backend.server.genericservices.DataLayer;

import java.util.List;

import com.google.gson.JsonObject;

public interface DataLayer {
    public void add(JSONDataContainer dataContainer);
    public boolean modify(JSONDataContainer dataContainer);
    public void delete(JSONDataContainer dataContainer);
    public JsonObject get(JSONDataContainer dataContainer);
    public boolean exists(JSONDataContainer dataContainer);
    public boolean checkFileExistance(JSONDataContainer dataContainer);
    public void createJSONEmptyFile(JSONDataContainer dataContainer);
    public List<JsonObject> getList(JSONDataContainer dataContainer);


}
