package backend.server.genericservices;

import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;

public abstract class Service <T> {
   
    protected final Socket socket;
    protected final Gson gson = new Gson();
    protected BufferedReader reader;
    protected PrintWriter writer;

    public Service(Socket socket) {
        this.socket = socket;
       
    }

    public T run() {
        try {
            InputStream input = socket.getInputStream();
            this.reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            this.writer = new PrintWriter(output, true);

            return applyLogic();
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    protected abstract T applyLogic() throws IOException;

    protected void write(String message, boolean responseRequired) {
        writer.println(gson.toJson(new Message(message, responseRequired)));
    }

    protected String read() throws IOException {
        return reader.readLine();
    }

    /**
     * ask the user if he wants to continue with the operation
     * @param message the operation the user wants to continue
     * @return
     */
    protected boolean continueChoice(String message) {
        write(String.format("Proseguire con %s? (s/n)", message),true);
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