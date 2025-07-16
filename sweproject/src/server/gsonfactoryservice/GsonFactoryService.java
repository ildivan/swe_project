package server.gsonfactoryservice;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

/**
 * classe per costruire un Gson 
 */
public class GsonFactoryService implements IGsonFactory{

    /**
     * Metodo che crea un Gson e lo ritorna
     * @return
     */
    public Gson getGson(){
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Type localDateBooleanMapType = new TypeToken<Map<LocalDate, Boolean>>(){}.getType();

        Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .enableComplexMapKeySerialization()
        // LocalDate
        .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.format(dateFormatter)); // Format: dd-mm-yyyy
            }
        })
        .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return LocalDate.parse(json.getAsString(), dateFormatter);
            }
        })

        // LocalTime
        .registerTypeAdapter(LocalTime.class, new JsonSerializer<LocalTime>() {
            @Override
            public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.format(timeFormatter)); // Format: HH:mm
            }
        })
        .registerTypeAdapter(LocalTime.class, new JsonDeserializer<LocalTime>() {
            @Override
            public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return LocalTime.parse(json.getAsString(), timeFormatter);
            }
        })

        // Serializer per Map<LocalDate, Boolean>
    .registerTypeAdapter(localDateBooleanMapType, new JsonSerializer<Map<LocalDate, Boolean>>() {
        @Override
        public JsonElement serialize(Map<LocalDate, Boolean> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            for (Map.Entry<LocalDate, Boolean> entry : src.entrySet()) {
                String key = entry.getKey().format(formatter);
                jsonObject.addProperty(key, entry.getValue());
            }
            return jsonObject;
        }
    })

    // Deserializer per Map<LocalDate, Boolean>
    .registerTypeAdapter(localDateBooleanMapType, new JsonDeserializer<Map<LocalDate, Boolean>>() {
        @Override
        public Map<LocalDate, Boolean> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Map<LocalDate, Boolean> map = new HashMap<>();
            JsonObject jsonObject = json.getAsJsonObject();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                LocalDate key = LocalDate.parse(entry.getKey(), formatter);
                Boolean value = entry.getValue().getAsBoolean();
                map.put(key, value);
            }
            return map;
        }
    })

        .create();

        return gson;
    }

}
