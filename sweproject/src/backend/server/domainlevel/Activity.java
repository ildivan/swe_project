package backend.server.domainlevel;

public class Activity {

    private String placeName;
    private String title;
    private String description;
    private Address meetingPoint;
    private String firstProgrammableDate;
    private String lastProgrammableDate;
    private String[] programmableDays;
    private String programmableHour;
    private String duration;
    private boolean bigliettoNecessario;
    private int maxPartecipanti;
    private int minPartecipanti;
    private String[] volunteers;

    public Activity(String placeName, String title, String description, Address meetingPoint,
    String firstProgrammableDate, String lastProgrammableDate, String[] programmableDays,
    String programmableHour, String duration, boolean bigliettoNecessario, int maxPartecipanti, int minPartecipanti, String[] volunteers) {
       
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
        this.volunteers = volunteers;
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

    public String getFirstProgrammableDate() {
        return firstProgrammableDate;
    }

    public void setFirstProgrammableDate(String firstProgrammableDate) {
        this.firstProgrammableDate = firstProgrammableDate;
    }

    public String getLastProgrammableDate() {
        return lastProgrammableDate;
    }

    public void setLastProgrammableDate(String lastProgrammableDate) {
        this.lastProgrammableDate = lastProgrammableDate;
    }

    public String[] getProgrammableDays() {
        return programmableDays;
    }

    public void setProgrammableDays(String[] programmableDays) {
        this.programmableDays = programmableDays;
    }

    public String getProgrammableHour() {
        return programmableHour;
    }

    public void setProgrammableHour(String programmableHour) {
        this.programmableHour = programmableHour;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
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

    public String[] getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(String[] volunteers) {
        this.volunteers = volunteers;
    }

    public String toString(){
        StringBuffer out = new StringBuffer();
        out.append("\n\n----------------\n\n");
        out.append(String.format("Attività: %s", getTitle()));
        out.append(String.format("\nDescrizione attività: %s", getDescription()));
        out.append(String.format("\nPunto di ritrovo: %s", getMeetingPoint()));
        out.append(String.format("\nMassimo numero partecipanti: %s", getMaxPartecipanti()));
        out.append(String.format("\nPrima data utile: %s", getFirstProgrammableDate()));
        out.append(String.format("\nUltima data utile: %s", getLastProgrammableDate()));
        out.append(String.format("\nOra di inizio: %s", getProgrammableHour()));
        out.append(String.format("\nDurata: %s", getDuration()));
        out.append(String.format("\nVolontari associati: %s", getVolunteersToString(volunteers)));
        return out.toString();
    }

    private String getVolunteersToString(String[] volunteers){
        String out = "";
        for(String s :volunteers){
            out = out + s + ", ";
        }
        return out;
    }
}
