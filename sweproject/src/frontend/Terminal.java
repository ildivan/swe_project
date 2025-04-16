package frontend;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import backend.server.genericservices.Message;

public class Terminal {

    private final int port;
    private final String hostname;

    public Terminal(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }

    public void run(){
        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString()); // Format: "2025-04-01"
        }
    })
    .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDate.parse(json.getAsString());
        }
    })
    .create();

        try (Socket socket = new Socket(hostname, port);
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true);
             InputStream input = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {

            do {
                String JSONMessage = reader.readLine();
                if (JSONMessage == null) break;
                Message msg = gson.fromJson(JSONMessage, Message.class);
                System.out.println(msg.text);

                if (msg.requiresResponse) {
                    System.out.print(">>");
                    String text = consoleReader.readLine();
                    writer.println(text);
                }
            } while (true);

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {

        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("Port: ");
            int port = Integer.parseInt(consoleReader.readLine());
           
            Terminal terminal = new Terminal("localhost", port);
            
            terminal.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


