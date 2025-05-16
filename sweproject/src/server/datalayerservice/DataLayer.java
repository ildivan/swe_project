package server.datalayerservice;

import java.util.List;

import com.google.gson.JsonObject;

public interface DataLayer {
    public void add(DataContainer dataContainer);
    public boolean modify(DataContainer dataContainer);
    public void delete(DataContainer dataContainer);
    public JsonObject get(DataContainer dataContainer);
    public boolean exists(DataContainer dataContainer);
    public boolean checkFileExistance(DataContainer dataContainer);
    public void createJSONEmptyFile(DataContainer dataContainer);
    public List<JsonObject> getList(DataContainer dataContainer);
    public List<JsonObject> getAll(DataContainer dataContainer);


}
