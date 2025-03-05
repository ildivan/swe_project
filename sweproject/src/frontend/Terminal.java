package frontend;

import java.io.*;

import java.net.Socket;
import java.net.UnknownHostException;

import backend.server.json.Message;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Terminal {

    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 5001;
        Gson gson = new Gson();

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
}


