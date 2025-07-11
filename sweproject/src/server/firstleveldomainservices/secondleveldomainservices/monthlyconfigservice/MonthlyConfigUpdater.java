package server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
public class MonthlyConfigUpdater {
    
    private final MonthlyConfigService monthlyConfigService;

    private MonthlyConfig mc;
    private LocalDate date;
   
    public MonthlyConfigUpdater(MonthlyConfig monthlyConfig, LocalDate date,
    IJsonLocInfoFactory locInfoFactory,
    JsonDataLayer dataLayer) {
        this.mc = monthlyConfig;
        this.date = date;
        this.monthlyConfigService = new MonthlyConfigService(locInfoFactory,dataLayer);
    }



    /**
     * method to uptate monthly config after monthlyplan generation
     */

    public void updateMonthlyConfigAfterPlan() {

        //increment month of plan
        incrementMonthOfPlan();

        //set the plan build flag as true
        setPlanBuildFlagAsTrue();
    }

    /**
     * change month of plan into monthly configs
     */
    private void incrementMonthOfPlan() {
    
        LocalDate date = mc.getMonthAndYear();
        LocalDate newDate = date.plusMonths(1);
        mc.setMonthAndYear(newDate);

        monthlyConfigService.saveMonthlyConfig(mc);
    }

    /**
     * set as true the monthly plan build flag in monthly config
     */
    private void setPlanBuildFlagAsTrue() {

        Map<LocalDate, Boolean> planConfiguredMap =new LinkedHashMap<>();
        planConfiguredMap.put(date, true);
        planConfiguredMap.put(date.plusMonths(1),false);
        mc.setPlanConfigured(planConfiguredMap);

        monthlyConfigService.saveMonthlyConfig(mc);
    }


}
