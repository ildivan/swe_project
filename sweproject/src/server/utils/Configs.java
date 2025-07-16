package server.utils;

public class Configs {
    private String configType;
    private boolean userConfigured;
    private String areaOfIntrest;
    private Integer maxSubscriptions;
    private boolean placesFirtsConfigured;
    private boolean activitiesFirtsConfigured;
    private boolean firstPlanConfigured;

    public Configs() {
        this.configType = "normalFunctionConfigs";
        this.userConfigured = false;
        this.areaOfIntrest = null;
        this.maxSubscriptions = null;
        this.placesFirtsConfigured = false;
        this.activitiesFirtsConfigured = false;
        this.firstPlanConfigured = false;
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

    public boolean getPlacesFirtsConfigured() {
        return placesFirtsConfigured;
    }

    public void setPlacesFirtsConfigured(boolean placesFirtsConfigured) {
        this.placesFirtsConfigured = placesFirtsConfigured;
    }

    public boolean getActivitiesFirtsConfigured() {
        return activitiesFirtsConfigured;
    }

    public void setActivitiesFirtsConfigured(boolean activitiesFirtsConfigured) {
        this.activitiesFirtsConfigured = activitiesFirtsConfigured;
    }

    public boolean getFirstPlanConfigured() {
        return firstPlanConfigured;
    }

    public void setFirstPlanConfigured(boolean firstPlanConfigured) {
        this.firstPlanConfigured = firstPlanConfigured;
    }


}
