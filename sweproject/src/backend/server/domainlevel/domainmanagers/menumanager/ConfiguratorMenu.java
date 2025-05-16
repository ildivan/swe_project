package backend.server.domainlevel.domainmanagers.menumanager;

import java.util.List;
import java.util.Map;

import backend.server.domainlevel.domainservices.ConfigService;
import backend.server.utils.DateUtil;

public class ConfiguratorMenu extends MenuManager{

    public ConfiguratorMenu(ConfigService configService) {
        super();
        vociVisibili.put("Aggiungi Volontario", true);
        vociVisibili.put("Aggiungi Luogo", true);
        vociVisibili.put("Aggiungi Attività", true);
        vociVisibili.put("Aggiungi data preclusa", true);
        vociVisibili.put("Mostra Volontari", true);
        vociVisibili.put("Mostra Luoghi", true);
        vociVisibili.put("Mostra Attività", true);
        vociVisibili.put("Modifica numero massimo di persone iscrivibili mediante una singola iscrizione", true);
        vociVisibili.put("Genera Piano Mensile", true);
        
        chiamateMetodi.put("Aggiungi Volontario", configService::addVolunteer);
        chiamateMetodi.put("Aggiungi Luogo", configService::addPlace);
        chiamateMetodi.put("Aggiungi Attività", configService::addActivity);
        chiamateMetodi.put("Aggiungi data preclusa", configService::addNonUsableDate);
        chiamateMetodi.put("Mostra Volontari", configService::showVolunteers);
        chiamateMetodi.put("Mostra Luoghi", configService::showPlaces);
        chiamateMetodi.put("Mostra Attività", configService::showActivities);
        chiamateMetodi.put("Modifica numero massimo di persone iscrivibili mediante una singola iscrizione", configService::modNumMaxSub);
        chiamateMetodi.put("Genera Piano Mensile", configService::generateMonthlyPlan);
    }

    @Override
    protected Map<String,Boolean> buildMenuVisibility(Map<String, Boolean> map){
        if(DateUtil.getTodayDate().getDayOfMonth()==16){
            map.put("Genera Piano Mensile", true);
            //fare che se il piano non è stato generato il 16 laprima cosa da fare è quella, vanno osxurate tutte le altre voci
            return map;
        }else{
            map.put("Genera Piano Mensile", false);
            return map;
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
        menuOut.append("BENVENUTO NEL MENU!");
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
