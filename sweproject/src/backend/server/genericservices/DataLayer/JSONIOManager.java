package backend.server.genericservices.DataLayer;
import com.google.gson.*;

import backend.server.domainlevel.User;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class JSONIOManager {
    private final Gson gson = new Gson();

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
            return new ArrayList<>();
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

        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(emptyObject, writer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * metodo per creare noi un configuratore con utente e password di default
     * @param args
     */
    public static void main(String[] args){
        Gson gson = new Gson();
        DataLayer DataLayer = new JSONDataManager();
        User user = new User("ConfiguratoreTest2", "final_p1", "configuratore");
        String StringJO = new String();
        StringJO = gson.toJson(user);
        JsonObject JO = gson.fromJson(StringJO, JsonObject.class);

        DataContainer dataContainer = new DataContainer("sweproject/JsonFiles/users.json", JO, "users");
        
        DataLayer.add(dataContainer);

    }
}

