package server;// Server.java

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import server.authservice.AuthenticationService;
import server.authservice.User;
import server.datalayerservice.DataLayerDispatcherService;
import server.datalayerservice.JsonDataLocalizationInformation;
import server.demonservices.DemonsService;
import server.firstleveldomainservices.configuratorservice.ConfigService;
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

    private static final String GENERAL_CONFIGS_KEY_DESCRIPTION = "configType";
    private static final String GENERAL_CONFIGS_MEMBER_NAME = "configs";
    private static final String GENERAL_CONFIG_PATH = "JF/configs.json";
    private final int CLIENT_PORT = ServerConnectionPorts.CLIENT.getCode();
    private final int SERVER_TERMINA_PORT = ServerConnectionPorts.SERVER.getCode();

    private final IGsonFactory gsonFactoryService = new GsonFactoryService();
    private final Gson gson = gsonFactoryService.getGson();

    private final IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    DemonsService demonsService = new DemonsService();

    
    public Server(){
       
       // this.planManager = new PlanManager();
    }

    public void startServer(ConfigType configType){
        try (ServerSocket clientSS = new ServerSocket(CLIENT_PORT);
            ServerSocket serverTerminalSS = new ServerSocket(SERVER_TERMINA_PORT)) {
            System.out.println("Server is listening on port " + CLIENT_PORT);
            System.out.println("Server is listening on port " + SERVER_TERMINA_PORT);

            //avvio i demoni
            demonsService.run();


            if(configType == ConfigType.NORMAL){
                firstTimeConfiguration();
                System.out.println("First time default configuration completed");
            }

            Thread internalConnectionThread = new Thread(() -> {
                try {
                    while(true) {
                        Socket socket = serverTerminalSS.accept();
                        ReadWrite.setConnection(socket);
                        System.out.println("Internal Connection");
                        User u = authenticate(socket,ConnectionType.Internal);
                        if(u == null){
                            socket.close();
                            continue;
                        }

                        //pattern solid delle inteerfacce per generalizzare e non dover riscrivere 
                        //il codice se aggiungo unnuovo tipo di utente 
                        Thread serviceThread = new Thread(() -> {
                            try {
                                ReadWrite.setConnection(socket);
                                // Ottieni il servizio associato all'utente e alla connessione
                                MainService<?> s = obtainService(u, socket, configType);
                                s.run();  // Esegui il servizio
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                            } finally {
                                try {
                                    socket.close();  // Assicurati di chiudere il socket alla fine
                                } catch (IOException e) {
                                    System.out.println("Error closing socket: " + e.getMessage());
                                }
                            }
                        });
            
                        // Avvia il thread che gestisce il servizio
                        serviceThread.start();
                        
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println(e.getMessage());
                    throw new RuntimeException(e);
                }
            });
            internalConnectionThread.start();

            while (true) {
                Socket socket = clientSS.accept();
                System.out.println("External Connection"); //è sicuramente un fruitore da implementare
                authenticate(socket,ConnectionType.External);
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        } catch(InterruptedException e) {
            System.out.println("Server interrupted: " + e.getMessage());
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

        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(GENERAL_CONFIG_PATH);

        if(!DataLayerDispatcherService.startWithResult(locInfo, layer->layer.checkFileExistance(locInfo))){
            DataLayerDispatcherService.start(locInfo, layer->layer.createJSONEmptyFile(locInfo));
        }

        JsonObject JO = jsonFactoryService.createJson(new Configs());

        locInfo.setMemberName(GENERAL_CONFIGS_MEMBER_NAME);
        locInfo.setKeyDesc(GENERAL_CONFIGS_KEY_DESCRIPTION);
        locInfo.setKey(ConfigType.NORMAL.getValue());
    
        DataLayerDispatcherService.startWithResult(locInfo, layer->layer.modify(JO, locInfo));
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



