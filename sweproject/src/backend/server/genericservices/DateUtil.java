package backend.server.genericservices;

import java.time.*;

public class DateUtil {
    
    public static LocalDate getTodayDate(){
        return LocalDate.now();
    }

    public static String getTodayDay(){
        return getTodayDate().getDayOfWeek().toString();
    }
}
