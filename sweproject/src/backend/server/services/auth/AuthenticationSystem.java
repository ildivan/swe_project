package backend.server.services.auth;
import backend.server.services.Service;

import java.io.*;
import java.net.Socket;

public class AuthenticationSystem extends Service {
    public AuthenticationSystem(Socket socket) {
        super(socket);
    }

    @Override
    public void applyLogic(InputStream input, OutputStream output) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        PrintWriter writer = new PrintWriter(output, true);

        writer.println("Inserisci username:");
        String username = reader.readLine();
        //CHECK USERNAME
        writer.println("Inserisci password:");
        String pass = reader.readLine();
        //CHECK PASSWORD
        writer.println("GRAZIE");
    }
}
