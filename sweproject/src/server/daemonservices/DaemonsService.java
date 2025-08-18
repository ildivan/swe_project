package server.daemonservices;

import java.util.ArrayList;
import java.util.List;
import server.daemonservices.daemons.MonthlyPlanDaemon;
import server.data.facade.FacadeHub;
import server.utils.ConfigType;

public class DaemonsService implements Runnable{

    private List<IDaemon> demons;
    private ConfigType configType;
    private final FacadeHub data;

    public DaemonsService(ConfigType configType, FacadeHub data) {
        this.configType = configType;
        this.data = data;
    }

    @Override
    public void run() {
    buildDemons();
    runDemons();
       
    }

    private void runDemons() {
        for (IDaemon demon : demons) {
            Thread thread = new Thread(demon);
            thread.start();
            System.out.println("Demone partito");
        }
    }

    private void buildDemons() {
        this.demons = new ArrayList<>();
        demons.add(new MonthlyPlanDaemon(configType, data));
        System.out.println("Demone costruito");
    }

}
