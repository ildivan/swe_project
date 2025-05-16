package server;

import java.time.*;

public class DateService {
    
    public static LocalDate getTodayDate(){
        return LocalDate.now();
    }

    public static String getTodayDay(){
        return getTodayDate().getDayOfWeek().toString();
    }

    /*
     * controlla se una data @target è copresa includendo gli estremi tra @start e @end
     * 
     */
    public static boolean checkIfIsBetween(LocalDate target, LocalDate start, LocalDate end) {
        if (target == null || start == null || end == null) {
            throw new IllegalArgumentException("Le date non possono essere null");
        }

        return !target.isBefore(start) && !target.isAfter(end);
    }
}
