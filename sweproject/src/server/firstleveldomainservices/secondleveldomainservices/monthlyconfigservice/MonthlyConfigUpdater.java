package server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalayers.JsonDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.datalayerservice.datalocalizationinformations.JsonLocInfoFactory;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;

public class MonthlyConfigUpdater {
    

    private static final String MONTHLY_CONFIG_CURRENT_KEY = "current";
    private IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory = new JsonLocInfoFactory();
    private IDataLayer<JsonDataLocalizationInformation> dataLayer = new JsonDataLayer();
    private final MonthlyConfigService monthlyConfigService = new MonthlyConfigService();

    private MonthlyConfig mc;
    private LocalDate date;
   
    public MonthlyConfigUpdater(MonthlyConfig monthlyConfig, LocalDate date) {
        this.mc = monthlyConfig;
        this.date = date;
    }



    /**
     * method to uptate monthly config after monthlyplan generation
     */

    public void updateMonthlyConfigAfterPlan() {

        //clear the precluded dates
        clearPrecludedDates();

        //increment month of plan
        incrementMonthOfPlan();

        //set the plan build flag as true
        setPlanBuildFlagAsTrue();
    }

    /**
     * clear the preclude dates in the config of the monthly plan
     */
    private void clearPrecludedDates() {
        
        mc.setPrecludeDates(new HashSet<>());


        monthlyConfigService.saveMonthlyConfig(mc);

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
