package backend.server.json;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.*;

public class JSONDataManager {
    //private static final String PATH = "sweproject/jsonFiles/users.json";
    private static final JSONIOManager fileManager = new JSONIOManager();
        
    
    public JSONDataManager() {
        super();
    }
    
    /**
     * 
     * @param path percorso del file
     * @param JO oggetto JO da aggiungere es nuovo utente
     * @param memberName nome dell'array contenuto nel json
     */
    public static void add(String path, JsonObject JO, String memberName) {


        if(!checkFileExistance(path)){
            fileManager.createJSONEmptyFile(path);};
            
        List<JsonObject> list = fileManager.readFromFile(path, memberName);
        
        if(list == null){
            list = new ArrayList<>();
        }
        // Aggiungi l'utente alla lista e salva
        list.add(JO);
        fileManager.writeToFile(path, list, memberName);
    }

    /**
     * controllo esistenza del file json su cui operare se non c'è verra poi creato nel metodo chiamante
     * @param path
     * @return
     */
    private static boolean checkFileExistance(String path) {
        File file = new File(path);

        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 
     * @param key parametro che identifica unicamente una istanza di utente/luogo etc
     * @param keyDesc come si chiama la key tipo nome per utente, nomeLuogo per luogo etc
     * @param JO
     * @param path
     * @param memberName
     */
    public static void modify(String key, String keyDesc, JsonObject JO, String path, String memberName) {
        
        if(!checkFileExistance(path)){
            fileManager.createJSONEmptyFile(path);};

        List<JsonObject> list = fileManager.readFromFile(path, memberName);

        for (JsonObject o : list) {
            if (o.get(keyDesc).getAsString().equalsIgnoreCase(key)) {
                list.set(list.indexOf(o), JO);
                fileManager.writeToFile(path, list, memberName);
                return;
            }
        }
       // System.out.println("Oggetto non trovato.");
    }

    // Elimina
    public static void delete(String key, String keyDesc, String path, String memberName) {
        
        if(!checkFileExistance(path)){
            fileManager.createJSONEmptyFile(path);};

        List<JsonObject> list = fileManager.readFromFile(path, memberName);

        for (JsonObject o : list) {
            if (o.get(keyDesc).getAsString().equalsIgnoreCase(key)) {
                list.remove(o);
                fileManager.writeToFile(path, list, memberName);
                return;
            }
        }
       //System.out.println("Oggetto non trovato.");
    }

    // Recupera un oggetto
    public static JsonObject get(String key, String keyDesc, String path, String memberName) {
        
        if(!checkFileExistance(path)){
            fileManager.createJSONEmptyFile(path);};

        List<JsonObject> list = fileManager.readFromFile(path, memberName);

        for (JsonObject o : list) {
            if (o.get(keyDesc).getAsString().equalsIgnoreCase(key)) {
                return o;
            }
        }
        return null;
    }

    // Verifica se un oggetto esiste
    public static boolean exists(String key, String keyDesc, String path, String memberName) {
        
        if(!checkFileExistance(path)){
            fileManager.createJSONEmptyFile(path);};
            
        JsonObject o = get(key, keyDesc, path, memberName);
        return o != null;

    }

}

