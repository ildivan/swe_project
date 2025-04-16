package backend.server.genericservices.datalayer;
import com.google.gson.*;

import backend.server.domainlevel.User;
import backend.server.domainlevel.monthlydomain.MonthlyConfig;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.lang.reflect.Type;

public class JSONIOManager {
    private Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
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

    // Funzione per leggere il file JSON e ottenere la lista degli oggetti serializzati
    public synchronized List<JsonObject> readFromFile(String filePath, String memberName) {
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath))) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            JsonArray objectArray = json.getAsJsonArray(memberName);
            
            if(objectArray == null){
                return null;
            }

            List<JsonObject> list = new ArrayList<>();
            for (JsonElement elem : objectArray) {
                list.add(elem.getAsJsonObject());
            }
            return list;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Funzione per scrivere la lista degli oggetti serializzati nel file JSON
    public synchronized void writeToFile(String filePath, List<JsonObject> list, String memberName) {
        JsonObject json = new JsonObject();
        JsonArray objectArray = new JsonArray();
        for (JsonObject user : list) {
            objectArray.add(user);
        }
        json.add(memberName, objectArray);

        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean createJSONEmptyFile(String path) {
        Object emptyObject = new Object();

        System.out.println("Writing to: " + path);

        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(emptyObject, writer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * metodo per creare noi un configuratore con utente e password di default e altro
     * @param args
     */
    public static void main(String[] args){
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
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
         DataLayer DataLayer = new JSONDataManager();

        // User user = new User("CT3", "temp_p3", "configuratore");
        MonthlyConfig monthlyConfig = new MonthlyConfig(LocalDate.of(2025, 4, 23), false, new HashSet<LocalDate>());
        
        String StringJO = new String();
        StringJO = gson.toJson(monthlyConfig);
        JsonObject JO = gson.fromJson(StringJO, JsonObject.class);

        JSONDataContainer dataContainer = new JSONDataContainer("sweproject/JF/monthlyConfigs.json", JO, "mc");
        
        DataLayer.add(dataContainer);

    }
}

