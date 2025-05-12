package server.demonservices.demons;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import com.google.gson.JsonObject;
import server.datalayerservice.DataLayerDispatcherService;
import server.datalayerservice.JsonDataLocalizationInformation;
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
    private static final String ARCHIVE_PATH = "JF/archive.json";
    private static final String ARCHIVE_MEMBER_NAME = "activityArchive";
    private static final String MONTHLY_CONFIG_KEY_DESC = "type";
    private static final String MONTHLY_CONFIG_KEY = "current";
    private static final String MONTHLY_CONFIG_MEMEBER_NAME = "mc";
    private static final String MONTHLY_CONFIG_PATH = "JF/monthlyConfigs.json";
    private static final String MONTHLY_PLAN_PATH = "JF/monthlyPlan.json";
    private static final String MONTHLY_PLAN_MEMBER_NAME = "monthlyPlan";
    private static final String MONTHLY_PLAN_KEY_DESC = "date";

    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();

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
       JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(ARCHIVE_PATH);
        locInfo.setMemberName(ARCHIVE_MEMBER_NAME);
       
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
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(ARCHIVE_PATH);
        locInfo.setMemberName(ARCHIVE_MEMBER_NAME);
        locInfo.setKeyDesc("name");
        locInfo.setKey(name);

        JsonObject pfJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.get(locInfo));
        if(pfJO ==null){
            return false;
        }
        PerformedActivity pf = jsonFactoryService.createObject(pfJO, PerformedActivity.class);

        if(pf.getDate().equals(date) && pf.getName().equals(name)){
            return true;
        }
        return false;
    }

    private MonthlyPlan getMonthlyPlan(){
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(MONTHLY_PLAN_PATH);
        locInfo.setMemberName(MONTHLY_PLAN_MEMBER_NAME);
        //poiche uso il metodo get devo aver sia la key che la keydesc settate nelle localization info
        locInfo.setKeyDesc(MONTHLY_PLAN_KEY_DESC);
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
        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(MONTHLY_CONFIG_PATH);
        locInfo.setMemberName(MONTHLY_CONFIG_MEMEBER_NAME);
        locInfo.setKeyDesc(MONTHLY_CONFIG_KEY_DESC);
        locInfo.setKey(MONTHLY_CONFIG_KEY);

        JsonObject mcJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.get(locInfo));
        MonthlyConfig mc = jsonFactoryService.createObject(mcJO, MonthlyConfig.class);
        return mc;

    }
    
}
