package server.data.facade.implementation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import com.google.gson.JsonObject;
import server.data.facade.interfaces.IMonthlyConfigFacade;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.data.json.datalayer.datareadwrite.IJsonReadWrite;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.jsonfactoryservice.JsonFactoryService;

public class JsonMonthlyConfigFacade implements IMonthlyConfigFacade{
    private static final String MONTHLY_CONFIG_KEY = "current";
    private static final int DAY_OF_PLAN = 16;


    private JsonDataLayer dataLayer;
    private final IJsonLocInfoFactory locInfoFactory;
    private final JsonFactoryService jsonFactoryService = new JsonFactoryService();

    public JsonMonthlyConfigFacade(IJsonReadWrite readWrite, 
                            IJsonLocInfoFactory locInfoFactory) {
        this.dataLayer = new JsonDataLayer(readWrite);
        this.locInfoFactory = locInfoFactory;
    }


    /**
     * method to obtain monthly config
     * @return
     */
    @Override
    public MonthlyConfig getMonthlyConfig(){
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyConfigLocInfo();
        locInfo.setKey(MONTHLY_CONFIG_KEY);

        JsonObject mcJO = dataLayer.get(locInfo);
        MonthlyConfig mc = jsonFactoryService.createObject(mcJO, MonthlyConfig.class);
        assert mc != null;
        return mc;

    }

    /**
     * method to save monthly config, to put in datalayer facade
     * @param monthlyConfig
     */
    @Override
    public void saveMonthlyConfig(MonthlyConfig monthlyConfig) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getMonthlyConfigLocInfo();
        locInfo.setKey(MONTHLY_CONFIG_KEY);

        JsonObject mcJO = jsonFactoryService.createJson(monthlyConfig);
        if(dataLayer.exists(locInfo)){
            dataLayer.modify(mcJO, locInfo);
        }else{
            dataLayer.add(mcJO, locInfo);
        }
       
    }

    /**
     * metodo test and set per il subcode corrente, sincronizzato per evitare race conditions
     * @return
     */
    @Override
    public int getCurrentSubCode(){
        MonthlyConfig mc = getMonthlyConfig();

        int subcode = mc.getSequenceSubscriptionNumber();
        mc.setSequenceSubscriptionNumber(subcode + 1);
        saveMonthlyConfig(mc);
        return subcode;
    }

    /**
     * method to uptate monthly config after monthlyplan generation
     */
    @Override
    public void updateMonthlyConfigAfterPlan(MonthlyConfig mc, LocalDate date) {

        //increment month of plan
        incrementMonthOfPlan(mc);

        //set the plan build flag as true
        setPlanBuildFlagAsTrue(mc, date);
    }

    /**
     * change month of plan into monthly configs
     */
    private void incrementMonthOfPlan(MonthlyConfig mc) {
    
        LocalDate date = mc.getMonthAndYear();
        LocalDate newDate = date.plusMonths(1);
        mc.setMonthAndYear(newDate);

        saveMonthlyConfig(mc);
    }

    /**
     * set as true the monthly plan build flag in monthly config
     */
    private void setPlanBuildFlagAsTrue(MonthlyConfig mc, LocalDate date) {

        Map<LocalDate, Boolean> planConfiguredMap =new LinkedHashMap<>();
        planConfiguredMap.put(date, true);
        planConfiguredMap.put(date.plusMonths(1),false);
        mc.setPlanConfigured(planConfiguredMap);

        saveMonthlyConfig(mc);
    }

    /**
     * method to get new monthly config
     * @return
     */
    @Override
    public MonthlyConfig getNewMonthlyConfig(){
        LocalDate dateOfNextPlan = getNextPlanDateBasedOnTodayDate();
    
        Map<LocalDate, Boolean> planConfigured = getPlanConfiguredNewMap(dateOfNextPlan);
        
        Set<LocalDate> precluDates = new HashSet<>();

        return new MonthlyConfig(dateOfNextPlan, planConfigured, precluDates);
    }

    private Map<LocalDate, Boolean> getPlanConfiguredNewMap(LocalDate dateOfNextPlan) {
        Map<LocalDate, Boolean> planConfigured = new HashMap<>();

        LocalDate dateOfPreviousPlan = dateOfNextPlan.minusMonths(1);

        planConfigured.put(dateOfPreviousPlan, false);
        planConfigured.put(dateOfNextPlan, false);

        return planConfigured;
    }

      
    /**
     * method to initialize MonthlyConfig.json
     */
    @Override
    public void initializeMonthlyConfig() {
        MonthlyConfig monthlyConfig = getNewMonthlyConfig();

        saveMonthlyConfig(monthlyConfig);
    
    }

     /**
     * ottiene la data del possimo piano dato usando la data di oggi
     * @return
     */
    private LocalDate getNextPlanDateBasedOnTodayDate() {
        LocalDate date = LocalDate.now(); 
        int month = date.getMonthValue();
        int year = date.getYear();
        if(LocalDate.now().getDayOfMonth()<=DAY_OF_PLAN){
            return LocalDate.of(year, month, DAY_OF_PLAN);
        }else{
            if(month == 12){
                return LocalDate.of(year + 1, 1, DAY_OF_PLAN);
            }
            return LocalDate.of(year, month + 1, DAY_OF_PLAN);
        }
    }

}
