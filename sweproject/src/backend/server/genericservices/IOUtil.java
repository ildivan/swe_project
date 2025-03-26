package backend.server.genericservices;

import java.io.IOException;

public class IOUtil extends ReadWrite{

	private final static String FORMAT_ERROR = "Attenzione: il dato inserito non e' nel formato corretto";
    
    public static int readInteger(String message){
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

	public static String readString(String message) {
		write(message, true);
		try {
			return read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static boolean readBoolean(String message){
		write(message, true);
        try {
			return Boolean.parseBoolean(read());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
