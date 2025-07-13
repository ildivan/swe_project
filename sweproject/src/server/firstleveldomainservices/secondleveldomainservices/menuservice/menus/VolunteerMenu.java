package server.firstleveldomainservices.secondleveldomainservices.menuservice.menus;

import java.util.List;
import java.util.Map;
import server.data.facade.FacadeHub;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuManager;
import server.firstleveldomainservices.volunteerservice.VolunteerService;
import server.utils.ConfigType;
import server.utils.Configs;


public class VolunteerMenu extends MenuManager{

    private FacadeHub data;
    private ConfigType configType;

    public VolunteerMenu(VolunteerService volService,
    ConfigType configType,
    FacadeHub data) {
        super();

        this.data = data;
        this.configType = configType;

        vociVisibili.put("Mostra le mie visite del piano", true);
        vociVisibili.put("Mostra la scheda delle mie visite", true);
        vociVisibili.put("Mostra visite confermate", true);
        vociVisibili.put("Aggiungi giorno di disponobilità", true);

        chiamateMetodi.put("Mostra le mie visite del piano", volService::showMyActivities);
        chiamateMetodi.put("Mostra la scheda delle mie visite", volService::showMyActivitiesDescription);
        chiamateMetodi.put("Mostra visite confermate", volService::showMyConfirmedActivitiesDescription);
        chiamateMetodi.put("Aggiungi giorno di disponobilità", volService::addDisponibilityDate);
      
    }

    @Override
    protected Map<String,Boolean> buildMenuVisibility(Map<String, Boolean> map){
        Configs configs = data.getConfigFacade().getConfig(configType);
        if(!configs.getFirstPlanConfigured()){
            map.put("Mostra le mie visite del piano", false);
            map.put("Mostra visite confermate", false);
        }else{
            map.put("Mostra le mie visite del piano", true);
            map.put("Mostra visite confermate", true);
        }
        return map;
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
