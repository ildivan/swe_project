package server;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GsonFactoryService {

    public static Gson getGson(){
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Gson gson = new GsonBuilder()
        .setPrettyPrinting()

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

        .create();

        return gson;
    }

}
