package backend.server.genericservices.DataLayer;

import com.google.gson.JsonObject;

public interface DataLayer {
    public void add(DataContainer dataContainer);
    public boolean modify(DataContainer dataContainer);
    public void delete(DataContainer dataContainer);
    public JsonObject get(DataContainer dataContainer);
    public boolean exists(DataContainer dataContainer);

}
