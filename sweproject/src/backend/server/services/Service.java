package backend.server.services;

import java.io.*;
import java.net.Socket;

public abstract class Service extends Thread {
    private final Socket socket;

    public Service(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            //BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            //PrintWriter writer = new PrintWriter(output, true);

            applyLogic(input,output);

            socket.close();
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public abstract void applyLogic(InputStream input, OutputStream output) throws IOException;
}