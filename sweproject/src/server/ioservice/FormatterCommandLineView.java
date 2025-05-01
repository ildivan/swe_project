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

public abstract class FormatterCommandLineView {

    protected static final ThreadLocal<BufferedReader> reader = new ThreadLocal<>();
    protected static final ThreadLocal<PrintWriter> writer = new ThreadLocal<>();
    protected static final Gson gson = new Gson();
    private static ControlTypeService controlTypeService = new ControlTypeService();
    
    public static void setConnection(Socket socket) throws IOException {
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
    protected static void write(Message message) {
        IOStringMessage ioMessage = controlTypeService.controlAndGet(message, IOStringMessage.class);
        writer.get().println(gson.toJson(ioMessage));
    }

    /**
     * read a message from the user
     * @return a message containing the input from the user in the form of a string
     */
    protected static Message read() throws IOException {
        String input = reader.get().readLine();
        Message ioMessage = new Message(JSONService.Service.CREATE_JSON.start(input), String.class);
        return ioMessage;
    }

    /**
     * ask the user if he wants to continue with the operation
     * 
     * necessita che message contenga una classe string
     * 
     * @param message the operation the user wants to continue
     * @return
     */
    protected static boolean continueChoice(Message message) {
       
        String ioString = controlTypeService.controlAndGet(message, String.class);
        //creo la stringa contentente il messaggio da visualizzare all'utente
        ioString = String.format("\nProseguire con %s? (s/n)", ioString);
        //incapsulo la stringa in un oggetto che rappresenta il messaggio con la necessita o meno di risposta
        IOStringMessage toWrite = new IOStringMessage(JSONService.Service.CREATE_JSON.start(ioString), true);
        //creo il messaggio che sarà mandato al server da mandare al client che poi lo formatterà per visualizzarlo
        Message ioMessage = new Message(JSONService.Service.CREATE_JSON.start(toWrite), IOStringMessage.class);

        write(ioMessage);
        String choice = "";
        try {
            choice = controlTypeService.controlAndGet(read(), String.class);
            //controllo che la risposta sia effettivamente una stringa;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return !"n".equals(choice);
    }
        
}
