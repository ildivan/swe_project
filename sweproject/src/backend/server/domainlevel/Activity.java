package backend.server.domainlevel;

import java.time.*;

public class Activity {

    private String placeName;
    private String title;
    private String description;
    private Address meetingPoint;
    private LocalDate firstProgrammableDate;
    private LocalDate lastProgrammableDate;
    private String[] programmableDays;
    private LocalTime programmableHour;
    private LocalTime duration;
    private boolean bigliettoNecessario;
    private int maxPartecipanti;
    private int minPartecipanti;

    public Activity(String placeName, String title, String description, Address meetingPoint,
        LocalDate firstProgrammableDate, LocalDate lastProgrammableDate, String[] programmableDays,
        LocalTime programmableHour, LocalTime duration, boolean bigliettoNecessario, int maxPartecipanti, int minPartecipanti){
       
        this.placeName = placeName;
        this.title = title;
        this.description = description;
        this.meetingPoint = meetingPoint;
        this.firstProgrammableDate = firstProgrammableDate;
        this.lastProgrammableDate = lastProgrammableDate;
        this.programmableDays = programmableDays;
        this.programmableHour = programmableHour;
        this.duration = duration;
        this.bigliettoNecessario = bigliettoNecessario;
        this.maxPartecipanti = maxPartecipanti;
        this.minPartecipanti = minPartecipanti;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Address getMeetingPoint() {
        return meetingPoint;
    }

    public void setMeetingPoint(Address meetingPoint) {
        this.meetingPoint = meetingPoint;
    }

    public LocalDate getFirstProgrammableDate() {
        return firstProgrammableDate;
    }

    public void setFirstProgrammableDate(LocalDate firstProgrammableDate) {
        this.firstProgrammableDate = firstProgrammableDate;
    }

    public LocalDate getLastProgrammableDate() {
        return lastProgrammableDate;
    }

    public void setLastProgrammableDate(LocalDate lastProgrammableDate) {
        this.lastProgrammableDate = lastProgrammableDate;
    }

    public String[] getProgrammableDays() {
        return programmableDays;
    }

    public void setProgrammableDays(String[] programmableDays) {
        this.programmableDays = programmableDays;
    }

    public LocalTime getProgrammableHour() {
        return programmableHour;
    }

    public void setProgrammableHour(LocalTime programmableHour) {
        this.programmableHour = programmableHour;
    }

    public LocalTime getDuration() {
        return duration;
    }

    public void setDuration(LocalTime duration) {
        this.duration = duration;
    }

    public boolean isBigliettoNecessario() {
        return bigliettoNecessario;
    }

    public void setBigliettoNecessario(boolean bigliettoNecessario) {
        this.bigliettoNecessario = bigliettoNecessario;
    }

    public int getMaxPartecipanti() {
        return maxPartecipanti;
    }

    public void setMaxPartecipanti(int maxPartecipanti) {
        this.maxPartecipanti = maxPartecipanti;
    }

    public int getMinPartecipanti() {
        return minPartecipanti;
    }

    public void setMinPartecipanti(int minPartecipanti) {
        this.minPartecipanti = minPartecipanti;
    }
}
