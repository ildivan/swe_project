package server.datalayerservice.datareadwrite;

import com.google.gson.*;

import server.GsonFactoryService;
import server.authservice.User;
import server.datalayerservice.DataLayerDispatcherService;
import server.datalayerservice.JsonDataLocalizationInformation;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyConfig;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.lang.reflect.Type;

public class JsonReadWrite implements IJsonReadWrite {
    
    private static final Gson gson = (Gson) GsonFactoryService.Service.GET_GSON.start();
    // Funzione per leggere il file JSON e ottenere la lista degli oggetti serializzati

    public synchronized List<JsonObject> readFromFile(String filePath, String memberName) {
        Path path = Paths.get(filePath);
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
            e.printStackTrace();
            return null;
        }
    }
    

    // Funzione per scrivere la lista degli oggetti serializzati nel file JSON
    public synchronized Boolean writeToFile(String filePath, List<JsonObject> list, String memberName) {
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
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean createJSONEmptyFile(String path) {
        JsonObject emptyJson = new JsonObject();
    
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(emptyJson, writer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    

    /**
     * metodo per creare noi un configuratore con utente e password di default e altro
     * SOLO PER TEST E CREAZOINE DEGLI UTENTI, NON VIENE UTILIZZATO DALL'APPLICAIZONE
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
        

        
        //riga per creare nuovo utente
        User user = new User("CT2", "temp_p2", "configuratore");

        //riga per creare monthly config
        // MonthlyConfig monthlyConfig = new MonthlyConfig(LocalDate.of(2025, 4, 23), false, new HashSet<LocalDate>());
        
        //salvare l'oggetto creato
        String StringJO = new String();
        StringJO = gson.toJson(user);
        JsonObject JO = gson.fromJson(StringJO, JsonObject.class);

        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath("JF/users.json");
        locInfo.setMemberName("users");
       

        
        DataLayerDispatcherService.start(locInfo, layer->layer.add(JO,locInfo));

    }
}

