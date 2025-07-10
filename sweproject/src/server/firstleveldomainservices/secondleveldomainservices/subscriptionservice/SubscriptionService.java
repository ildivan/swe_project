package server.firstleveldomainservices.secondleveldomainservices.subscriptionservice;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;

import lock.MonthlyPlanLockManager;
import server.authservice.User;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityInfo;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.DailyPlan;
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

    private static final int DAYS_BEFORE_SUBSCRIPTION_CLOSURE = 3;

    private static final String SUBSCRIPTION_KEY_DESC = "subscriptionId";
    private final ConfigType configType;
    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private transient IJsonLocInfoFactory locInfoFactory;
    private transient JsonDataLayer dataLayer;
    private final IInputOutput ioService = new IOService();
    private final MonthlyConfigService monthlyConfigService;
    private final IIObjectFormatter<String> objectFormatter = new TerminalObjectFormatter();
    private final MonthlyPlanService monthlyPlanService;
    private User user;

    public SubscriptionService(User user, IJsonLocInfoFactory locInfoFactory, ConfigType configType,
    JsonDataLayer dataLayer) {
        this.user = user;
        this.configType = configType;
        this.locInfoFactory = locInfoFactory;
        this.dataLayer = dataLayer;
        this.monthlyPlanService = new MonthlyPlanService(locInfoFactory, configType, dataLayer);
        this.monthlyConfigService = new MonthlyConfigService(locInfoFactory, dataLayer);
    }

    /**
     * metodo per aggiungere una iscrizione
     */
    public void addSubscription() {
        boolean locked = false;
        try {
            // Provo a prendere il lock, aspetto massimo 2 secondi
            locked = MonthlyPlanLockManager.tryLock(2, TimeUnit.SECONDS);
            if (!locked) {
                ioService.writeMessage("Sistema occupato. Riprova tra qualche secondo: il demone è in esecuzione.", false);
                return;
            }

            int day = ioService.readIntegerWithMinMax("Inserisci il giorno per la sottoscrizione (1-31): ", 1, 31);

            DailyPlan dailyPlan = monthlyPlanService.getDailyPlanOfTheChosenDay(day);
            if (dailyPlan == null) {
                ioService.writeMessage("Impossibile trovare attività per la data selezionata.", false);
                return;
            }

            String activityName = choseActivity(day, dailyPlan); 
            ActivityInfo activityInfo = getActInfoOfTheChosenActivity(dailyPlan, activityName);

            if (activityInfo == null) {
                return;
            }

            if (activityInfo.getState() != ActivityState.PROPOSTA) {
                ioService.writeMessage(String.format("Impossibile iscriversi a questa visita:\nMotivo: %s", getErrorMessagebaseOnState(activityInfo)), false);
                return;
            }

            if (!isInTimeToSubscribe(activityInfo, dailyPlan.getDate())) {
                ioService.writeMessage("Impossibile iscriversi a questa visita: SUPERATA LA DATA DI TERMINE DELLE ISCRIZIONI", false);
                return;
            }

            String userName = user.getName();
            int numberOfSubscriptions = ioService.readIntegerWithMinMax("\nInserisci il numero di iscrizioni: ", 1, getMaxNumberOfSubscriptions());

            if (!checkIfCanSubscribeEveryone(numberOfSubscriptions, activityName, activityInfo)) {
                ioService.writeMessage("Impossibile iscriversi a questa visita: MASSIMO NUMERO DI ISCRITTI SUPERATO", false);
                return;
            }

            int subscriptionCode = monthlyConfigService.getCurrentSubCode();

            Subscription subscription = new Subscription(
                userName,
                numberOfSubscriptions,
                activityName,
                subscriptionCode,
                LocalDate.now(),
                monthlyPlanService.getFullDateOfChosenDay(day)
            );

            DailyPlan updatedDailyPlan = updateDailyPlan(dailyPlan, activityInfo, subscription, subscriptionCode, activityName);
            LocalDate dateOfSubscription = dailyPlan.getDate();

            saveSubscription(subscription);
            monthlyPlanService.updateMonthlyPlan(dateOfSubscription, updatedDailyPlan);

            ioService.writeMessage("Sottoscrizione aggiunta con successo!", false);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ioService.writeMessage("Operazione interrotta.", false);
        } finally {
            if (locked && MonthlyPlanLockManager.isHeldByCurrentThread()) {
                MonthlyPlanLockManager.unlock();
            }
        }
    }


    /**
     * metodo che controlla se sono in tempo per iscrivermi
     * 
     * @param activityInfo
     * @param dateOfActivity
     * @return
     */
    private boolean isInTimeToSubscribe(ActivityInfo activityInfo, LocalDate dateOfActivity) {
        if((ChronoUnit.DAYS.between(LocalDate.now(), dateOfActivity))>=DAYS_BEFORE_SUBSCRIPTION_CLOSURE){
            return true;
        }

        return false;
    
    }

    /**
     * metodo per controllare s enon supero il numero massimo di iscritti
     * @param numberOfSubscriptions
     * @param activityName
     * @return
     */
    private boolean checkIfCanSubscribeEveryone(int numberOfSubscriptions, String activityName, ActivityInfo activityInfo) {
        Activity activity = localizeActivity(activityName);

        if((activityInfo.getNumberOfSub()+numberOfSubscriptions)>activity.getMaxPartecipanti()){
            return false;
        }

        return true;

    }

    /**
     * metodo util di checkIfCanSubscribeEveryone,
     * per ottenere l'attività dato il titolo
     * @param activityTitle
     * @return
     */
    private Activity localizeActivity(String activityTitle) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getActivityLocInfo();
        locInfo.setKey(activityTitle);
        JsonObject activityJO = dataLayer.get(locInfo);
        
        if (activityJO == null) {
            ioService.writeMessage("\nAttività non trovata con il titolo: " + activityTitle, false);
            return null;
        }
        
        return jsonFactoryService.createObject(activityJO, Activity.class);
    }

    /**
     * metodo per ottenere errore in base allo stato della visita
     * @param activityInfo
     * @return
     */
    private String getErrorMessagebaseOnState(ActivityInfo activityInfo) {
        //Posssibile refactor tramite polimorfismo
        String message;
        switch (activityInfo.getState()) {
            case CANCELLATA:
                message = "Visita cancellata";
                break;
            case COMPLETA:
                message = "Visita completa";
                break;

            case CONFERMATA:
                message = "Visita confermata";
                break;
            default:
                message = "";
                break;
        }

        return message;
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
        Set<Subscription> userSubscriptions = new HashSet<>();

        for (Subscription subscription : subscriptions) {
            if (subscription.getUserName().equals(user.getName())) {
                userSubscriptions.add(subscription);
            }
        }

        return userSubscriptions;
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

        ActivityInfo activityInfo = monthlyPlanService.getActivityInfoBasedOnSubCode(subscription);

        if (!(activityInfo.getState() == ActivityState.PROPOSTA || activityInfo.getState() == ActivityState.COMPLETA)){
            ioService.writeMessage(String.format("Impossibile eliminare iscrizione a questa visita:\nMotivo: %s",getErrorMessagebaseOnState(activityInfo)), false);
            return;
        }

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
