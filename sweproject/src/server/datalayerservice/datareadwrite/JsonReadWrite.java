package server.datalayerservice.datareadwrite;

import com.google.gson.*;

import server.authservice.User;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.gsonfactoryservice.GsonFactoryService;
import server.gsonfactoryservice.IGsonFactory;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class JsonReadWrite implements IJsonReadWrite {
    
    private final IGsonFactory gsonFactoryService = new GsonFactoryService();
    private final Gson gson = gsonFactoryService.getGson();
    private final IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();
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
            assert !list.isEmpty();
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
        Path path = Paths.get(filePath);
        assert Files.exists(path): "File path does not exist";
        JsonObject emptyJson = new JsonObject();
    
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(emptyJson, writer);
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
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
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> {
                return new JsonPrimitive(src.toString()); // Format: "2025-04-01"
            })
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString()))
            .create();
        IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();

        
        //riga per creare nuovo utente
        User user = new User("CT2", "temp_p2", "configuratore");

        //riga per creare monthly config
        // MonthlyConfig monthlyConfig = new MonthlyConfig(LocalDate.of(2025, 4, 23), false, new HashSet<LocalDate>());
        
        //salvare l'oggetto creato
        String StringJO;
        StringJO = gson.toJson(user);
        JsonObject JO = gson.fromJson(StringJO, JsonObject.class);

        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath("JF/users.json");
        locInfo.setMemberName("users");
       

        
        dataLayer.add(JO, locInfo);

    }
}

