package server.firstleveldomainservices.configuratorservice;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import com.google.gson.JsonObject;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuService;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.menus.EditMenu;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlanService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.monthlyconfig.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.monthlyconfig.PlanState;
import server.ioservice.IInputOutput;
import server.ioservice.IOService;
import server.ioservice.objectformatter.IIObjectFormatter;
import server.ioservice.objectformatter.TerminalObjectFormatter;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;
import server.utils.ConfigType;
import server.utils.Configs;
import server.utils.MainService;

public class EditPossibilitiesService extends MainService<Void>{

    private static final String CLEAR = "CLEAR";
    private static final String SPACE = "SPACE";
    private static final String MONTHLY_CONFIG_KEY = "current";

    private final MenuService menu = new EditMenu(this);

    private final ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory = new JsonLocInfoFactory();
    private final IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private final IInputOutput ioService = new IOService();
    private final IIObjectFormatter<String> formatter= new TerminalObjectFormatter();
    private final IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();
    private ConfigType configType;


    public EditPossibilitiesService(Socket socket, ConfigType configType) {
        super(socket);
        this.configType = configType;
    }


    @Override
    protected Void applyLogic() throws IOException {
        
        MonthlyConfig monthlyConfig = getMonthlyConfig();

        if(!monthlyConfig.getPlanStateMap().get(PlanState.MODIFICHE_APERTE)){
            ioService.writeMessage("\n\nFase di modifica non disponibile, piano corrente non ancora generato\n\n", false);
            return null;
        }

        doOperations();
        ioService.writeMessage("\nFase modifica dati conclusa\n", false);
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

        setIsBeingConfigured(PlanState.MODIFICHE_APERTE, false);
        setIsBeingConfigured(PlanState.DISPONIBILITA_APERTE, true);
    }

    private MonthlyConfig getMonthlyConfig(){
        MonthlyPlanService monthlyPlanService = new MonthlyPlanService();
        return monthlyPlanService.getMonthlyConfig();
    }

    /**
     * metodo epr modificare il fatto che si sta iniziando a modificare dati
     * @param isBeingConfigured
     */
    private void setIsBeingConfigured(PlanState isBeingConfigured, Boolean value) {
        MonthlyConfig mc = getMonthlyConfig();
        Map<PlanState, Boolean> stateMap = mc.getPlanStateMap();
        stateMap.put(isBeingConfigured, value);
        mc.setPlanStateMap(stateMap);
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyConfigLocInfo();
        locInfo.setKey(MONTHLY_CONFIG_KEY);
        dataLayer.modify(jsonFactoryService.createJson(mc), locInfo);

    }

        /**
     * Modifica il numero massimo di iscrizioni contemporanee per una iniziativa.
     *
     * @pre Il sistema deve essere correttamente configurato e l’utente deve inserire un numero valido tra 1 e 50.
     * @post Il nuovo numero massimo è salvato nella configurazione.
     */
    public void modNumMaxSub(){
        ioService.writeMessage(CLEAR,false);
        int n = ioService.readIntegerWithMinMax("\nInserire nuovo numero di iscrizioni massime (massimo numero 50)",1,50);

        assert n >= 1 && n <= 50 : "Valore non valido per il numero massimo di iscrizioni";

        Configs configs = getConfig();

        assert configs != null : "Configurazione non trovata";

        configs.setMaxSubscriptions(n);
        JsonObject newConfigsJO = jsonFactoryService.createJson(configs);

        JsonDataLocalizationInformation locInfo = locInfoFactory.getConfigLocInfo();
        locInfo.setKey(configType.getValue());
        
        boolean modified = dataLayer.modify(newConfigsJO, locInfo);

        assert modified : "Modifica configurazione fallita";

        ioService.writeMessage("\nNumero massimo di iscrizioni modificato", false);
    }

    private Configs getConfig(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getConfigLocInfo();

        locInfo.setKey(configType.getValue());

        JsonObject cJO = dataLayer.get(locInfo);
        return jsonFactoryService.createObject(cJO, Configs.class);
    }

    /**
     * metodo per modificare una attivita
     */
    public void modActivity(){
        ioService.writeMessage(CLEAR,false);    
        ioService.writeMessage("\nModifica attivita non ancora implementata\n", false);
    }

    /**
     * metodo per modificare un luogo
     */
    public void modPlace(){
        ioService.writeMessage(CLEAR,false);    
        ioService.writeMessage("\nModifica luogo non ancora implementata\n", false);
    }

    
}
