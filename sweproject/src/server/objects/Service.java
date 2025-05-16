package server.objects;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

public abstract class Service <T> extends ReadWrite{
   
    protected final Socket socket;

    public Service(Socket socket) {
        this.socket = socket;
       
    }

    public T run() {
        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            return applyLogic();
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    protected abstract T applyLogic() throws IOException;
}