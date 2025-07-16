package server.firstleveldomainservices.userservice;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;

import server.authservice.User;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuService;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.menus.UserMenu;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityRecord;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.Subscription;
import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.SubscriptionService;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.ioservice.objectformatter.IIObjectFormatter;
import server.ioservice.objectformatter.TerminalObjectFormatter;
import server.utils.ActivityUtil;
import server.utils.ConfigType;
import server.utils.MainService;

public class UserService extends MainService<Void> {

    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";

    private final MenuService menu; 
    private final IInputOutput ioService = new IOService();
    private final SubscriptionService subscriptionService;
    private final ActivityUtil activityUtil;
    private final IIObjectFormatter<String> formatter = new TerminalObjectFormatter();


    public UserService(Socket socket, User user, ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory, ConfigType configType) {
        super(socket);
        this.menu = new UserMenu(this);
        this.subscriptionService = new SubscriptionService(user, locInfoFactory, configType);
        this.activityUtil = new ActivityUtil(locInfoFactory, configType);
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

    /**
     * metodo per aggiungere una iscrizione
     */
    public void addSubscription() {
        subscriptionService.addSubscription();
    }

     /**
     * mostra le visite in base allo stato richiesto
     * @param desiredState stato delle visite che vuoi visualizzare
     */
    public void showActivitiesWithCondition(ActivityState desiredState) {
        
        List<ActivityRecord> result = activityUtil.getActiviyByState(desiredState);
    
        ioService.writeMessage(formatter.formatListActivityRecord(result), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * metotodo per visualizzare le iscrizioni effettuate dall'utente
     */
    public void showSubscriptions(){
        Set<Subscription> subscriptions = subscriptionService.getSubscriptionsForUser();
        if(subscriptions.isEmpty()){
            ioService.writeMessage("Non hai sottoscrizioni attive.", false);
            return;
        }
        ioService.writeMessage(formatter.formatListSubscription(subscriptions), false);
        ioService.writeMessage(SPACE,false);
    }

    /**
     * metodo per eliminare una iscrizione
     */
    public void deleteSubscription() {
        int subCode;
        ioService.writeMessage("\n\nIscrizioni effettuate:\n", false);
        showSubscriptions();
        subCode = getSubCode();
        subscriptionService.deleteSubscription(subCode);
    }

    /**
     * metodo per ottenre il codice dell'iscrizione da rimuovere
     * @return
     */
    private int getSubCode() {
        return ioService.readInteger("Inserisci il codice dell'iscrizione da rimuovere: ");
    }

}
