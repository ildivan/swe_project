package server.firstleveldomainservices.secondleveldomainservices.menuservice.menus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import server.DateService;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.firstleveldomainservices.configuratorservice.ConfigService;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuManager;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfigService;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.utils.ConfigType;
import server.utils.Configs;
import server.utils.ConfigsUtil;

public class ConfiguratorMenu extends MenuManager{
  
    private DateService dateService = new DateService();
    private MonthlyConfigService monthlyConfigService;
    private ConfigsUtil configsUtil;



    public ConfiguratorMenu(ConfigService configService, ConfigType configType,
    MonthlyConfigService monthlyConfigService,
    ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory,
    IDataLayer<JsonDataLocalizationInformation> dataLayer) {
        super();

        this.monthlyConfigService = monthlyConfigService;
        this.configsUtil = new ConfigsUtil(locInfoFactory, configType, dataLayer);

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
        vociVisibili.put("Mostra Piano Mensile", true);
        vociVisibili.put("Modifica dati", true);
        vociVisibili.put("Genera Piano Mensile", true);
        vociVisibili.put("Elimina Volontario", true);
        vociVisibili.put("Elimina Luogo", true);
        vociVisibili.put("Elimina Attività", true);
       
        
        
        chiamateMetodi.put("Aggiungi Volontario", () -> configService.addVolunteer(false));
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
        chiamateMetodi.put("Mostra Piano Mensile", configService::showMonthlyPlan);
        chiamateMetodi.put("Modifica dati", () -> configService.modifyData(configType));
        chiamateMetodi.put("Genera Piano Mensile", configService::generateMonthlyPlan);
        chiamateMetodi.put("Elimina Volontario", configService::deleteVolunteer);
        chiamateMetodi.put("Elimina Luogo", configService::deletePlace);
        chiamateMetodi.put("Elimina Attività", configService::deleteActivity);

        
    }

    @Override
    protected Map<String,Boolean> buildMenuVisibility(Map<String, Boolean> map){
        Configs configs = configsUtil.getConfig();
        MonthlyConfig mc = monthlyConfigService.getMonthlyConfig();
        
        if(correctDateToGeneratePlan(mc)&& checkIfAlredyBuildPlan()){
            map.put("Genera Piano Mensile", true);
            //fare che se il piano non è stato generato il 16 laprima cosa da fare è quella, vanno osxurate tutte le altre voci
        }else{
            map.put("Genera Piano Mensile", false);
        }

        if(!configs.getFirstPlanConfigured()){
            map.put("Aggiungi Luogo", false);
            map.put("Aggiungi Attività", false);
            map.put("Mostra Attività Proposte", false);
            map.put("Mostra Attività Confermata", false);
            map.put("Mostra Attività Completa", false);
            map.put("Mostra Attività Cancellata", false);
            map.put("Mostra Attività Effettuata", false);
            map.put("Mostra Piano Mensile", false);
            map.put("Modifica dati", false);
            map.put("Elimina Volontario", false);
            map.put("Elimina Luogo", false);
            map.put("Elimina Attività", false);
        }else{
            map.put("Aggiungi Luogo", true);
            map.put("Aggiungi Attività", true);
            map.put("Mostra Attività Proposte", true);
            map.put("Mostra Attività Confermata", true);
            map.put("Mostra Attività Completa", true);
            map.put("Mostra Attività Cancellata", true);
            map.put("Mostra Attività Effettuata", true);
            map.put("Mostra Piano Mensile", true);
            map.put("Modifica dati", true);
            map.put("Elimina Volontario", true);
            map.put("Elimina Luogo", true);
            map.put("Elimina Attività", true);
        }

        return map;
    }

    /**
     * metodo che controlla che mi trovi nella data corretta per generare il piano
     * se il 16 è un gionro lavoroativo allora ok,
     * altrimenti vado al primo giorno lavorativo (no sabato ne domenica
     * 
     * @param mc
     * @return
     */
    private boolean correctDateToGeneratePlan(MonthlyConfig mc) {
        LocalDate correctDate = getCorrectDateForPlan(mc.getMonthAndYear());
        if(dateService.getTodayDate().equals(correctDate)){
            return true;
        }

        return false;
    }

    /**
     * metodo per ottenre la orreetta data di generazone del piano
     * controllo se il 16 è giorno lavorativo altrimenti sposto
     * @param teoricalDate
     * @return
     */
    private LocalDate getCorrectDateForPlan(LocalDate teoricalDate) {

        if (teoricalDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return teoricalDate.plusDays(2);
        } else if (teoricalDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return teoricalDate.plusDays(1);
        }

        return teoricalDate;
    }

    /**
     * metodo che controlla se ho gia creato il piano, se è gia stato creato allora
     * non lo posso ricreare, ecco perche ritorna false se il piano è gia stato configurato
     * @return
     */
    private boolean checkIfAlredyBuildPlan(){
        MonthlyConfig mc = monthlyConfigService.getMonthlyConfig();
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
        menuOut.append(obtainMenuString("\n\nFunzioni di eliminazione:\n\n", "Elimina", menu));

        return menuOut.toString();
    }

    private String obtainMenuString(String desc, String key, List<String> menu){ 
        StringBuffer menuOut = new StringBuffer();
        if(!checkIfNullMenuList(desc, key, menu)){
        
            menuOut.append(desc);
            for (int i = 0; i < menu.size(); i++) {
                if(menu.get(i).contains(key)){
                    menuOut.append((i + 1) + ") " + menu.get(i)+"\n");
                }
            }
        }
        return menuOut.toString();
    }

    /**
     * metodo che controlla se la lista contiene una voce (contiene key) della categoria indicata (desc)
     * @param desc
     * @param key
     * @param menu
     * @return
     */
    private boolean checkIfNullMenuList(String desc, String key, List<String> menu) {
        for (int i = 0; i < menu.size(); i++) {
            if(menu.get(i).contains(key)){
                return false;
            }
        }
        return true;
    }
    
}
