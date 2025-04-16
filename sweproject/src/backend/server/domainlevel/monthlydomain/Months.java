package backend.server.domainlevel.monthlydomain;

public enum Months {
    JANUARY("January"),
    FEBRUARY("February"),
    MARCH("March"),
    APRIL("April"),
    MAY("May"),
    JUNE("June"),
    JULY("July"),
    AUGUST("August"),
    SEPTEMBER("September"),
    OCTOBER("October"),
    NOVEMBER("November"),
    DECEMBER("December");
    
    private final String description;
    
    Months(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
