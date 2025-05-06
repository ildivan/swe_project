package server.jsonfactoryservice;

import java.util.List;

import com.google.gson.JsonObject;

public interface IJsonFactoryService {
    public <T> JsonObject createJson(T object);
    public <T> T createObject(JsonObject json, Class<T> c);
    public <T> List<T> createObjectList(List<JsonObject> jsonObjects, Class<T> c);
}
