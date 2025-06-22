package server.firstleveldomainservices.secondleveldomainservices.subscriptionservice;

public class Subscription {
    private String userName;
    private int numberOfSubscriptions;

    public Subscription(String userId, int numberOfSubscriptions) {
        this.userName = userId;
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

    public int getNumberOfSubscriptions() {
        return numberOfSubscriptions;
    }

    public void setNumberOfSubscriptions(int numberOfSubscriptions) {
        this.numberOfSubscriptions = numberOfSubscriptions;
    }
}
