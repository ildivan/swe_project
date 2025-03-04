package backend.server.services;

import java.io.*;
import java.net.Socket;

public class ConfigService extends Service{

    public ConfigService(Socket socket){
        super(socket);
    }
    
    @Override
    void applyLogic(InputStream input, OutputStream output) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        PrintWriter writer = new PrintWriter(output, true);


    }
}
