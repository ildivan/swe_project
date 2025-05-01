package server.ioservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;

import server.ControlTypeService;
import server.datalayerservice.JSONService;
import server.messages.IOStringMessage;
import server.messages.Message;

public class FormatterCommandLineView implements IExternalFormatter {

    protected static final ThreadLocal<BufferedReader> reader = new ThreadLocal<>();
    protected static final ThreadLocal<PrintWriter> writer = new ThreadLocal<>();
    protected static final Gson gson = new Gson();
    private static ControlTypeService controlTypeService = new ControlTypeService();

   
    public FormatterCommandLineView() {
        super();
    }
    
    /**
     * Initializes the input and output streams for the current thread using the provided socket.
     * Used in class: Server.java
     * <p>
     * This method sets up a {@link BufferedReader} and {@link PrintWriter} in {@link ThreadLocal} storage,
     * allowing thread-safe I/O operations for each client connection in a multithreaded server environment.
     * </p>
     *
     * <p>
     * It must be called once per client connection (typically inside the thread that handles the client),
     * before using {@code read()} or {@code write()} methods.
     * </p>
     *
     * @param socket the socket representing the client connection
     * @throws IOException if an I/O error occurs while accessing the socket's input or output streams
     */
    public void setConnection(Socket socket) throws IOException {
        reader.set(new BufferedReader(new InputStreamReader(socket.getInputStream())));
        writer.set(new PrintWriter(socket.getOutputStream(), true));
    }

    /**
     * write a message to the user, viene controllato che sia stata passata effittivamente una
     * stringa e poi viene stampata
     * 
     * necessita che messsage contenga una istanza di IOStringMessage altrimenti non funziona la ricezione del mesaggio
     * e l'analisi della necessità di risposta in terminal
     * 
     * poiche questo è il formatter per il terminal non manda un messaggio generico ma il tipo di messaggio specifico
     * gia unboxato pronto all'uso dal terminal
     * @param message the message to write
     * @param requiresResponse if the message requires a response from the user
     */
    public void write(Message message) {
        IOStringMessage ioMessage = controlTypeService.controlAndGet(message, IOStringMessage.class);
        writer.get().println(gson.toJson(ioMessage));
    }

    /**
     * read a message from the user
     * @return a message containing the input from the user in the form of a string
     */
    public Message read() throws IOException {
        String input = reader.get().readLine();
        Message ioMessage = new Message(JSONService.Service.CREATE_JSON.start(input), String.class);
        return ioMessage;
    }

   
}
