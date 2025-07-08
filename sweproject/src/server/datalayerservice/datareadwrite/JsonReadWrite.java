package server.datalayerservice.datareadwrite;

import com.google.gson.*;
import server.gsonfactoryservice.GsonFactoryService;
import server.gsonfactoryservice.IGsonFactory;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class JsonReadWrite implements IJsonReadWrite {
    
    private final IGsonFactory gsonFactoryService = new GsonFactoryService();
    private final Gson gson = gsonFactoryService.getGson();
    // Funzione per leggere il file JSON e ottenere la lista degli oggetti serializzati

    public synchronized List<JsonObject> readFromFile(String filePath, String memberName) {
        Path path = Paths.get(filePath);
        assert Files.exists(path): "File path does not exist";
        assert !memberName.trim().isEmpty();

        if (!Files.exists(path)) {
            return null;
        }
    
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            JsonArray objectArray = json.getAsJsonArray(memberName);
    
            if (objectArray == null) {
                return new ArrayList<>();
            }
    
            List<JsonObject> list = new ArrayList<>();
            for (JsonElement elem : objectArray) {
                list.add(elem.getAsJsonObject());
            }
            return list;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    

    // Funzione per scrivere la lista degli oggetti serializzati nel file JSON
    public synchronized Boolean writeToFile(String filePath, List<JsonObject> list, String memberName) {
        Path path = Paths.get(filePath);
        assert Files.exists(path): "File path does not exist";
        assert !memberName.trim().isEmpty();

        JsonObject json = new JsonObject();
        JsonArray objectArray = new JsonArray();
        for (JsonObject user : list) {
            objectArray.add(user);
        }
        json.add(memberName, objectArray);

        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(json, writer);
            return true; // Successo
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public synchronized boolean createJSONEmptyFile(String filePath) {
        JsonObject emptyJson = new JsonObject();
    
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(emptyJson, writer);
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    
}

