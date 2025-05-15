package server.firstleveldomainservices.secondleveldomainservices.menuservice.menus;

import java.util.List;
import java.util.Map;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuManager;
import server.firstleveldomainservices.volunteerservice.VolunteerService;


public class VolunteerMenu extends MenuManager{

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
        // qua possono essere implementate condizioni sulla visibilità delle varie voci del 
        //menu del volontario, mettendo a false il value nella map voci visibili si oscura
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
