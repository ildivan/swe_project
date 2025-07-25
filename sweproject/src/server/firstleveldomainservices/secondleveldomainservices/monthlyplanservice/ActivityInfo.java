package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.util.HashSet;
import java.util.Set;

import server.data.Address;
import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.Subscription;

public class ActivityInfo {
    private int numberOfSub;
    private ActivityState state;
    private String timeOfTheActivity;
    private Set<Integer> subscriptions;
    private boolean biglietto;
    private Address meetingPoint;

    public ActivityInfo(int numberOfSub, ActivityState state, String timeOfTheActivity,
                        boolean biglietto, Address meetingPoint) {
        this.numberOfSub = numberOfSub;
        this.state = state;
        this.timeOfTheActivity = timeOfTheActivity;
        this.subscriptions = new HashSet<>();
        this.biglietto = biglietto;
        this.meetingPoint = meetingPoint;
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

    public  Set<Integer> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Integer> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public synchronized void addSubscription(int subscriptionCode, Subscription subscription) {
       subscriptions.add(subscriptionCode);
       updateNumberOfSub(subscription);
    }

    private void updateNumberOfSub(Subscription subscription) {
        numberOfSub += subscription.getNumberOfSubscriptions();
    }

    public void removeSubscription(Subscription subscription) {
        if (subscriptions.contains(subscription.getSubscriptionId())) {
            subscriptions.remove(subscription.getSubscriptionId());
            numberOfSub -= subscription.getNumberOfSubscriptions();
        }
    }

    public boolean getbigliettoNecessario(){
        return biglietto;
    }

    public void setbigliettoNecessario(boolean biglietto) {
        this.biglietto = biglietto;
    }

    public Address getMeetingPoint() {
        return meetingPoint;
    }

    public void setMeetingPoint(Address meetingPoint) {
        this.meetingPoint = meetingPoint;
    }
}
