package backend;

import java.io.*;

import java.net.Socket;
import java.net.UnknownHostException;

public class Terminal {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 5000;

        try (Socket socket = new Socket(hostname, port);
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true);
             InputStream input = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {

            Thread readThread = new Thread(() -> {
                do {
                    try {
                        String response = reader.readLine();
                        System.out.println(response);
                    } catch (IOException e) {
                        return;
                    }
                } while (true);
            });

            Thread writeThread = new Thread(() -> {
                do {
                    try {
                        System.out.print(">>");
                        String text = consoleReader.readLine();
                        writer.println(text);
                    } catch (IOException e) {
                        return;
                    }
                } while (true);
            });

            readThread.start();
            writeThread.start();

            try{
                readThread.join();
                writeThread.join();
            }catch(Exception e){
                System.out.println(e.getMessage());
            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}


