package backend.server.domainlevel.monthlydomain;

import java.time.LocalDate;

public class DailyPlan {
    private LocalDate date;
    private boolean isDone = false;

    public DailyPlan(LocalDate date) {
        this.date = date;
    }

public void generate(){
   isDone = true;
}
}
