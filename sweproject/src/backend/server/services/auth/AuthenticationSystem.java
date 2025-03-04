package backend.server.services.auth;
import backend.server.json.Message;
import backend.server.services.Service;
import com.google.gson.Gson;

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
        Gson gson = new Gson();

        writer.println(gson.toJson(new Message("Inserisci username:", true)));
        String username = reader.readLine();
        //CHECK USERNAME
        writer.println(gson.toJson(new Message("Inserisci password:", true)));
        String pass = reader.readLine();
        //CHECK PASSWORD
        writer.println(gson.toJson(new Message("GRAZIE", false)));
    }
}
