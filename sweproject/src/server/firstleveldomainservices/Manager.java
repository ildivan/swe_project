package server.firstleveldomainservices;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface Manager {
    public void add(JsonObject data);
    public void remove(JsonObject data, String key);
    public void update(JsonObject data, String key);
    public JsonObject get(String key);
    public boolean exists(String key);
    public boolean checkIfThereIsSomethingWithCondition();
    public List<?> getCustomList();
    public String getAll();

}
