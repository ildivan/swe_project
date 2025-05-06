package server.jsonfactoryservice;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import server.gsonfactoryservice.GsonFactoryService;
import server.gsonfactoryservice.IGsonFactory;


public class JsonFactoryService implements IJsonFactoryService{
    private IGsonFactory gsonFactoryService = new GsonFactoryService();
    private final Gson gson = gsonFactoryService.getGson();

    public <T> JsonObject createJson(T object){
        return gson.toJsonTree(object).getAsJsonObject();
    }

    public <T> T createObject(JsonObject json, Class<T> c){
        try {
            return (T) gson.fromJson(json, c);
        } catch (JsonSyntaxException e) {
            // Gestire l'errore, ad esempio restituendo null o lanciando una RuntimeException
            e.printStackTrace();
        return null;
        }
    }

    public <T> List<T> createObjectList(List<JsonObject> jsonObjects, Class<T> c){
        List<T> result = new ArrayList<>();

        for (JsonObject jsonObject : jsonObjects) {
            T obj = gson.fromJson(jsonObject, c);
            result.add(obj);
        }
    
        return result;
    }
}

