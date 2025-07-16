package server.demonservices;

import java.util.ArrayList;
import java.util.List;

import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.demonservices.demons.MonthlyPlanDemon;
import server.utils.ConfigType;

public class DemonsService implements Runnable{

    private List<IDemon> demons;
    private ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory;
    private ConfigType configType;
    public DemonsService(ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory, ConfigType configType) {
        this.locInfoFactory = locInfoFactory;
        this.configType = configType;
    }

    @Override
    public void run() {
    buildDemons();
    runDemons();
       
    }

    private void runDemons() {
        for (IDemon demon : demons) {
            Thread thread = new Thread(demon);
            thread.start();
            System.out.println("Demone partito");
        }
    }

    private void buildDemons() {
        this.demons = new ArrayList<>();
        demons.add(new MonthlyPlanDemon(locInfoFactory,configType ));
        System.out.println("Demone costruito");
    }

    
    
}
