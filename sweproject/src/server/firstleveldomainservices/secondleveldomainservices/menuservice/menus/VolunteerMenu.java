package server.firstleveldomainservices.secondleveldomainservices.menuservice.menus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonObject;
import server.DateService;
import server.datalayerservice.DataLayerDispatcherService;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuManager;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyConfig;
import server.firstleveldomainservices.volunteerservice.VolunteerService;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;


public class VolunteerMenu extends MenuManager{

    private static final String MONTHLY_CONFIG_CURRENT_KEY = "current";

    private transient ILocInfoFactory locInfoFactory = new JsonLocInfoFactory();
    private transient IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private transient DateService dateService = new DateService();

    public VolunteerMenu(VolunteerService volService) {
        super();
        vociVisibili.put("Mostra le mie visite del piano", true);
        vociVisibili.put("Mostra la scheda delle mie visite", true);
        vociVisibili.put("Aggiungi giorno di non disponobilità", true);

        chiamateMetodi.put("Mostra le mie visite del piano", volService::showMyActivities);
        chiamateMetodi.put("Mostra la scheda delle mie visite", volService::showMyActivitiesDescription);
        chiamateMetodi.put("Aggiungi giorno di non disponobilità", volService::addPrecludeDate);
      
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

        JsonObject mcJO = DataLayerDispatcherService.startWithResult(locInfo, layer -> layer.get(locInfo));
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
        menuOut.append("BENVENUTO NEL MENU VOLONTARIO!");
        menuOut.append("\n\nSelezionare una opzione:\n");
        menuOut.append(obtainMenuString("\n\nFunzioni di visualizzazione:\n\n", "Mostra", menu));
        menuOut.append(obtainMenuString("\n\nFunzioni di aggiunta:\n\n", "Aggiungi", menu));

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
