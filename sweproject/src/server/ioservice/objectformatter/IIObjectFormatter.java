package server.ioservice.objectformatter;

import java.util.List;
import java.util.Set;

import server.firstleveldomainservices.Activity;
import server.firstleveldomainservices.Place;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityRecord;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.DailyPlan;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;
import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.Subscription;
import server.firstleveldomainservices.volunteerservice.ConfirmedActivity;
import server.firstleveldomainservices.volunteerservice.Volunteer;

public interface IIObjectFormatter <T> {
    public T formatActivity(Activity a);
    public T formatListActivity(List<Activity> aList);
    public T formatListVolunteer(List<Volunteer> vList);
    public T formatVolunteer(Volunteer v);
    public T formatListPlace(List<Place> pList);
    public T formatPlace(Place p);
    public T formatListActivityRecord(List<ActivityRecord> arList);
    public T formatMonthlyPlan(MonthlyPlan monthlyPlanData);
    public T formatListConfirmedActivity(List<ConfirmedActivity> actList);
    public T formatDailyPlan(DailyPlan dailyPlan);
    public String formatListSubscription(Set<Subscription> subscriptions);
}
