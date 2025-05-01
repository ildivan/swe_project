package server.messages;

public class IOStringMessage {
    private String text;
    private boolean requiresResponse;

    public IOStringMessage(String text, boolean requiresResponse) {
        this.text = text;
        this.requiresResponse = requiresResponse;
    }

    // Getters
    public String getText() {
        return text;
    }

    public boolean getIfRequiresResponse() {
        return requiresResponse;
    }

    // Setters
    public void setText(String text) {
        this.text = text;
    }

    public void setRequiresResponse(boolean requiresResponse) {
        this.requiresResponse = requiresResponse;
    }
}