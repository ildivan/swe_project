package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

public class ActivityInfo {
    private int numberOfSub;
    private ActivityState state;
    private String timeOfTheActivity;

    public ActivityInfo(int numberOfSub, ActivityState state, String timeOfTheActivity) {
        this.numberOfSub = numberOfSub;
        this.state = state;
        this.timeOfTheActivity = timeOfTheActivity;
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
}
