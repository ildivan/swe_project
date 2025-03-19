package backend.server;

public class Configs {
    private boolean userConfigured;
    private String areaOfIntrest;
    private Integer maxSubscriptions;

    public Configs() {
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


}
