package backend.server.domainlevel;

import java.time.LocalDate;
import java.util.Map;
import backend.server.domainlevel.*;

public class MontlyPlan {

    private Map<LocalDate,Boolean> precludesDays;
    private Map<LocalDate,DailyPlan> montlyPlan;


}
