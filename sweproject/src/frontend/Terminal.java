package frontend;

import java.io.*;

import java.net.Socket;
import java.net.UnknownHostException;

import com.google.gson.Gson;

import backend.server.json.objects.Message;

public class Terminal {

    private final int port;
    private final String hostname;

    public Terminal(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }

    public void run(){
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


