package server;// Server.java

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import server.authservice.AuthenticationService;
import server.authservice.User;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.demonservices.DemonsService;
import server.firstleveldomainservices.configuratorservice.ConfigService;
import server.firstleveldomainservices.userservice.UserService;
import server.firstleveldomainservices.volunteerservice.VolunteerService;
import server.gsonfactoryservice.GsonFactoryService;
import server.gsonfactoryservice.IGsonFactory;
import server.ioservice.ReadWrite;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;
import server.utils.ConfigType;
import server.utils.Configs;
import server.utils.ConnectionType;
import server.utils.MainService;
import server.utils.ServerConnectionPorts;

import java.io.*;
import java.net.*;



public class Server {

    private final ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory = new JsonLocInfoFactory();
    private final int CLIENT_PORT = ServerConnectionPorts.CLIENT.getCode();
    private final int SERVER_TERMINA_PORT = ServerConnectionPorts.SERVER.getCode();

    private final IGsonFactory gsonFactoryService = new GsonFactoryService();
    private final Gson gson = gsonFactoryService.getGson();
    private final IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    DemonsService demonsService = new DemonsService();
    private final IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();

    
    public Server(){
       
       // this.planManager = new PlanManager();
    }

public void startServer(ConfigType configType) {
    try (ServerSocket clientSS = new ServerSocket(CLIENT_PORT);
         ServerSocket serverTerminalSS = new ServerSocket(SERVER_TERMINA_PORT)) {

        System.out.println("Server is listening on port " + CLIENT_PORT);
        System.out.println("Server is listening on port " + SERVER_TERMINA_PORT);

        // Avvio i demoni
        demonsService.run();

        if (configType == ConfigType.NORMAL) {
            firstTimeConfiguration();
            System.out.println("First time default configuration completed");
        }

        // Thread per connessioni interne
        Thread internalConnectionThread = new Thread(() -> {
            try {
                while (true) {
                    Socket socket = serverTerminalSS.accept();
                    System.out.println("Internal Connection accepted");

                    final Socket internalSocket = socket;

                    // Autentica e gestisci la connessione in un thread separato
                    Thread serviceThread = new Thread(() -> {
                        try {
                            ReadWrite.setConnection(internalSocket);
                            User u = authenticate(internalSocket, ConnectionType.Internal);
                            if (u == null) {
                                internalSocket.close();
                                return;
                            }
                           
                            MainService<?> service = obtainService(u, internalSocket, configType);
                            service.run();
                        } catch (IOException | InterruptedException e) {
                            System.out.println("Internal service error: " + e.getMessage());
                        } finally {
                            try {
                                internalSocket.close();
                            } catch (IOException e) {
                                System.out.println("Error closing internal socket: " + e.getMessage());
                            }
                        }
                    });
                    serviceThread.start();
                }
            } catch (IOException e) {
                System.out.println("Internal connection thread exception: " + e.getMessage());
            }
        });
        internalConnectionThread.start();

        // Thread per connessioni esterne
        Thread externalConnectionThread = new Thread(() -> {
            try {
                while (true) {
                    Socket socket = clientSS.accept();
                    System.out.println("External Connection accepted");

                    final Socket externalSocket = socket;

                    Thread serviceThread = new Thread(() -> {
                        try {
                            ReadWrite.setConnection(externalSocket);
                            User u = authenticate(externalSocket, ConnectionType.External);
                            if (u == null) {
                                externalSocket.close();
                                return;
                            }
                            MainService<?> service = new UserService(externalSocket,u,configType);
                            service.run();
                        } catch (IOException | InterruptedException e) {
                            System.out.println("External service error: " + e.getMessage());
                        } finally {
                            try {
                                externalSocket.close();
                            } catch (IOException e) {
                                System.out.println("Error closing external socket: " + e.getMessage());
                            }
                        }
                    });
                    serviceThread.start();
                }
            } catch (IOException e) {
                System.out.println("External connection thread exception: " + e.getMessage());
            }
        });
        externalConnectionThread.start();

        //attendo che i due thread terminimo prima di chiudere il socket (creato nel try in cui sono creati
        //i thread di ascolto)
        internalConnectionThread.join();
        externalConnectionThread.join();


    } catch (IOException | InterruptedException e) {
        System.out.println("Server exception: " + e.getMessage());
    }
}


    private User authenticate(Socket socket, ConnectionType connectionType)
            throws InterruptedException, IOException {
        AuthenticationService login = new AuthenticationService(socket, connectionType);
        return login.run();
    }

    private MainService<?> obtainService(User u, Socket socket, ConfigType configType){
        assert u != null;
        assert socket != null;
        assert configType != null;

        switch (u.getRole()){
            case "configuratore":
                return new ConfigService(socket,gson,configType);
            case "volontario":
                return new VolunteerService(socket,gson,configType,u.getName());
            case "fruitore":
                //return new UserService(socket,gson);
            default:
                assert false;
                return null;
        }
    }
    public static void output(String message){
        System.out.println(message);
    }

    private void firstTimeConfiguration(){

        JsonDataLocalizationInformation locInfo = locInfoFactory.getConfigLocInfo();

        if(!dataLayer.checkFileExistance(locInfo)){
            dataLayer.createJSONEmptyFile(locInfo);
        }

        JsonObject JO = jsonFactoryService.createJson(new Configs());
        
        locInfo.setKey(ConfigType.NORMAL.getValue());
    
        dataLayer.modify(JO, locInfo);
    }

    public static void main(String[] args) {
        // String currentDir = System.getProperty("user.dir");
        // System.out.println("La directory di lavoro corrente è: " + currentDir);

        // ConfigType configType = ConfigType.NORMAL;

        
        ConfigType configType = ConfigType.NO_FIRST_CONFIG;
        Server s = new Server();
        
        s.startServer(configType);
       
        


    }

}



