package backend.server.services;

import backend.server.json.Message;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

public abstract class Service <T> {
    private final Socket socket;
    private final Gson gson;
    private BufferedReader reader;
    private PrintWriter writer;

    public Service(Socket socket, Gson gson) {
        this.socket = socket;
        this.gson = gson;
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
}