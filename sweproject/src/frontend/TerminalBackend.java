package frontend;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import com.google.gson.Gson;

import server.utils.*;

public class TerminalBackend extends Terminal {
    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";
    private int port;
    private String hostname;
    
    public TerminalBackend(String hostname) {
        this.hostname = hostname;
        this.port = 6001; // Default port, can be changed as needed
    }

    public void run(){
        Gson gson = FrontEndUtils.buildGson();

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
                if (msg.text.equals(CLEAR)) {
                    FrontEndUtils.clearConsole();
                }else{
                    if (msg.text.equals(SPACE)){
                        FrontEndUtils.spaceConsole();
                    }else{
                        System.out.println(msg.text);
                    }
                    
                }
                

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
        
        FrontEndUtils.clearConsole();

        Terminal terminal = new TerminalBackend("localhost");
            
        terminal.run();
        
    }
}


