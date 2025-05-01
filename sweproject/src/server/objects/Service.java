package server.objects;

import java.io.*;
import java.net.Socket;

public abstract class Service <T> {
   
    protected final Socket socket;

    public Service(Socket socket) {
        this.socket = socket;
       
    }

    public T run() {
        try {
            return applyLogic();
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    protected abstract T applyLogic() throws IOException;

}