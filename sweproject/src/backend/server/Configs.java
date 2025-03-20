package backend.server;

public class Configs {
    private String configType;
    private boolean userConfigured;
    private String areaOfIntrest;
    private Integer maxSubscriptions;

    public Configs() {
        this.configType = "normalFunctionConfigs";
        this.userConfigured = false;
        this.areaOfIntrest = null;
        this.maxSubscriptions = null;
    }

    public boolean getUserConfigured() {
        return userConfigured;
    }

    public void setUserConfigured(boolean userConfigured) {
        this.userConfigured = userConfigured;
    }

    public String getAreaOfIntrest() {
        return areaOfIntrest;
    }

    public void setAreaOfIntrest(String areaOfIntrest) {
        this.areaOfIntrest = areaOfIntrest;
    }

    public Integer getMaxSubscriptions() {
        return maxSubscriptions;
    }

    public void setMaxSubscriptions(Integer maxSubscriptions) {
        this.maxSubscriptions = maxSubscriptions;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }


}
