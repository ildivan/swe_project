package server.firstleveldomainservices.secondleveldomainservices.menuservice.menus;

import java.util.List;
import java.util.Map;
import server.firstleveldomainservices.secondleveldomainservices.menuservice.MenuManager;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.userservice.UserService;


public class UserMenu extends MenuManager{

    public UserMenu(UserService userService) {
        super();
        vociVisibili.put("Aggiungi iscrizione", true);
        vociVisibili.put("Mostra Attività Proposte", true);
        vociVisibili.put("Mostra Attività Confermata", true);
        vociVisibili.put("Mostra Attività Cancellata", true);
        vociVisibili.put("Mostra iscrizioni effettuate", true);
        vociVisibili.put("Cancellazione", true);
        
        
        chiamateMetodi.put("Aggiungi iscrizione", userService::addSubscription);
        chiamateMetodi.put("Mostra Attività Proposte", () -> userService.showActivitiesWithCondition(ActivityState.PROPOSTA));
        chiamateMetodi.put("Mostra Attività Confermata", () -> userService.showActivitiesWithCondition(ActivityState.CONFERMATA));
        chiamateMetodi.put("Mostra Attività Cancellata", () -> userService.showActivitiesWithCondition(ActivityState.CANCELLATA));
        chiamateMetodi.put("Mostra iscrizioni effettuate", userService::showSubscriptions);
        chiamateMetodi.put("Cancellazione", userService::deleteSubscription);
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
        menuOut.append("BENVENUTO NEL MENU FRUITORE!");
        menuOut.append("\n\nSelezionare una opzione:\n");
        menuOut.append(obtainMenuString("\n\nFunzioni di iscrizione:\n\n", "iscrizione", menu));
        menuOut.append(obtainMenuString("\n\nFunzioni di visualizzazione:\n\n", "Mostra", menu));
        menuOut.append(obtainMenuString("\n\nFunzioni di cancellazione:\n\n", "Cancellazione", menu));

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
