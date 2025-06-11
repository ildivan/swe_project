package server.firstleveldomainservices.secondleveldomainservices.menuservice.menus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonObject;
import server.DateService;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.firstleveldomainservices.configuratorservice.ConfigService;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuManager;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyConfig;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;

public class ConfiguratorMenu extends MenuManager{

    
    private static final String MONTHLY_CONFIG_CURRENT_KEY = "current";

    private transient ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory = new JsonLocInfoFactory();
    private transient DateService dateService = new DateService();
    private transient IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private transient IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();


    public ConfiguratorMenu(ConfigService configService) {
        super();
        vociVisibili.put("Aggiungi Volontario", true);
        vociVisibili.put("Aggiungi Luogo", true);
        vociVisibili.put("Aggiungi Attività", true);
        vociVisibili.put("Aggiungi data preclusa", true);
        vociVisibili.put("Mostra Volontari", true);
        vociVisibili.put("Mostra Luoghi", true);
        vociVisibili.put("Mostra Bozza delle Attività", true);
        vociVisibili.put("Mostra Attività Proposte", true);
        vociVisibili.put("Mostra Attività Confermata", true);
        vociVisibili.put("Mostra Attività Completa", true);
        vociVisibili.put("Mostra Attività Cancellata", true);
        vociVisibili.put("Mostra Attività Effettuata", true);
        vociVisibili.put("Modifica numero massimo di persone iscrivibili mediante una singola iscrizione", true);
        vociVisibili.put("Genera Piano Mensile", true);
        
        chiamateMetodi.put("Aggiungi Volontario", configService::addVolunteer);
        chiamateMetodi.put("Aggiungi Luogo", configService::addPlace);
        chiamateMetodi.put("Aggiungi Attività", configService::addActivity);
        chiamateMetodi.put("Aggiungi data preclusa", configService::addNonUsableDate);
        chiamateMetodi.put("Mostra Volontari", configService::showVolunteers);
        chiamateMetodi.put("Mostra Luoghi", configService::showPlaces);
        chiamateMetodi.put("Mostra Bozza delle Attività", configService::showActivities);
        chiamateMetodi.put("Mostra Attività Proposte", () -> configService.showActivitiesWithCondition(ActivityState.PROPOSTA));
        chiamateMetodi.put("Mostra Attività Confermata", () -> configService.showActivitiesWithCondition(ActivityState.CONFERMATA));
        chiamateMetodi.put("Mostra Attività Completa", () -> configService.showActivitiesWithCondition(ActivityState.COMPLETA));
        chiamateMetodi.put("Mostra Attività Cancellata", () -> configService.showActivitiesWithCondition(ActivityState.CANCELLATA));
        chiamateMetodi.put("Mostra Attività Effettuata", () -> configService.showActivitiesWithCondition(ActivityState.EFFETTUATA));
        chiamateMetodi.put("Modifica numero massimo di persone iscrivibili mediante una singola iscrizione", configService::modNumMaxSub);
        chiamateMetodi.put("Genera Piano Mensile", configService::generateMonthlyPlan);
    }

    @Override
    protected Map<String,Boolean> buildMenuVisibility(Map<String, Boolean> map){
        MonthlyConfig mc = geMonthlyConfig();
        
        if(dateService.getTodayDate().equals(mc.getMonthAndYear()) && checkIfAlredyBuildPlan()){
            map.put("Genera Piano Mensile", true);
            //fare che se il piano non è stato generato il 16 laprima cosa da fare è quella, vanno osxurate tutte le altre voci
            return map;
        }else{
            map.put("Genera Piano Mensile", false);
            return map;
        }
    }

    /**
     * metodo che controlla se ho gia creato il piano, se è gia stato creato allora
     * non lo posso ricreare, ecco perche ritorna false se il piano è gia stato configurato
     * @return
     */
    private boolean checkIfAlredyBuildPlan(){
        MonthlyConfig mc = geMonthlyConfig();
        LocalDate dateOfNextPlan = mc.getMonthAndYear();
        //come oggetto Boolean e non come tipo primitivo cosi da poter gestire il caso null
        Boolean planConfigured = mc.isPlanConfigured().get(dateOfNextPlan);


        if (planConfigured == null){
            return false;
        }
        if(planConfigured){
            return false;
        }else{
            return true;
        }
    }

    private MonthlyConfig geMonthlyConfig(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyConfigLocInfo();
        locInfo.setKey(MONTHLY_CONFIG_CURRENT_KEY);

        JsonObject mcJO = dataLayer.get(locInfo);
        return jsonFactoryService.createObject(mcJO, MonthlyConfig.class);
    }

    /**
     * convert the menu to a string
     * @param menu recive the list of the menu option to print on the terminal for the user
     * @return
     */
    public String toString(List<String> menu) {
        StringBuffer menuOut = new StringBuffer();
        menuOut.append("\n\n\n------------------\n\n\n");
        menuOut.append("BENVENUTO NEL MENU CONFIGURATORE!");
        menuOut.append("\n\nSelezionare una opzione:\n");
        menuOut.append(obtainMenuString("\n\nFunzioni di aggiunta:\n\n", "Aggiungi", menu));
        menuOut.append(obtainMenuString("\n\nFunzioni di visualizzazione:\n\n", "Mostra", menu));
        menuOut.append(obtainMenuString("\n\nFunzioni di modifica:\n\n", "Modifica", menu));
        menuOut.append(obtainMenuString("\n\nFunzioni di generazione:\n\n", "Genera", menu));

        return menuOut.toString();
    }

    private String obtainMenuString(String Desc, String key, List<String> menu){ 
        StringBuffer menuOut = new StringBuffer();
        menuOut.append(Desc);
        for (int i = 0; i < menu.size(); i++) {
            if(menu.get(i).contains(key)){
                menuOut.append((i + 1) + ") " + menu.get(i)+"\n");
            }
        }
        return menuOut.toString();
    }
    
}
