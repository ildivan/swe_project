package server.data.facade.interfaces;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import server.data.Activity;
import server.data.Address;
import server.data.Place;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityRecord;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.ActivityState;
import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyPlan;

public interface IActivitiesFacade {
    public List<Activity> getActivities();
    public List<Activity> getChangedActivities();
    public Activity getActivity(String activityName);
    public Activity getChangedActivity(String activityName);
    public Activity addActivity(
        Place place,
        String name,
        String description,
        Address meetingPoint,
        LocalDate firstProgrammableDate,
        LocalDate lastProgrammableDate,
        String[] programmableDays,
        LocalTime programmableHour,
        LocalTime duration,
        boolean bigliettoNecessario,
        int maxPartecipanti,
        int minPartecipanti,
        String[] volunteers
    );
    public List<ActivityRecord> getActivitiesByState(ActivityState desiredState, MonthlyPlan monthlyPlan);
    public boolean modifyActivity(
        String activityName,
        Optional<String> title,
        Optional<String> description,
        Optional<Address> newMeetingPoint,
        Optional<LocalDate> firstDate,
        Optional<LocalDate> lastDate,
        Optional<String[]> newDays,
        Optional<LocalTime> newHour,
        Optional<LocalTime> newDuration,
        Optional<Boolean> newTicket,
        Optional<Integer> newMax,
        Optional<Integer> newMin,
        Optional<String[]> volunteers
    );
    public boolean deleteActivity(String activityName);
    public boolean doesActivityExist(String activityName);
    public void copyToReadOnlyActivity();
    public void initializeChangedFiles();
    public void refreshChangedActivities();
}
