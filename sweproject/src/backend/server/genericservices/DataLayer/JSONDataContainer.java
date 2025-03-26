package backend.server.genericservices.DataLayer;

import com.google.gson.JsonObject;

public class JSONDataContainer {
    private String path;
    private JsonObject JO;
    private String memberName;
    private String key; 
    private String keyDesc;

    public JSONDataContainer(String path, JsonObject JO, String memberName, String key, String keyDesc) {
        this.path = path;
        this.JO = JO;
        this.memberName = memberName;
        this.key = key;
        this.keyDesc = keyDesc;
    }

    public JSONDataContainer(String path, String memberName, String key, String keyDesc) {
        this.path = path;
        this.memberName = memberName;
        this.key = key;
        this.keyDesc = keyDesc;
    }

    public JSONDataContainer(String path, JsonObject JO, String memberName) {
        this.path = path;
        this.JO = JO;
        this.memberName = memberName;
    }

    public JSONDataContainer(String path, String memberName) {
        this.path = path;
        this.memberName = memberName;
    }

    public JSONDataContainer(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public JsonObject getJO() {
        return JO;
    }

    public void setJO(JsonObject JO) {
        this.JO = JO;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKeyDesc() {
        return keyDesc;
    }

    public void setKeyDesc(String keyDesc) {
        this.keyDesc = keyDesc;
    }
    

}
