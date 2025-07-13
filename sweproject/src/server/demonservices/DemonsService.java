package server.demonservices;

import java.util.ArrayList;
import java.util.List;
import server.data.facade.FacadeHub;
import server.demonservices.demons.MonthlyPlanDemon;
import server.utils.ConfigType;

public class DemonsService implements Runnable{

    private List<IDemon> demons;
    private ConfigType configType;
    private final FacadeHub data;

    public DemonsService(ConfigType configType, FacadeHub data) {
        this.configType = configType;
        this.data = data;
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
        demons.add(new MonthlyPlanDemon(configType, data));
        System.out.println("Demone costruito");
    }

}
