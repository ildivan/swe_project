package server.ioservice;

import java.io.IOException;

import server.ControlTypeService;
import server.datalayerservice.JSONService;
import server.messages.IOMessageReadIntWithBoundaries;
import server.messages.IOStringMessage;
import server.messages.Message;
import server.objects.interfaceforservices.IActionService;

public class IOServiceWithCommandLine extends FormatterCommandLineView{
	private static final ControlTypeService controlTypeService = new ControlTypeService();

	private final static String FORMAT_ERROR = "Attenzione: il dato inserito non e' nel formato corretto";
    
	/**
	 * struttura di enum per rendere questa classe un servizio
	 */
	public enum Service {
		READ_INTEGER(( params) -> IOServiceWithCommandLine.readInteger((Message) params[0])),
		READ_INTEGER_WITH_BOUNDARIES(( params) -> IOServiceWithCommandLine.readIntegerWithMinMax((Message) params[0])),
		READ_STRING(( params) -> IOServiceWithCommandLine.readString((Message) params[0])),
		READ_BOOLEAN(( params) -> IOServiceWithCommandLine.readBoolean((Message) params[0])),
		WRITE(( params) -> {
			IOServiceWithCommandLine.write((Message) params[0]);
			return null;
		});
	
		private final IActionService<?> service;
	
		Service(IActionService<?> service) {
			this.service = service;
		}
	
		// il problem compile time viene ignorato poiche a run time il tipo viene deciso e controllato
        // in modo sicuro
        @SuppressWarnings("unchecked")
        public <T> T start(Object... params) {
            // Il tipo viene deciso dinamicamente, quindi il casting avviene in modo sicuro
            return (T) service.apply(params);
        }
	}


	/*
	 * metodo per leggere un intero dall'utente
	 */
    private static int readInteger(Message message){
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

	/*
	 * metodo per leggere un intero con massimo e minimo dall'utente
	 * String message, int min, int max
	 */
	private static int readIntegerWithMinMax(Message message) {
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
	

	private static String readString(Message message) {
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

	private static boolean readBoolean(Message message){
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

	
}
