package backend.server.domainlevel.domainmanagers;

import java.util.List;

import com.google.gson.JsonObject;

import backend.server.domainlevel.Manager;

public class MonthlyPlanManager implements Manager{

     //TODO
    /*
     * 1. metodo per prendere la data di sistema (potrà essere avviato solo il 16 di ogni mese) e
     * popolare la map con le date di ogni giono
     * 
     * 2. metodo per settare i gionri preclusi
     * 
     * 3. metodo per creare il monthly plan (che chiama per ogni gionro non precluso un daily plan manager
     * che crea il piano di ogni giorno)
     */

    @Override
    public void add(JsonObject data) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public void remove(JsonObject data) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public void update(JsonObject data) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public JsonObject get(String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public String getAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }

    @Override
    public boolean exists(String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'exists'");
    }

    @Override
    public boolean checkIfThereIsSomethingWithCondition() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkIfThereIsSomethingWithCondition'");
    }

    @Override
    public List<Object> getCustomList() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCustomList'");
    }
    
}
