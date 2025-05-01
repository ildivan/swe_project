package server.messages;

import com.google.gson.JsonObject;

public class Message {
    private JsonObject jsonObject;
    private Class<?> containedClass;

    public Message(JsonObject jsonObject, Class<?> containedClass) {
        this.jsonObject = jsonObject;
        this.containedClass = containedClass;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public Class<?> getContainedClass() {
        return containedClass;
    }

    public void setContainedClass(Class<?> containedClass) {
        this.containedClass = containedClass;
    }
}
