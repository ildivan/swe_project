package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.util.HashSet;
import java.util.Set;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.subscriptionlogic.Subscription;

public class ActivityInfo {
    private int numberOfSub;
    private ActivityState state;
    private String timeOfTheActivity;
    private Set<Subscription> subscriptions;

    public ActivityInfo(int numberOfSub, ActivityState state, String timeOfTheActivity) {
        this.numberOfSub = numberOfSub;
        this.state = state;
        this.timeOfTheActivity = timeOfTheActivity;
        this.subscriptions = new HashSet<>();
    }

    public int getNumberOfSub() {
        return numberOfSub;
    }

    public void setNumberOfSub(int numberOfSub) {
        this.numberOfSub = numberOfSub;
    }

    public ActivityState getState() {
        return state;
    }

    public void setState(ActivityState state) {
        this.state = state;
    }

    public void setTime(String time){
        this.timeOfTheActivity = time;
    }

    public String getTime(){
        return timeOfTheActivity;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
