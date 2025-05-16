package server.objects;

public class Message {
    public final String text;
    public final boolean requiresResponse;

    public Message(String text, boolean requiresResponse) {
        this.text = text;
        this.requiresResponse = requiresResponse;
    }
}
