package frontend;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import com.google.gson.Gson;
import server.messages.IOStringMessage;

public class Terminal {
    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";
    private final int port;
    private final String hostname;
    
    public Terminal(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
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
                //è un gson perche in formatterCommandLineView il metodo write manda un json
                String JSONMessage = reader.readLine();
                if (JSONMessage == null) break;
                IOStringMessage msg = gson.fromJson(JSONMessage, IOStringMessage.class);
                if (msg.getText().equals(CLEAR)) {
                    FrontEndUtils.clearConsole();
                }else{
                    if (msg.getText().equals(SPACE)){
                        FrontEndUtils.spaceConsole();
                    }else{
                        System.out.println(msg.getText());
                    }
                    
                }
                

                if (msg.getIfRequiresResponse()) {
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
        int port = 0;
        try {
            FrontEndUtils.clearConsole();
            
            do{ 
                System.out.println("Port: ");
                port = Integer.parseInt(consoleReader.readLine());
                if(port != FrontEndUtils.getClientPort() && port != FrontEndUtils.getServerPort()){
                    System.out.println("Port not valid (6001 for server or 5001 for user)");
                }
            }while(port != FrontEndUtils.getClientPort() && port != FrontEndUtils.getServerPort());
           
            Terminal terminal = new Terminal("localhost", port);
            
            terminal.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


