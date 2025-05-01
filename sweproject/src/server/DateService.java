package server;

import java.time.*;
import server.objects.interfaceforservices.IActionService;


public class DateService {
    
    public enum Service {
		GET_TODAY_DATE((params) -> DateService.getTodayDate()),
		GET_TODAY_DAY((params) -> DateService.getTodayDay()),
		CHECK_IF_BETWEEN((params) -> DateService.checkIfIsBetween((LocalDate) params[0], (LocalDate) params[1], (LocalDate) params[2]));

		private final IActionService<?> service;
	
		Service(IActionService<?> service) {
			this.service = service;
		}
	
		public Object start(Object... params) {
			return service.apply(params);
		}
	}

    private static LocalDate getTodayDate(){
        return LocalDate.now();
    }

    private static String getTodayDay(){
        return getTodayDate().getDayOfWeek().toString();
    }

    /*
     * controlla se una data @target è copresa includendo gli estremi tra @start e @end
     * 
     */
    private static boolean checkIfIsBetween(LocalDate target, LocalDate start, LocalDate end) {
        if (target == null || start == null || end == null) {
            throw new IllegalArgumentException("Le date non possono essere null");
        }

        return !target.isBefore(start) && !target.isAfter(end);
    }
}
