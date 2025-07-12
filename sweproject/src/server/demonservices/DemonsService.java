package server.demonservices;

import java.util.ArrayList;
import java.util.List;

import server.data.facade.FacadeHub;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.demonservices.demons.MonthlyPlanDemon;
import server.utils.ConfigType;

public class DemonsService implements Runnable{

    private List<IDemon> demons;
    private IJsonLocInfoFactory locInfoFactory;
    private ConfigType configType;
    private JsonDataLayer dataLayer;
    private final FacadeHub data;

    public DemonsService(IJsonLocInfoFactory locInfoFactory,
    ConfigType configType, JsonDataLayer dataLayer, FacadeHub data) {
        this.locInfoFactory = locInfoFactory;
        this.configType = configType;
        this.dataLayer = dataLayer;
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
        demons.add(new MonthlyPlanDemon(locInfoFactory, configType, dataLayer, data));
        System.out.println("Demone costruito");
    }

}
