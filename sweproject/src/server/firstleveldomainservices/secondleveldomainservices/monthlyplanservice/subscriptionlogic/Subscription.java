package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.subscriptionlogic;

public class Subscription {
    private String userName;
    private int subscriptionCode;
    private int numberOfSubscriptions;

    public Subscription(String userId, int subscriptionCode, int numberOfSubscriptions) {
        this.userName = userId;
        this.subscriptionCode = subscriptionCode;
        this.numberOfSubscriptions = numberOfSubscriptions;
    }

    public Subscription(String userName){

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getSubscriptionCode() {
        return subscriptionCode;
    }

    public void setSubscriptionCode(int subscriptionCode) {
        this.subscriptionCode = subscriptionCode;
    }

    public int getNumberOfSubscriptions() {
        return numberOfSubscriptions;
    }

    public void setNumberOfSubscriptions(int numberOfSubscriptions) {
        this.numberOfSubscriptions = numberOfSubscriptions;
    }
}
