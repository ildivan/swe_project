package server;

import server.authservice.AuthenticationService;
import server.authservice.User;
import server.data.facade.FacadeHub;
import server.data.facade.implementation.NoFirstConfigJsonFacadeFactory;
import server.data.facade.implementation.NormalFunctionJsonFacadeFactory;
import server.data.facade.implementation.TestJsonFacadeFactory;
import server.data.facade.interfaces.IFacadeAbstractFactory;
import server.demonservices.DemonsService;
import server.firstleveldomainservices.configuratorservice.ConfigService;
import server.firstleveldomainservices.userservice.UserService;
import server.firstleveldomainservices.volunteerservice.VolunteerService;
import server.ioservice.ReadWrite;
import server.utils.ConfigType;
import server.utils.ConnectionType;
import server.utils.MainService;
import server.utils.ServerConnectionPorts;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


public class Server {

    private static final String JSON_NORMAL_FUNCTION_DIRECTORY = "sweproject/JFNormalFunction";
    private static final String JSON_TEST_FUNCTION_DIRECTORY = "test/JFTest";
    private final int CLIENT_PORT = ServerConnectionPorts.CLIENT.getCode();
    private final int SERVER_TERMINA_PORT = ServerConnectionPorts.SERVER.getCode();


    DemonsService demonsService;
    private final FacadeHub data;

    public Server(ConfigType configType, List<User> users, IFacadeAbstractFactory facadeFactory) {

        this.data = new FacadeHub(facadeFactory);
       
        if(configType == ConfigType.NORMAL || configType == ConfigType.TEST){
            initializeJsonRepository(configType);
            data.getConfigFacade().initializeConfig();
            data.getUsersFacade().addUsers(users);
            data.getMonthlyConfigFacade().initializeMonthlyConfig();
        }

        if(configType == ConfigType.NO_FIRST_CONFIG){
            initializeChangedFiles();
        }
        this.demonsService = new DemonsService(configType,data);

    }

    /**
     * method to initialize editable files
     */
    private void initializeChangedFiles() {
       data.getPlacesFacade().initializeChangedFiles();
       data.getActivitiesFacade().initializeChangedFiles();
        
    }

    /**
     * Metodo per creare la directory JFNormalFunction e svuotarla se esiste
     */
    private void initializeJsonRepository(ConfigType configType) {

        File newFolder;

        if(configType == ConfigType.NORMAL){
            newFolder = new File(JSON_NORMAL_FUNCTION_DIRECTORY);
        }else{
            newFolder = new File(JSON_TEST_FUNCTION_DIRECTORY);
        }

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

    public void startServer(ConfigType configType) {
        try (ServerSocket clientSS = new ServerSocket(CLIENT_PORT);
            ServerSocket serverTerminalSS = new ServerSocket(SERVER_TERMINA_PORT)) {
            
            System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
            System.out.println("Server is listening on port " + CLIENT_PORT + " -> Client Port");
            System.out.println("Server is listening on port " + SERVER_TERMINA_PORT + " -> Backend Port");

            // Avvio i demoni
            demonsService.run();

            if (configType == ConfigType.NORMAL) {
                data.getConfigFacade().firstTimeConfigurationServerConfig();
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
                                MainService<?> service = new UserService(externalSocket,u,configType, data);
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
        AuthenticationService login = new AuthenticationService(socket, connectionType, data);
        return login.run();
    }

    private MainService<?> obtainService(User u, Socket socket, ConfigType configType, FacadeHub data){
        assert u != null;
        assert socket != null;
        assert configType != null;

        switch (u.getRole()){
            case "configuratore":
                return new ConfigService(socket,configType, data);
            case "volontario":
                return new VolunteerService(socket,u.getName(), configType,data);
            default:
                assert false;
                return null;
        }
    }
    public static void output(String message){
        System.out.println(message);
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
        
        IFacadeAbstractFactory facadeFactory;
        switch(configType) {
            case ConfigType.NORMAL:
                facadeFactory = new NormalFunctionJsonFacadeFactory();
            break;
            case ConfigType.NO_FIRST_CONFIG:
                facadeFactory = new NoFirstConfigJsonFacadeFactory();
            break;
            case ConfigType.TEST:
                facadeFactory = new TestJsonFacadeFactory();
            break;
            default:
                facadeFactory = null;
        }

        Server s = new Server(configType,users, facadeFactory);

        s.startServer(configType);
       
    }

}



