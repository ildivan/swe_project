package server.utils;

public enum ConfigType {
    NORMAL("normalFunctionConfigs"),
    NO_FIRST_CONFIG("noFirstConfig"),
    TEST("testConfig");

    private final String value;

    ConfigType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
