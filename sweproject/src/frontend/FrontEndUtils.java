package frontend;

import java.lang.reflect.Type;
import java.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class FrontEndUtils {

    private enum ConnectionType {
        CLIENT(5001),
        SERVER(6001);
    
        private final int code;
    
        ConnectionType(int code) {
            this.code = code;
        }
    
        public int getCode() {
            return code;
        }
    }

    public static int getClientPort() {
        return ConnectionType.CLIENT.getCode();
    }
    public static int getServerPort() {
        return ConnectionType.SERVER.getCode();
    }

    /*
     * simula la pulizia della console per avere un effetto di refresh
     */
    public static void clearConsole() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    /*
     * simula spazio nella console
     */
    public static void spaceConsole() {
        for (int i = 0; i < 5; i++) {
            System.out.println();
        }
    }

    /*
     * build the gson
     */
    public static Gson buildGson(){
        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.toString()); // Format: "2025-04-01"
            }
        })
        .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return LocalDate.parse(json.getAsString());
            }
        })
        .create();
        return gson; 
    }


}
