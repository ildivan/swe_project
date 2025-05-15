package server.demonservices.demons;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import com.google.gson.JsonObject;
import server.datalayerservice.DataLayerDispatcherService;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.demonservices.IDemon;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityInfo;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.DailyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.PerformedActivity;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;

public class MonthlyPlanDemon implements IDemon{
    private static final String MONTHLY_CONFIG_KEY = "current";

    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private ILocInfoFactory locInfoFactory = new JsonLocInfoFactory();

    //ogni secondo viene chiamato il metodo tick che esegue il compito del demone
    @Override
    public void run() {
        while (true) {
            try {
                tick();
                Thread.sleep(1000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public void tick() {
       MonthlyPlan monthlyPlan = getMonthlyPlan();

       if(monthlyPlan == null){
        return;
       }
       JsonDataLocalizationInformation locInfo = locInfoFactory.getArchiveLocInfo();
       
       //ottengo la lista di attività da aggiungere e le aggiungo al file di archivio
       
        for (Map.Entry<LocalDate, DailyPlan> dailyEntry : monthlyPlan.getMonthlyPlan().entrySet()) {
            LocalDate date = dailyEntry.getKey();
            DailyPlan dailyPlan = dailyEntry.getValue();
    
            for (Map.Entry<String, ActivityInfo> activityEntry : dailyPlan.getPlan().entrySet()) {
                String activityName = activityEntry.getKey();
                ActivityInfo activityInfo = activityEntry.getValue();
    
                if (activityInfo.getState() == ActivityState.EFFETTUATA && !activityArchived(activityName,date)) {
                    int subs = activityInfo.getNumberOfSub();
                    String time = activityInfo.getTime();

                    PerformedActivity pf = new PerformedActivity(activityName, date, subs, time);

                    DataLayerDispatcherService.start(locInfo, layer->layer.add(jsonFactoryService.createJson(pf), locInfo));
                }
            }
        }
    
       //le aggiunge
    }

    private boolean activityArchived(String name, LocalDate date) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getArchiveLocInfo();
        locInfo.setKeyDesc("name");
        locInfo.setKey(name);

        JsonObject pfJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.get(locInfo));
        if(pfJO ==null){
            return false;
        }
        PerformedActivity pf = jsonFactoryService.createObject(pfJO, PerformedActivity.class);

        return pf.getDate().equals(date) && pf.getName().equals(name);
    }

    private MonthlyPlan getMonthlyPlan(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyPlanLocInfo();
        locInfo.setKey(getMonthlyPlanDate());
        JsonObject mpJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.get(locInfo));

        return jsonFactoryService.createObject(mpJO, MonthlyPlan.class);
    }

    private String getMonthlyPlanDate(){
        MonthlyConfig mc = getMonthlyConfig();
        LocalDate date = mc.isPlanConfigured().keySet().iterator().next();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return date.format(formatter);
    }

    private MonthlyConfig getMonthlyConfig(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyConfigLocInfo();
        locInfo.setKey(MONTHLY_CONFIG_KEY);

        JsonObject mcJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.get(locInfo));
        MonthlyConfig mc = jsonFactoryService.createObject(mcJO, MonthlyConfig.class);
        assert mc != null;
        return mc;

    }
    
}
