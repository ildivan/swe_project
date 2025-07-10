package server;// Server.java


import com.google.gson.JsonObject;
import server.authservice.AuthenticationService;
import server.authservice.User;
import server.data.DataController;
import server.data.facade.FacadeAbstractFactory;
import server.data.json.JsonFacadeAbstractFactory;
import server.data.json.NormalFunctionJsonFacadeAbstractFactory;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.data.json.datalayer.datalocalizationinformations.JsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.NormalFunctionJsonLocInfoFactory;
import server.data.json.datalayer.datareadwrite.IJsonReadWrite;
import server.data.json.datalayer.datareadwrite.JsonReadWrite;
import server.demonservices.DemonsService;
import server.firstleveldomainservices.configuratorservice.ConfigService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlanService;
import server.firstleveldomainservices.userservice.UserService;
import server.firstleveldomainservices.volunteerservice.VolunteerService;
import server.ioservice.ReadWrite;
import server.jsonfactoryservice.*;
import server.utils.ConfigType;
import server.utils.Configs;
import server.utils.ConnectionType;
import server.utils.MainService;
import server.utils.ServerConnectionPorts;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Server {

    private static final String JSON_NORMAL_FUNCTION_DIRECTORY = "sweproject/JFNormalFunction";
    private final IJsonLocInfoFactory locInfoFactory;
    private final int CLIENT_PORT = ServerConnectionPorts.CLIENT.getCode();
    private final int SERVER_TERMINA_PORT = ServerConnectionPorts.SERVER.getCode();
    private final IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    DemonsService demonsService;
    private final IJsonReadWrite jsonReadWrite;
    private final JsonDataLayer dataLayer;
    MonthlyPlanService monthlyPlanService;

    public Server(ConfigType configType, List<User> users) {
        this.jsonReadWrite = new JsonReadWrite();
        this.dataLayer = new JsonDataLayer(jsonReadWrite);
        this.locInfoFactory = getLocInfoFactory(configType);
        this.monthlyPlanService = new MonthlyPlanService(locInfoFactory, configType, dataLayer);
        
        if(configType == ConfigType.NORMAL){
            initializeJsonRepository();
            initializeConfig();
            initializeUsers(users);
            initializeVolunteers();
            initializeMonthlyConfig();
        }

        if(configType == ConfigType.NO_FIRST_CONFIG){
            initializeChangedFiles();
        }
        this.demonsService = new DemonsService(locInfoFactory, configType, dataLayer);

    }

    /**
     * method to initialize editable files
     */
    private void initializeChangedFiles() {
        Path changedPlacesPath = Paths.get(locInfoFactory.getChangedPlacesLocInfo().getPath());
        Path originalPlacesPath = Paths.get(locInfoFactory.getPlaceLocInfo().getPath());

        try {
            Files.copy(originalPlacesPath, changedPlacesPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Path changedActivitiesPath = Paths.get(locInfoFactory.getChangedActivitiesLocInfo().getPath());
        Path originalActivitiesPath = Paths.get(locInfoFactory.getActivityLocInfo().getPath());

        try {
            Files.copy(originalActivitiesPath,changedActivitiesPath,  java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per creare la directory JFNormalFunction e svuotarla se esiste
     */
    private void initializeJsonRepository() {
        File newFolder = new File(JSON_NORMAL_FUNCTION_DIRECTORY);

        if (newFolder.exists()) {
            deleteContents(newFolder);
        }

        newFolder.mkdirs();
    }

    /**
     * Metodo ricorsivo per cancellare tutti i file e le sottocartelle in una directory
     */
    private void deleteContents(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteContents(file); // Elimina ricorsivamente il contenuto
                }
                file.delete(); // Elimina file o cartella vuota
            }
        }
    }

    /**
     * method to initialize configs.json
     */
    private void initializeConfig() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getConfigLocInfo();
        Configs configs = new Configs();

        dataLayer.add(jsonFactoryService.createJson(configs), locInfo);
    }

    /**
     * method to initialize users.json
     * @param users
     */
    private void initializeUsers(List<User> users) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getUserLocInfo();

        for (User user : users) {
            dataLayer.add(jsonFactoryService.createJson(user), locInfo);
        }
    }

    /**
     * method to initialize volunteers.json
     */
    private void initializeVolunteers() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getVolunteerLocInfo();
        dataLayer.createJSONEmptyFile(locInfo);
    }

    /**
     * method to initialize MonthlyConfig.json
     */
    private void initializeMonthlyConfig() {
        MonthlyConfig monthlyConfig = monthlyPlanService.getNewMonthlyConfig();

        saveMonthlyConfig(monthlyConfig);
    
    }

    /**
     * method to save monthly config
     * @param monthlyConfig
     */
    private void saveMonthlyConfig(MonthlyConfig monthlyConfig) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyConfigLocInfo();

        dataLayer.add(jsonFactoryService.createJson(monthlyConfig), locInfo);
    }

    /**
     * Metodo che ritorna il factory per le informazioni di localizzazione
     * @param configType
     * @return
     */
    private IJsonLocInfoFactory getLocInfoFactory(ConfigType configType) {
        switch (configType) {
            case NORMAL:
                return new NormalFunctionJsonLocInfoFactory();
            case NO_FIRST_CONFIG:
                return new JsonLocInfoFactory();
            default:
                throw new IllegalArgumentException("Unsupported config type: " + configType);
        }
    }

    public void startServer(ConfigType configType) {
        try (ServerSocket clientSS = new ServerSocket(CLIENT_PORT);
            ServerSocket serverTerminalSS = new ServerSocket(SERVER_TERMINA_PORT)) {

            System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
            System.out.println("Server is listening on port " + CLIENT_PORT + " -> Client Port");
            System.out.println("Server is listening on port " + SERVER_TERMINA_PORT + " -> Backend Port");

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
                                FacadeAbstractFactory facadeFactory;
                                if (configType == ConfigType.NORMAL){
                                    facadeFactory = new NormalFunctionJsonFacadeAbstractFactory();
                                } else {
                                    facadeFactory = new JsonFacadeAbstractFactory();
                                }
                                DataController data = new DataController(facadeFactory);
                                ReadWrite.setConnection(internalSocket);
                                User u = authenticate(internalSocket, ConnectionType.Internal);
                                if (u == null) {
                                    Thread.sleep(100);
                                    internalSocket.close();
                                    return;
                                }
                                
                                MainService<?> service = obtainService(u, internalSocket, configType, data);
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
                                    Thread.sleep(100);
                                    externalSocket.close();
                                    return;
                                }
                                MainService<?> service = new UserService(externalSocket,u,locInfoFactory,configType, dataLayer);
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
        AuthenticationService login = new AuthenticationService(socket, connectionType, locInfoFactory, dataLayer);
        return login.run();
    }

    private MainService<?> obtainService(User u, Socket socket, ConfigType configType, DataController data){
        assert u != null;
        assert socket != null;
        assert configType != null;

        switch (u.getRole()){
            case "configuratore":
                return new ConfigService(socket,locInfoFactory, configType, dataLayer, data);
            case "volontario":
                return new VolunteerService(socket,u.getName(),locInfoFactory, configType, dataLayer);
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
        
        ConfigType configType = ConfigType.NORMAL;
        // ConfigType configType = ConfigType.NO_FIRST_CONFIG;

        //creo gli utenti che mi servono
        // configuratore, fruitore -> volontario è aggiunto successivamente
        
        List<User> users = new ArrayList<>();
        User configuratore = new User("c1", "temp_123", "configuratore");
        User fruitore = new User("f1", "temp_234", "fruitore");
        users.add(fruitore);
        users.add(configuratore);

        Server s = new Server(configType,users);

        s.startServer(configType);
       
    }

}



