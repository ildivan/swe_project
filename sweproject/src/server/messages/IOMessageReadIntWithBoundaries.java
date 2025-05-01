package server.messages;

public class IOMessageReadIntWithBoundaries {
    
    private String text;
    private boolean requiresResponse;
    private int min;
    private int max;

    public IOMessageReadIntWithBoundaries(String text, boolean requiresResponse, int min, int max) {
        this.text = text;
        this.requiresResponse = requiresResponse;
        this.min = min;
        this.max = max;
    }

    // Getters
    public String getText() {
        return text;
    }

    public boolean isRequiresResponse() {
        return requiresResponse;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    // Setters
    public void setText(String text) {
        this.text = text;
    }

    public void setRequiresResponse(boolean requiresResponse) {
        this.requiresResponse = requiresResponse;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
