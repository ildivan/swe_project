package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;
import java.util.List;
import com.google.gson.Gson;

import server.DateService;
import server.GsonFactoryService;
import server.datalayerservice.ActivityManager;
import server.datalayerservice.JSONUtil;
import server.datalayerservice.MonthlyPlanManager;
import server.firstleveldomainservices.Activity;
import server.objects.interfaceforservices.IActionDateService;

public class MonthlyPlanService {
    private static Gson gson = (Gson)GsonFactoryService.Service.GET_GSON.start();
    private static ActivityManager activityManager = new ActivityManager(gson);
    

    public enum Service {
        BUILD_PLAN((params) -> MonthlyPlanService.buldMonthlyPlan());

        private IActionDateService<?> service;

        Service(IActionDateService<?> service) {
            this.service = service;
        }

        public Object start(Object... params) {
            return service.apply(params);
        }
    }

    private static boolean buldMonthlyPlan() {
        LocalDate today = (LocalDate) DateService.Service.GET_TODAY_DATE.start();
        MonthlyPlan monthlyPlan = new MonthlyPlan(today);
        List<Activity> activity = activityManager.getAllAsActivities();
        monthlyPlan.generateMonthlyPlan(activity);
        MonthlyPlanManager monthlyPlanManager = new MonthlyPlanManager(gson);
        monthlyPlanManager.add(JSONUtil.createJson(monthlyPlan));
        return true;//return true se va tutto bene, sarebbe meglio implementare anche iil false con delle eccezioni dentro
        //DA FARE
    }

}
