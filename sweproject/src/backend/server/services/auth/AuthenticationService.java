package backend.server.services.auth;
import backend.server.ConnectionType;
import backend.server.json.Message;
import backend.server.services.Service;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

public class AuthenticationService extends Service<String> {

    private final ConnectionType connectionType;

    public AuthenticationService(Socket socket, Gson gson, ConnectionType connectionType) {
        super(socket, gson);
        this.connectionType = connectionType;
    }

    @Override
    public String applyLogic() throws IOException {

        write("Inserisci username:", true);
        String username = read();
        //CHECK USERNAME
        write("Inserisci password:", true);
        String pass = read();
        //CHECK PASSWORD
        write("GRAZIE", false);
        return "";
    }
}
