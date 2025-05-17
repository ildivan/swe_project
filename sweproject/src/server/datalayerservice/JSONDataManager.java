package server.datalayerservice;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.*;

public class JSONDataManager implements DataLayer {
    //private static final String PATH = "sweproject/jsonFiles/users.json";

    private static Gson gson;
        
    
    public JSONDataManager(Gson gson) {
        super();
        this.gson = gson;
    }
    
    /**
     * 
     * @param path percorso del file
     * @param JO oggetto JO da aggiungere es nuovo utente
     * @param memberName nome dell'array contenuto nel json
     */
    @Override
    public void add(DataContainer dataContainer) {


        if(!checkFileExistance(dataContainer)){
            JSONIOService.Service.CREATE_JSON_EMPTY_FILE.start(dataContainer.getPath());};
            
        List<JsonObject> list = (List<JsonObject>) JSONIOService.Service.READ_FROM_FILE.start(dataContainer.getPath(), dataContainer.getMemberName());
        
        if(list == null){
            list = new ArrayList<>();
        }
        // Aggiungi l'utente alla lista e salva
        list.add(dataContainer.getJO());
        JSONIOService.Service.WRITE_TO_FILE.start(dataContainer.getPath(), list, dataContainer.getMemberName());
    }

    /**
     * controllo esistenza del file json su cui operare se non c'è verra poi creato nel metodo chiamante
     * @param path
     * @return
     */
    
    public boolean checkFileExistance(DataContainer dataContainer) {
        File file = new File(dataContainer.getPath());

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
    @Override
    public boolean modify(DataContainer dataContainer) {
        
        if(!checkFileExistance(dataContainer)){
            JSONIOService.Service.CREATE_JSON_EMPTY_FILE.start(dataContainer.getPath());};

        List<JsonObject> list = (List<JsonObject>)JSONIOService.Service.READ_FROM_FILE.start(dataContainer.getPath(), dataContainer.getMemberName());

        for (JsonObject o : list) {
            if (o.get(dataContainer.getKeyDesc()).getAsString().equals(dataContainer.getKey())) {
                list.set(list.indexOf(o), dataContainer.getJO());
                JSONIOService.Service.WRITE_TO_FILE.start(dataContainer.getPath(), list, dataContainer.getMemberName());
                return true;
            }
        }
       return false;
    }

    // Elimina
    @Override
    public void delete(DataContainer dataContainer) {
        
        if(!checkFileExistance(dataContainer)){
            JSONIOService.Service.CREATE_JSON_EMPTY_FILE.start(dataContainer.getPath());};

        List<JsonObject> list = (List<JsonObject>)JSONIOService.Service.READ_FROM_FILE.start(dataContainer.getPath(), dataContainer.getMemberName());

        for (JsonObject o : list) {
            if (o.get(dataContainer.getKeyDesc()).getAsString().equals(dataContainer.getKey())) {
                list.remove(o);
                JSONIOService.Service.WRITE_TO_FILE.start(dataContainer.getPath(), list, dataContainer.getMemberName());
                return;
            }
        }
       //System.out.println("Oggetto non trovato.");
    }

    /**
     * path, membername, key e keydesc
     */
    @Override
    public JsonObject get(DataContainer dataContainer) {
        
        if(!checkFileExistance(dataContainer)){
            JSONIOService.Service.CREATE_JSON_EMPTY_FILE.start(dataContainer.getPath());};

        List<JsonObject> list = (List<JsonObject>)JSONIOService.Service.READ_FROM_FILE.start(dataContainer.getPath(), dataContainer.getMemberName());

        for (JsonObject o : list) {
            if(o.get(dataContainer.getKeyDesc()) == null){
                return null;
            }
            if (o.get(dataContainer.getKeyDesc()).getAsString().equals(dataContainer.getKey())) {
                return o;
            }
        }
        return null;
    }

    /**
     * verifica se un oggetto esiste
     */
    @Override
    public boolean exists(DataContainer dataContainer) {
        
        if(!checkFileExistance(dataContainer)){
            JSONIOService.Service.CREATE_JSON_EMPTY_FILE.start(dataContainer.getPath());};
        
        DataContainer DC = new DataContainer(dataContainer.getPath(), dataContainer.getMemberName(), dataContainer.getKey(), dataContainer.getKeyDesc());
        JsonObject o = get(DC);
        return o != null;

    }

    @Override
    public void createJSONEmptyFile(DataContainer dataContainer) {
        JSONIOService.Service.CREATE_JSON_EMPTY_FILE.start(dataContainer.getPath());
    }

    public List<JsonObject> getList(DataContainer dataContainer) {

        if (!checkFileExistance(dataContainer)) {
            JSONIOService.Service.CREATE_JSON_EMPTY_FILE.start(dataContainer.getPath());
        }
    
        List<JsonObject> list = (List<JsonObject>)JSONIOService.Service.READ_FROM_FILE.start(dataContainer.getPath(), dataContainer.getMemberName());
        List<JsonObject> result = new ArrayList<>();
    
        for (JsonObject o : list) {
          
            if (o.get(dataContainer.getKeyDesc()).getAsString().equals(dataContainer.getKey())) {
                result.add(o);  
            }
        }
    
        return result.isEmpty() ? null : result; 
    }
    
    /**
     * mettere path e membername
     * @param dataContainer
     * @return
     */
    @Override
    public List<JsonObject> getAll(DataContainer dataContainer){
        return (List<JsonObject>)JSONIOService.Service.READ_FROM_FILE.start(dataContainer.getPath(), dataContainer.getMemberName());
    }

}

