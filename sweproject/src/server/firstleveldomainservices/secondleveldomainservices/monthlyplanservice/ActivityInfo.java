package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.util.HashMap;
import java.util.Map;
import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.Subscription;

public class ActivityInfo {
    private int numberOfSub;
    private ActivityState state;
    private String timeOfTheActivity;
    private Map<Integer, Subscription> subscriptions;

    public ActivityInfo(int numberOfSub, ActivityState state, String timeOfTheActivity) {
        this.numberOfSub = numberOfSub;
        this.state = state;
        this.timeOfTheActivity = timeOfTheActivity;
        this.subscriptions = new HashMap<>();
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

    public Map<Integer, Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Map<Integer, Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public synchronized void addSubscription(int subscriptionCode, Subscription subscription) {
       subscriptions.put(subscriptionCode, subscription);
       updateNumberOfSub(subscription);
    }

    private void updateNumberOfSub(Subscription subscription) {
        numberOfSub += subscription.getNumberOfSubscriptions();
    }
}
