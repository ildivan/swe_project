package backend.server.genericservices.DataLayer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class JSONUtil {
    private static Gson gson = new Gson();

    public static <T> JsonObject createJson(T object){
        return gson.toJsonTree(object).getAsJsonObject();
    }
}
