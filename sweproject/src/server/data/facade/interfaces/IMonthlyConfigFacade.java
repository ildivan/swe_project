package server.data.facade.interfaces;

import java.time.LocalDate;

import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;

public interface IMonthlyConfigFacade {
    public MonthlyConfig getMonthlyConfig();
    public void saveMonthlyConfig(MonthlyConfig monthlyConfig);
    public int getCurrentSubCode();
    public void updateMonthlyConfigAfterPlan(MonthlyConfig mc, LocalDate date);
    public MonthlyConfig getNewMonthlyConfig();
    public void initializeMonthlyConfig();
}  
