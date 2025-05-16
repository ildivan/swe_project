package server.ioservice;

import java.io.IOException;
import server.objects.interfaceforservices.iointerfaces.IActionIOService;

public class IOService extends ReadWrite{

	private final static String FORMAT_ERROR = "Attenzione: il dato inserito non e' nel formato corretto";
    
	/**
	 * struttura di enum per rendere questa classe un servizio
	 */
	public enum Service {
		READ_INTEGER((message, params) -> IOService.readInteger(message)),
		READ_INTEGER_WITH_BOUNDARIES((message, params) -> IOService.readIntegerWithMinMax(message, (int) params[0], (int) params[1])),
		READ_STRING((message, params) -> IOService.readString(message)),
		READ_BOOLEAN((message, params) -> IOService.readBoolean(message)),
		WRITE((message,params) -> {
			IOService.write(message, (Boolean) params[0]);
			return null;
		});
	
		private final IActionIOService<?> service;
	
		Service(IActionIOService<?> service) {
			this.service = service;
		}
	
		public Object start(String message, Object... params) {
			return service.apply(message, params);
		}
	}


	/*
	 * metodo per leggere un intero dall'utente
	 */
    private static int readInteger(String message){
	    boolean finito = false;
	    int valoreLetto = 0;
	    do
	    {
	     write(message,true);
	     try
	      {
	       valoreLetto = Integer.parseInt(read());
	       finito = true;
	      }
	     catch (IOException | NumberFormatException e)
	      {
	       write(FORMAT_ERROR, false);
	       
	      }
	    } while (!finito);
	   return valoreLetto;
	  
    }

	/*
	 * metodo per leggere un intero con massimo e minimo dall'utente
	 */
	private static int readIntegerWithMinMax(String message, int min, int max) {
		boolean finito = false;
		int valoreLetto = 0;
		do {
			write(message, true);
			try {
				valoreLetto = Integer.parseInt(read());
				if (valoreLetto >= min && valoreLetto <= max) {
					finito = true;
				} else {
					write("Inserisci un numero tra " + min + " e " + max + ".", false);
				}
			} catch (IOException | NumberFormatException e) {
				write(FORMAT_ERROR, false);
			}
		} while (!finito);
		return valoreLetto;
	}
	

	private static String readString(String message) {
		write(message, true);
		try {
			return read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private static boolean readBoolean(String message){
		write(message, true);
        try {
			return Boolean.parseBoolean(read());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	
}
