package backend.server.genericservices.DataLayer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class JSONUtil {
    private static Gson gson = new Gson();

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
}
