package server.firstleveldomainservices.secondleveldomainservices.subscriptionservice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

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


    public void addSubscription() {

        int day = ioService.readIntegerWithMinMax("Inserisci il giorno per la sottoscrizione (1-31): ",1,31);

        DailyPlan dailyPlan = getDailyPlanOfTheChosenDay(day);

        String activityName  = choseActivity(day, dailyPlan); 

        ActivityInfo activityInfo = getActInfoOfTheChosenActivity(dailyPlan, activityName);

        if(activityInfo == null) {
            return;
        }

        String userName = user.getName();
        int subscriptionCode = monthlyConfigService.getCurrentSubCode();
        int numberOfSubscriptions = ioService.readIntegerWithMinMax("\nInserisci il numero di iscrizioni: ",1,getMaxNumberOfSubscriptions());

        Subscription subscription = new Subscription(userName, numberOfSubscriptions);

        DailyPlan updatedDailyPlan = updateDailyPlan(dailyPlan, activityInfo, subscription, subscriptionCode, activityName);
    
        LocalDate dateOfSubscription = dailyPlan.getDate(); //ottengo la data del piano giornaliero a cui mi sto iscrivendo
        //cosi da poter aggiurnare il piano mensile
        
        monthlyPlanService.updateMonthlyPlan(dateOfSubscription, updatedDailyPlan);

        ioService.writeMessage("Sottoscrizione aggiunta con successo!", false);
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
     * metodo per ottenere le informazoni delle attività del giorno scelto
     * @param day
     * @return
     */
    private DailyPlan getDailyPlanOfTheChosenDay(int day) {
        DateService dateService = new DateService();
        MonthlyPlanService monthlyPlanService = new MonthlyPlanService();
        MonthlyPlan monthlyPlan = monthlyPlanService.getMonthlyPlan();
        Map<LocalDate,DailyPlan> monthlyMap = monthlyPlan.getMonthlyPlan();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String dateString = monthlyPlanService.getMonthlyPlanDate();
        LocalDate dateOfPlan = LocalDate.parse(dateString, formatter); //data del piano, uso questa data per ottenere il mese e l'anno

        int chosenMonth = dateService.setMonthOnDayOfSubscription(dateOfPlan, day);
        int chosenYear = dateService.setYearOnDayOfSubscription(dateOfPlan, day);

        LocalDate data = LocalDate.of(chosenYear, chosenMonth, day); //data del giorno scelto

        DailyPlan dailyPlan;

        if(monthlyMap.containsKey(data)) {
            if(monthlyMap.get(data) == null) {
                ioService.writeMessage("Piano mensile nullo per la data selezionata.", false);
                return null;
            }
            dailyPlan = monthlyMap.get(data);

        } else {
            ioService.writeMessage("Piano mensile non trovato per la data selezionata.", false);
            return null;
        }


       return dailyPlan;
        
            
    }


}
