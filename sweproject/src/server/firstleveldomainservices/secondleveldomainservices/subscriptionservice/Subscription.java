package server.firstleveldomainservices.secondleveldomainservices.subscriptionservice;

import java.time.LocalDate;

public class Subscription {
    private String userName;
    private int numberOfSubscriptions;
    private String activityName;
    private int subscriptionId;
    private LocalDate dateOfSub;
    private LocalDate dateOfActivity;

    public Subscription(String userId, int numberOfSubscriptions, String activityName, int subscriptionId, LocalDate dateOfSub, LocalDate dateOfActivity) {
        this.userName = userId;
        this.numberOfSubscriptions = numberOfSubscriptions;
        this.activityName = activityName;
        this.subscriptionId = subscriptionId;
        this.dateOfSub = dateOfSub;
        this.dateOfActivity = dateOfActivity;
    }

    public Subscription(String userName){

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getNumberOfSubscriptions() {
        return numberOfSubscriptions;
    }

    public void setNumberOfSubscriptions(int numberOfSubscriptions) {
        this.numberOfSubscriptions = numberOfSubscriptions;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public int getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(int subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public LocalDate getDateOfSub() {
        return dateOfSub;
    }

    public void setDateOfSub(LocalDate dateOfSub) {
        this.dateOfSub = dateOfSub;
    }

    public LocalDate getDateOfActivity() {
        return dateOfActivity;
    }

    public void setDateOfActivity(LocalDate dateOfActivity) {
        this.dateOfActivity = dateOfActivity;
    }

}
