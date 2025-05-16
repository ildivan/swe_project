package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;
import java.util.List;
import com.google.gson.JsonObject;
import server.DateService;
import server.datalayerservice.DataLayerDispatcherService;
import server.datalayerservice.JsonDataLocalizationInformation;
import server.firstleveldomainservices.Activity;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;
import server.objects.interfaceforservices.IActionDateService;

public class MonthlyPlanService {

    private static final String ACTIVITY_PATH = "JF/activities.json";
    private static final String ACTIVITY_MEMBER_NAME = "activities";
    private static final String MONTHLY_PLAN_PATH = "JF/monthlyPlan.json";
    private static final String MONTHLY_PLAN_MEMBER_NAME = "monthlyPlan";

    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();


    public boolean buldMonthlyPlan() {
        LocalDate today = (LocalDate) DateService.Service.GET_TODAY_DATE.start();
        MonthlyPlan monthlyPlan = new MonthlyPlan(today);

        JsonDataLocalizationInformation locInfo = new JsonDataLocalizationInformation();
        locInfo.setPath(ACTIVITY_PATH);
        locInfo.setMemberName(ACTIVITY_MEMBER_NAME);
    
        List<JsonObject> activityJO = DataLayerDispatcherService.startWithResult(locInfo, layer->layer.getAll(locInfo));
        List<Activity> activity = jsonFactoryService.createObjectList(activityJO, Activity.class);

        monthlyPlan.generateMonthlyPlan(activity);

        locInfo.setPath(MONTHLY_PLAN_PATH);
        locInfo.setMemberName(MONTHLY_PLAN_MEMBER_NAME);

        DataLayerDispatcherService.start(locInfo, layer -> layer.add(jsonFactoryService.createJson(monthlyPlan), locInfo));

        monthlyPlan.incrementMonthOfPlan();
        monthlyPlan.clearPrecludedDates();

        return true;//return true se va tutto bene, sarebbe meglio implementare anche iil false con delle eccezioni dentro
        //DA FARE
    }

}
