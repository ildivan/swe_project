package server.data.facade.interfaces;

import java.time.LocalDate;

import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityInfo;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.DailyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.Subscription;

public interface IMonthlyPlanFacade {
    
    public MonthlyPlan getMonthlyPlan();
    public String getMonthlyPlanDate();
    public void updateMonthlyPlan(LocalDate dateOfSubscription, DailyPlan updatedDailyPlan);
    public void refreshMonthlyPlan(MonthlyPlan monthlyPlan);
    public LocalDate getFullDateOfChosenDay(int day);
    public DailyPlan getDailyPlanOfTheChosenDay(int day);
    public DailyPlan getDailyPlan(LocalDate date);
    public void checkIfActivitiesNeedToBeArchived(MonthlyPlan monthlyPlan);
    public ActivityInfo getActivityInfoBasedOnSubCode(Subscription subscription);
    public void erasePreviousPlan(MonthlyPlan monthlyPlan);

}
