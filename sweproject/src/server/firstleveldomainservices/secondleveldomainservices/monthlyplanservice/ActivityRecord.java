package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;

public class ActivityRecord {
    private LocalDate date;
    private String name;
    private ActivityInfo activity;

    public ActivityRecord(LocalDate date, String name, ActivityInfo activity) {
        this.date = date;
        this.name = name;
        this.activity = activity;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public ActivityInfo getActivity() {
        return activity;
    }
}

