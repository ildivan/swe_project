package server.firstleveldomainservices.userservice;

import java.io.IOException;
import java.net.Socket;
import server.authservice.User;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuService;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.menus.UserMenu;
import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.SubscriptionService;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.utils.ConfigType;
import server.utils.MainService;

public class UserService extends MainService<Void> {

    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";

    private final MenuService menu; 
    private final IInputOutput ioService = new IOService();
    private final SubscriptionService subscriptionService;
    


    public UserService(Socket socket, User user, ConfigType configType) {
        super(socket);
        this.menu = new UserMenu(this);
        this.subscriptionService = new SubscriptionService(user, configType);
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
        subscriptionService.addSubscription();
    }

}
