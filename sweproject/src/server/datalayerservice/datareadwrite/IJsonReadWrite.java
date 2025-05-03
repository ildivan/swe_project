package server.datalayerservice.datareadwrite;

import java.util.List;

import com.google.gson.JsonObject;

public interface IJsonReadWrite {
    /*
     * necessitano tutti di essere synchronized per evitare che più thread possano
     * interagire con i file contemporaneamente
     */
    public List<JsonObject> readFromFile(String filePath, String memberName); 
    public Boolean writeToFile(String filePath, List<JsonObject> list, String memberName);
    public boolean createJSONEmptyFile(String path);
}
