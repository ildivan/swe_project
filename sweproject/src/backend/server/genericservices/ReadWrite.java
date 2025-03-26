package backend.server.genericservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;

public abstract class ReadWrite {

    protected static BufferedReader reader;
    protected static PrintWriter writer;
    protected static final Gson gson = new Gson();
    
    public static void write(String message, boolean responseRequired) {
        writer.println(gson.toJson(new Message(message, responseRequired)));
    }

    public static String read() throws IOException {
        return reader.readLine();
    }

    /**
     * ask the user if he wants to continue with the operation
     * @param message the operation the user wants to continue
     * @return
     */
    protected static boolean continueChoice(String message) {
        write(String.format("\nProseguire con %s? (s/n)", message),true);
        String choice = "";
        try {
            choice = read();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(choice.equals("n")){
            return false;
        }
        return true;
    }

}