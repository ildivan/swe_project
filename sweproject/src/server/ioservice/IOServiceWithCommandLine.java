package server.ioservice;

import java.io.IOException;
import server.ControlTypeService;
import server.datalayerservice.JSONService;
import server.messages.IOMessageReadIntWithBoundaries;
import server.messages.IOStringMessage;
import server.messages.Message;

public class IOServiceWithCommandLine extends FormatterCommandLineView implements IIOService {
	private static final ControlTypeService controlTypeService = new ControlTypeService();

	private final static String FORMAT_ERROR = "Attenzione: il dato inserito non e' nel formato corretto";

	public void write(Message message) {
		super.write(message);
	}
	/**
	 * metodo per leggere un intero dall'utente
	 * @param message message che contiene un oggetto stringa con il messsaggio di richiesta
	 */
    public int readInteger(Message message){
		//leggo la stringa che mi arriva dal server che mi descrive la richiesta
		String stringMessage = controlTypeService.controlAndGet(message, String.class);
		//creo un messaggio di tipo IOStringMessage per il terminale contenente il fatto che necessito di risposta
		//dato che sto leggendo un intero dall'utente la rispota è proprio questo intero
		IOStringMessage ioStringMessage = new IOStringMessage(stringMessage, true);
		//Creo il messsaggio che viene manato a write
		Message toBeWritten = new Message(JSONService.createJson(ioStringMessage), IOStringMessage.class);
	    boolean finito = false;
	    int valoreLetto = 0;
	    do
	    {
	     write(toBeWritten);
	     try
	      {
		   String input = controlTypeService.controlAndGet(read() , String.class);
	       valoreLetto = Integer.parseInt(input);
	       finito = true;
	      }
	     catch (IOException | NumberFormatException e)
	      {
		   IOStringMessage errorMessage = new IOStringMessage(FORMAT_ERROR, false);
		   Message error = new Message(JSONService.createJson(errorMessage), IOStringMessage.class);
	       write(error);
	       
	      }
	    } while (!finito);
	   return valoreLetto;
	  
    }

	/**
	 * metodo per leggere un intero con massimo e minimo dall'utente
	 * @param message message che contiene un oggetto IOMessageReadIntWithBoundaries con il messsaggio di richiesta,
	 */
	public int readIntegerWithMinMax(Message message) {
		//unbooxo il messaggio che mi arriva dal server che mi descrive la richiesta
		IOMessageReadIntWithBoundaries messageWithBoundaries = controlTypeService.controlAndGet(message, IOMessageReadIntWithBoundaries.class);
		int min = messageWithBoundaries.getMin();
		int max = messageWithBoundaries.getMax();	
		String stringMessage = messageWithBoundaries.getText();
		//creo un messaggio di tipo IOStringMessage per il terminale contenente il fatto che necessito di risposta
		//dato che sto leggendo un intero dall'utente la rispota è proprio questo intero
		IOStringMessage ioStringMessage = new IOStringMessage(stringMessage, true);
		//Creo il messsaggio che viene manato a write
		Message toBeWritten = new Message(JSONService.createJson(ioStringMessage), IOStringMessage.class);
		boolean finito = false;
		int valoreLetto = 0;
		do {
			write(toBeWritten);
			try {
				String input = controlTypeService.controlAndGet(read() , String.class);
	            valoreLetto = Integer.parseInt(input);
				if (valoreLetto >= min && valoreLetto <= max) {
					finito = true;
				} else {
					IOStringMessage errorMessage = new IOStringMessage("Inserisci un numero tra " + min + " e " + max + ".", false);
		            Message error = new Message(JSONService.createJson(errorMessage), IOStringMessage.class);
					write(error);
				}
			} catch (IOException | NumberFormatException e) {
				IOStringMessage errorMessage = new IOStringMessage(FORMAT_ERROR, false);
		  		Message error = new Message(JSONService.createJson(errorMessage), IOStringMessage.class);
	       		write(error);
			}
		} while (!finito);
		return valoreLetto;
	}
	
	/**
	 * metodo per leggere una stringa dall'utente
	 * @param message un message che contiene un oggetto stringa con il messsaggio di richiesta
	 * @return
	 */
	public String readString(Message message) {
		//leggo la stringa che mi arriva dal server che mi descrive la richiesta
		String stringMessage = controlTypeService.controlAndGet(message, String.class);
		//creo un messaggio di tipo IOStringMessage per il terminale contenente il fatto che necessito di risposta
		//dato che sto leggendo un intero dall'utente la rispota è proprio questo intero
		IOStringMessage ioStringMessage = new IOStringMessage(stringMessage, true);
		//Creo il messsaggio che viene manato a write
		Message toBeWritten = new Message(JSONService.createJson(ioStringMessage), IOStringMessage.class);
		write(toBeWritten);
		try {
			return controlTypeService.controlAndGet(read() , String.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * metodo per leggere un booleano dall'utente
	 * @param message un message che contiene un oggetto stringa con il messsaggio di richiesta
	 * @return
	 */
	public boolean readBoolean(Message message){
		//leggo la stringa che mi arriva dal server che mi descrive la richiesta
		String stringMessage = controlTypeService.controlAndGet(message, String.class);
		//creo un messaggio di tipo IOStringMessage per il terminale contenente il fatto che necessito di risposta
		//dato che sto leggendo un intero dall'utente la rispota è proprio questo intero
		IOStringMessage ioStringMessage = new IOStringMessage(stringMessage, true);
		//Creo il messsaggio che viene manato a write
		Message toBeWritten = new Message(JSONService.createJson(ioStringMessage), IOStringMessage.class);
		write(toBeWritten);
        try {
			String input = controlTypeService.controlAndGet(read() , String.class);
	      	return Boolean.parseBoolean(input);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	 /**
     * ask the user if he wants to continue with the operation
     * 
     * necessita che message contenga una classe string
     * 
     * @param message the operation the user wants to continue
     * @return
     */
	
    public boolean continueChoice(Message message) {
       
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
