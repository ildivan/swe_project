package server.ioservice;

import java.io.IOException;

public class IOService extends ReadWrite implements IInputOutput{

	private final static String FORMAT_ERROR = "Attenzione: il dato inserito non e' nel formato corretto";
    
	/*
	 * metodo per leggere un intero dall'utente
	 */
    public int readInteger(String message){
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
	public int readIntegerWithMinMax(String message, int min, int max) {
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
	

	public String readString(String message) {
		write(message, true);
		try {
			return read();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean readBoolean(String message){
		write(message, true);
        try {
			return Boolean.parseBoolean(read());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void writeMessage(String message, boolean responseRequired){
		write(message, responseRequired);
	}

	
}
