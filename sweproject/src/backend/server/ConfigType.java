package backend.server;

public enum ConfigType {
    NORMAL("normalFunctionConfigs"),
    NO_FIRST_CONFIG("noFirstConfig");

    private final String value;

    ConfigType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
