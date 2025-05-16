package server.objects;

public enum ServerConnectionPorts {
    CLIENT(5001),
    SERVER(6001);

    private final int code;

    ServerConnectionPorts(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
