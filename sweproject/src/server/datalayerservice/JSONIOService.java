package server.datalayerservice;

import com.google.gson.*;

import server.GsonFactoryService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyConfig;
import server.objects.interfaceforservices.IActionService;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.lang.reflect.Type;
//DA VERIFICARE SE è DAVVERO UN CONTROLLER
public class JSONIOService {
    
    private static final Gson gson = (Gson) GsonFactoryService.Service.GET_GSON.start();
    // Funzione per leggere il file JSON e ottenere la lista degli oggetti serializzati

    public enum Service {
        READ_FROM_FILE((params) -> JSONIOService.readFromFile((String) params[0], (String) params[1])),
        WRITE_TO_FILE((params) -> JSONIOService.writeToFile((String) params[0], (List<JsonObject>) params[1], (String) params[2])),
        CREATE_JSON_EMPTY_FILE((params) -> JSONIOService.createJSONEmptyFile((String) params[0]));

        private IActionService<?> service;

        Service(IActionService<?> service) {
            this.service = service;
        }

        public Object start(Object... params) {
            return service.apply(params);
        }
    }


    private static synchronized List<JsonObject> readFromFile(String filePath, String memberName) {
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
    private static synchronized Boolean writeToFile(String filePath, List<JsonObject> list, String memberName) {
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
            return null;
        }
        return null;//fake return
    }

    private static synchronized boolean createJSONEmptyFile(String path) {
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
         DataLayer DataLayer = new JSONDataManager(gson);

        // User user = new User("CT3", "temp_p3", "configuratore");
        MonthlyConfig monthlyConfig = new MonthlyConfig(LocalDate.of(2025, 4, 23), false, new HashSet<LocalDate>());
        
        String StringJO = new String();
        StringJO = gson.toJson(monthlyConfig);
        JsonObject JO = gson.fromJson(StringJO, JsonObject.class);

        DataContainer dataContainer = new DataContainer("sweproject/JF/monthlyConfigs.json", JO, "mc");
        
        DataLayer.add(dataContainer);

    }
}

