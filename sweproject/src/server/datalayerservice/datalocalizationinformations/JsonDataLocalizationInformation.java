package server.datalayerservice.datalocalizationinformations;

public class JsonDataLocalizationInformation implements IDataLocalizationInformation {
    // informazioni per la localizzazione e la connessione al file JSON
    private String path;
    private String memberName;
    private String key; 
    private String keyDesc;

    /**
     * construcotor
     */
    public JsonDataLocalizationInformation() {
       super();
    }

    /**
     * method to get localization information
     * @return
     */
    @Override
    public JsonDataLocalizationInformation getDataLocalizationInformation(){
        return this;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
