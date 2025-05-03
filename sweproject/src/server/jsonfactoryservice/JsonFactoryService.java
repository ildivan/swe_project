package server.jsonfactoryservice;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import server.GsonFactoryService;

/**
 * STATICO perchè è un servizio che non deve cambiare implementazione
 */
public class JsonFactoryService {
    private static Gson gson = (Gson) GsonFactoryService.Service.GET_GSON.start();

    public static <T> JsonObject createJson(T object){
        return gson.toJsonTree(object).getAsJsonObject();
    }

    public static <T> T createObject(JsonObject json, Class<T> c){
        try {
            return (T) gson.fromJson(json, c);
        } catch (JsonSyntaxException e) {
            // Gestire l'errore, ad esempio restituendo null o lanciando una RuntimeException
            e.printStackTrace();
        return null;
        }
    }

    public static <T> List<T> createObjectList(List<JsonObject> jsonObjects, Class<T> c){
        List<T> result = new ArrayList<>();

        for (JsonObject jsonObject : jsonObjects) {
            T obj = gson.fromJson(jsonObject, c);
            result.add(obj);
        }
    
        return result;
    }
}

