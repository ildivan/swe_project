package server.firstleveldomainservices.secondleveldomainservices.subscriptionservice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;

import server.DateService;
import server.authservice.User;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityInfo;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.DailyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlanService;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.ioservice.objectformatter.IIObjectFormatter;
import server.ioservice.objectformatter.TerminalObjectFormatter;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;
import server.utils.ConfigType;
import server.utils.Configs;

public class SubscriptionService {

    private static final String SUBSCRIPTION_KEY_DESC = "subscriptionId";
    private final ConfigType configType;
    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private transient ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory = new JsonLocInfoFactory();
    private transient IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();
    private final IInputOutput ioService = new IOService();
    private final MonthlyConfigService monthlyConfigService = new MonthlyConfigService();
    private final IIObjectFormatter<String> objectFormatter = new TerminalObjectFormatter();
    private final MonthlyPlanService monthlyPlanService = new MonthlyPlanService();
    private User user;

    public SubscriptionService(User user, ConfigType configType) {
        this.user = user;
        this.configType = configType;
    }

    /**
     * metodo per aggiungere una iscrizione
     */
    public void addSubscription() {

        int day = ioService.readIntegerWithMinMax("Inserisci il giorno per la sottoscrizione (1-31): ",1,31);

        DailyPlan dailyPlan = monthlyPlanService.getDailyPlanOfTheChosenDay(day);
        if(dailyPlan == null) {
            ioService.writeMessage("Impossibile trovare attività per la data selezionata per la data selezionata.", false);
            return;
        }

        String activityName  = choseActivity(day, dailyPlan); 

        ActivityInfo activityInfo = getActInfoOfTheChosenActivity(dailyPlan, activityName);

        if(activityInfo == null) {
            return;
        }

        String userName = user.getName();
        int subscriptionCode = monthlyConfigService.getCurrentSubCode();
        int numberOfSubscriptions = ioService.readIntegerWithMinMax("\nInserisci il numero di iscrizioni: ",1,getMaxNumberOfSubscriptions());

        Subscription subscription = new Subscription(userName, numberOfSubscriptions, activityName, subscriptionCode, LocalDate.now(), monthlyPlanService.getFullDateOfChosenDay(day));

        DailyPlan updatedDailyPlan = updateDailyPlan(dailyPlan, activityInfo, subscription, subscriptionCode, activityName);
    
        LocalDate dateOfSubscription = dailyPlan.getDate(); //ottengo la data del piano giornaliero a cui mi sto iscrivendo
        //cosi da poter aggiurnare il piano mensile
        
        saveSubscription(subscription);
        monthlyPlanService.updateMonthlyPlan(dateOfSubscription, updatedDailyPlan);

        ioService.writeMessage("Sottoscrizione aggiunta con successo!", false);
    }

    /**
     * metodo per salvare l'iscrizione
     * @param subscription
     */
    private void saveSubscription(Subscription subscription) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getSubscriptionLocInfo();
        dataLayer.add(jsonFactoryService.createJson(subscription), locInfo);
    }


    /**
     * aggiorna il piano giornaliero
     * @param dailyPlan
     * @param activityInfo
     * @return
     */
    private DailyPlan updateDailyPlan(DailyPlan dailyPlan, ActivityInfo activityInfo, Subscription subscription, int subscriptionCode, String activityName) {
        activityInfo.addSubscription(subscriptionCode, subscription);
        dailyPlan.getPlan().put(activityName, activityInfo);
        return dailyPlan;

    }


    /**
     * metodo per ottenre il massimo numero di iscrizioni possibili in una volta sola
     * @return
     */
    private int getMaxNumberOfSubscriptions() {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getConfigLocInfo();
        locInfo.setKey(configType.getValue());
        JsonObject jsonObject = dataLayer.get(locInfo);

        Configs configs = jsonFactoryService.createObject(jsonObject, Configs.class);
        return configs.getMaxSubscriptions();
    }



    /**
     * metodo per selezionare l'attività e ottenre le sue informazioni
     * @param day
     * @return
     */
    private String choseActivity(int day, DailyPlan dailyPlan) {
        
        if(dailyPlan == null) {
            ioService.writeMessage("\nNessuna visita il giorno scelto", false);
            return null;
        }

        ioService.writeMessage(String.format("%s", objectFormatter.formatDailyPlan(dailyPlan)), false);
        String activityName = ioService.readString("Inserisci il nome dell'attività a cui vuoi iscriverti: ");

        return activityName;
    }

    /**
     * ottiene le informazioni di localizzazione dell'attività scelta
     * @param dailyPlan
     * @param activityName
     * @return
     */
    private ActivityInfo getActInfoOfTheChosenActivity(DailyPlan dailyPlan, String activityName) {
      
        for (Map.Entry<String, ActivityInfo> entry : dailyPlan.getPlan().entrySet()) {
            if(entry.getKey().equalsIgnoreCase(activityName)) {
                return entry.getValue();
            }
        }

        ioService.writeMessage("\nAttività non trovata nel piano giornaliero.", false);
        return null;
       
    }

    /**
     * metodo per ottenre le iscrizioni dell'utente
     * @return
     */
    public Set<Subscription> getSubscriptionsForUser() {
        Set<Subscription> subscriptions = getSubscriptions(); //questo metodo deve essere inserito nella facade per il datalayer

        for (Subscription subscription : subscriptions) {
            if (subscription.getUserName().equals(user.getName())) {
                subscriptions.add(subscription);
            }
        }

        return subscriptions;
    }

    /**
     * metodo per ottenrer tutte le iscrizioni
     * @return
     */
    private Set<Subscription> getSubscriptions() {
        Set<Subscription> subscriptions = new HashSet<>();
        List<JsonObject> subscriptionsJO = dataLayer.getAll(locInfoFactory.getSubscriptionLocInfo());

        for (JsonObject jsonObject : subscriptionsJO) {
            Subscription subscription = jsonFactoryService.createObject(jsonObject, Subscription.class);
            subscriptions.add(subscription);
        }

        return subscriptions;
    }

    /**
     * metodo per eliminare una iscrizione
     * @param subCode
     */
    public void deleteSubscription(int subCode) {
        Subscription subscription = getSubscriptionByCode(subCode);
        if (subscription != null) {
            updateSubscriptionArchive(subscription);
            updateMonthlyPlan(subscription);
            ioService.writeMessage("Iscrizione eliminata con successo.", false);
        } else {
            ioService.writeMessage("Iscrizione non trovata.", false);
        }
    }

    /**
     * metodo per rimuovere l'iscrizione dal piano mensile
     * @param subscription
     */
    private void updateMonthlyPlan(Subscription subscription) {
        monthlyPlanService.removeSubscription(subscription);
    }

    /**
     * metodo per aggiornare l'archivio delle iscrizioni
     * @param subscription
     */
    private void updateSubscriptionArchive(Subscription subscription) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getSubscriptionLocInfo();
        locInfo.setKeyDesc(SUBSCRIPTION_KEY_DESC);
        locInfo.setKey(String.valueOf(subscription.getSubscriptionId()));

        dataLayer.delete(locInfo);
        
    }

    /**
     * metodo per ottenere l'iscrizione in base al codice
     * @param subCode
     * @return
     */
    private Subscription getSubscriptionByCode(int subCode) {
        Set<Subscription> subscriptions = getSubscriptions();
        for (Subscription subscription : subscriptions) {
            if (subscription.getSubscriptionId() == subCode) {
                return subscription;
            }
        }
        return null;
    }

}
