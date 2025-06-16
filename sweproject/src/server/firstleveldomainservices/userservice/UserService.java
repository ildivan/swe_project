package server.firstleveldomainservices.userservice;

import java.io.IOException;
import java.net.Socket;

import com.google.gson.Gson;

import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuService;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.menus.UserMenu;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.ioservice.objectformatter.IIObjectFormatter;
import server.ioservice.objectformatter.TerminalObjectFormatter;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;
import server.utils.ConfigType;
import server.utils.MainService;

public class UserService extends MainService<Void> {

    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";

    private final MenuService menu; 
    private final ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory = new JsonLocInfoFactory();
    private final IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private final IInputOutput ioService = new IOService();
    private final IIObjectFormatter<String> formatter= new TerminalObjectFormatter();
    private final IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();
    
  

    public UserService(Socket socket) {
        super(socket);
        this.menu = new UserMenu(this);
    }



    @Override
    protected Void applyLogic() throws IOException {
        doOperations();
        ioService.writeMessage("\nArrivederci!\n", false);

        return null;
    }

    private void doOperations() {
        boolean continuare;
        do{
            ioService.writeMessage(CLEAR,false);
            Runnable toRun = menu.startMenu();
            ioService.writeMessage(SPACE, false);
            if(toRun==null){
                continuare = false;
            }else{
                toRun.run();
                continuare = continueChoice("scelta operazioni");
            }

        }while(continuare);
    }

    public void addSubscription() {
        // Implement the logic to add a subscription
        // This is a placeholder method and should be replaced with actual implementation
        ioService.writeMessage("TO DO: Implement addSubscription logic in UserService.", false);
    }



}
