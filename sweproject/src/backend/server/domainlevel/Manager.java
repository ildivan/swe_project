package backend.server.domainlevel;

import java.util.List;

import com.google.gson.JsonObject;

public interface Manager {
    public void add(JsonObject data);
    public void remove(JsonObject data);
    public void update(JsonObject data);
    public void get(JsonObject data);
    public void getAll(JsonObject data);
    public boolean exists(String key);
    public boolean checkIfThereIsSomethingWithCondition();
    public List<?> getCustomList();

}
