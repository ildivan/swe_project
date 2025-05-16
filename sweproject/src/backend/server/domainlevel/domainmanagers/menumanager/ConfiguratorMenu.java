package backend.server.domainlevel.domainmanagers.menumanager;

import java.util.Map;

import backend.server.domainlevel.domainservices.ConfigService;
import backend.server.utils.DateUtil;

public class ConfiguratorMenu extends MenuManager{

    public ConfiguratorMenu(ConfigService configService) {
        super();
        vociVisibili.put("Modifica numero massimo di persone iscrivibili mediante una singola iscrizione", true);
        vociVisibili.put("Aggiungi Volontario", true);
        vociVisibili.put("Aggiungi Luogo", true);
        vociVisibili.put("Aggiungi Attività", true);
        vociVisibili.put("Mostra Volontari", true);
        vociVisibili.put("Mostra Luoghi", true);
        vociVisibili.put("Mostra Attività", true);
        vociVisibili.put("Aggiungi data preclusa", true);
        vociVisibili.put("Genera Piano Mensile", true);
        
        chiamateMetodi.put("Modifica numero massimo di persone iscrivibili mediante una singola iscrizione", configService::modNumMaxSub);
        chiamateMetodi.put("Aggiungi Volontario", configService::addVolunteer);
        chiamateMetodi.put("Aggiungi Luogo", configService::addPlace);
        chiamateMetodi.put("Aggiungi Attività", configService::addActivity);
        chiamateMetodi.put("Mostra Volontari", configService::showVolunteers);
        chiamateMetodi.put("Mostra Luoghi", configService::showPlaces);
        chiamateMetodi.put("Mostra Attività", configService::showActivities);
        chiamateMetodi.put("Aggiungi data preclusa", configService::addNonUsableDate);
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
    
}
