package server.firstleveldomainservices.secondleveldomainservices.subscriptionservice;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lock.MonthlyPlanLockManager;
import server.authservice.User;
import server.data.facade.FacadeHub;
import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityInfo;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.DailyPlan;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.ioservice.objectformatter.IIObjectFormatter;
import server.ioservice.objectformatter.TerminalObjectFormatter;
import server.utils.ConfigType;
import server.utils.Configs;

public class SubscriptionService {

    private static final int DAYS_BEFORE_SUBSCRIPTION_CLOSURE = 3;

    private final ConfigType configType;
    private final IInputOutput ioService = new IOService();
    private final IIObjectFormatter<String> objectFormatter = new TerminalObjectFormatter();
    private User user;
    private FacadeHub data;

    public SubscriptionService(User user, ConfigType configType,FacadeHub data) {
        this.user = user;
        this.configType = configType;
        this.data = data;
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

            DailyPlan dailyPlan = data.getMonthlyPlanFacade().getDailyPlanOfTheChosenDay(day);
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

            int subscriptionCode = data.getMonthlyConfigFacade().getCurrentSubCode();

            Subscription subscription = new Subscription(
                userName,
                numberOfSubscriptions,
                activityName,
                subscriptionCode,
                LocalDate.now(),
                data.getMonthlyPlanFacade().getFullDateOfChosenDay(day)
            );

            DailyPlan updatedDailyPlan = updateDailyPlan(dailyPlan, activityInfo, subscription, subscriptionCode, activityName);
            LocalDate dateOfSubscription = dailyPlan.getDate();

            data.getSubscriptionFacade().saveSubscription(subscription);
            data.getMonthlyPlanFacade().updateMonthlyPlan(dateOfSubscription, updatedDailyPlan);

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
        Activity activity = data.getActivitiesFacade().getActivity(activityTitle);
        
        if (activity == null) {
            ioService.writeMessage("\nAttività non trovata con il titolo: " + activityTitle, false);
            return null;
        }
        
        return activity;
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
        Configs configs = data.getConfigFacade().getConfig(configType);
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
        Set<Subscription> subscriptions = data.getSubscriptionFacade().getAllSubs();
        Set<Subscription> userSubscriptions = new HashSet<>();

        for (Subscription subscription : subscriptions) {
            if (subscription.getUserName().equals(user.getName())) {
                userSubscriptions.add(subscription);
            }
        }

        return userSubscriptions;
    }

    /**
     * metodo per eliminare una iscrizione
     * @param subCode
     */
    public void deleteSubscription(int subCode) {

        Subscription subscription = getSubscriptionByCode(subCode);

        ActivityInfo activityInfo = data.getMonthlyPlanFacade().getActivityInfoBasedOnSubCode(subscription);

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
        removeSubscription(subscription);
    }

    /**
     * metodo per aggiornare l'archivio delle iscrizioni
     * @param subscription
     */
    private void updateSubscriptionArchive(Subscription subscription) {
        data.getSubscriptionFacade().deleteSubscription(subscription);
        
    }

    /**
     * metodo per ottenere l'iscrizione in base al codice
     * @param subCode
     * @return
     */
    private Subscription getSubscriptionByCode(int subCode) {
        Set<Subscription> subscriptions = data.getSubscriptionFacade().getAllSubs();
        for (Subscription subscription : subscriptions) {
            if (subscription.getSubscriptionId() == subCode) {
                return subscription;
            }
        }
        return null;
    }

    /**
     * metodo per aggiornare il piano mensile dopo una eliminazione di una iscrizione
     * @param subscription
     */
    private void removeSubscription(Subscription subscription) {
        //ottengo il piano giornaliero della data dell'attività a cui sono iscritto
        DailyPlan dailyPlan = data.getMonthlyPlanFacade().getDailyPlan(subscription.getDateOfActivity());
        if(dailyPlan == null) {
            return;
        }

        //rimuovo l'iscrizione
        dailyPlan.removeSubscriptionOnActivity(subscription);

        //aggiorno il piano mensile
        data.getMonthlyPlanFacade().updateMonthlyPlan(subscription.getDateOfActivity(), dailyPlan);

    }

}
