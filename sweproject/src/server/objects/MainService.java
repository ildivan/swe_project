package server.objects;

import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import java.io.*;
import java.net.Socket;

public abstract class MainService <T> {
   
    protected final Socket socket;

    public MainService(Socket socket) {
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
        IInputOutput ioService = new IOService();
        String choice = ioService.readString(String.format("\nProseguire con %s? (s/n)", message));
       
        if(choice.equals("n")){
            return false;
        }
        return true;
    }
}