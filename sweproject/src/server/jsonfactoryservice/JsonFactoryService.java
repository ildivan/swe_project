package server.jsonfactoryservice;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import server.gsonfactoryservice.GsonFactoryService;
import server.gsonfactoryservice.IGsonFactory;


public class JsonFactoryService implements IJsonFactoryService{
    private final IGsonFactory gsonFactoryService = new GsonFactoryService();
    private final Gson gson = gsonFactoryService.getGson();

    public <T> JsonObject createJson(T object){
        return gson.toJsonTree(object).getAsJsonObject();
    }

    public <T> T createObject(JsonObject json, Class<T> c){
        assert json != null;
        assert c != null;

        try {
            return gson.fromJson(json, c);
        } catch (JsonSyntaxException e) {
            assert false: "Json syntax error: " + e.getMessage();
            System.out.println(e.getMessage());
            return null;
        }
    }

    public <T> List<T> createObjectList(List<JsonObject> jsonObjects, Class<T> c){
        List<T> result = new ArrayList<>();

        for (JsonObject jsonObject : jsonObjects) {
            T obj = gson.fromJson(jsonObject, c);
            result.add(obj);
        }

        assert (jsonObjects.size() == result.size());
        assert (jsonObjects.stream().noneMatch(Objects::isNull));
        return result;
    }
}

