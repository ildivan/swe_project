package server.data.facade.interfaces;

import java.time.LocalDate;

public interface IPrecludeDateFacade {
    public boolean checkIfIsPrecludeDate(LocalDate possiblePrecludeDate, LocalDate dateOfPlanGeneration);
    public void savePrecludeDate(LocalDate precludeDate);
    public void refreshPrecludeDates(LocalDate dateOfPlan);
}
