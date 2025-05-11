package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;

public class PerformedActivity {
    private String name;
    private LocalDate date;
    private int subs;
    private String time;

    public PerformedActivity(String name, LocalDate date, int subs, String time) {
        this.name = name;
        this.date = date;
        this.subs = subs;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getSubs() {
        return subs;
    }

    public void setSubs(int subs) {
        this.subs = subs;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
