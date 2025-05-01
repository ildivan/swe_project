package server.objects;

import server.ioservice.IOServiceWithCommandLine;
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

    /**
     * ask the user if he wants to continue with the operation
     * @param message the operation the user wants to continue
     * @return
     */
    protected static boolean continueChoice(String message) {
        String choice = (String) IOServiceWithCommandLine.Service.READ_STRING.start(String.format("\nProseguire con %s? (s/n)", message));
       
        if(choice.equals("n")){
            return false;
        }
        return true;
    }
}