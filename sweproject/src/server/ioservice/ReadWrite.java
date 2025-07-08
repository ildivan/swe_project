package server.ioservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;

import server.utils.Message;

public abstract class ReadWrite {

    protected static final ThreadLocal<BufferedReader> reader = new ThreadLocal<>();
    protected static final ThreadLocal<PrintWriter> writer = new ThreadLocal<>();
    protected static final Gson gson = new Gson();
    
    public static void setConnection(Socket socket) throws IOException {
        reader.set(new BufferedReader(new InputStreamReader(socket.getInputStream())));
        writer.set(new PrintWriter(socket.getOutputStream(), true));
    }

    protected static void write(String message, boolean responseRequired) {
        writer.get().println(gson.toJson(new Message(message, responseRequired)));

    }

    protected static String read() throws IOException {
        return reader.get().readLine();
    }

    /**
     * ask the user if he wants to continue with the operation
     * @param message the operation the user wants to continue
     * @return
     */
    protected static boolean continueChoice(String message) {
        write(String.format("\nProseguire con %s? (s/n)", message), true);
        String choice = "";
        try {
            choice = read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return !"n".equals(choice);
    }
}
