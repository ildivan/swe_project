package server.firstleveldomainservices.volunteerservice;

import java.time.LocalDate;

public class ConfirmedActivity {
    private String activityName;
    private int numberOfSub;
    private String timeOfTheActivity;
    private LocalDate dateOfTheActivity;

    public ConfirmedActivity() {
    }

    public ConfirmedActivity(String activityName, int numberOfSub, String timeOfTheActivity, LocalDate dateOfTheActivity) {
        this.activityName = activityName;
        this.numberOfSub = numberOfSub;
        this.timeOfTheActivity = timeOfTheActivity;
        this.dateOfTheActivity = dateOfTheActivity;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public int getNumberOfSub() {
        return numberOfSub;
    }

    public void setNumberOfSub(int numberOfSub) {
        this.numberOfSub = numberOfSub;
    }

    public String getTimeOfTheActivity() {
        return timeOfTheActivity;
    }

    public void setTimeOfTheActivity(String timeOfTheActivity) {
        this.timeOfTheActivity = timeOfTheActivity;
    }

    public LocalDate getDateOfTheActivity() {
        return dateOfTheActivity;
    }

    public void setDateOfTheActivity(LocalDate dateOfTheActivity) {
        this.dateOfTheActivity = dateOfTheActivity;
    }

}
