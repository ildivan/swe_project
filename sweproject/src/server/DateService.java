package server;

import java.time.*;

import server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice.MonthlyConfig;


public class DateService {

    public LocalDate getTodayDate(){
        return LocalDate.now();
    }

    public String getTodayDay(){
        return getTodayDate().getDayOfWeek().toString();
    }

    /*
     * controlla se una data @target è copresa includendo gli estremi tra @start e @end
     * 
     */
    public boolean checkIfIsBetween(LocalDate target, LocalDate start, LocalDate end) {
        if (target == null || start == null || end == null) {
            throw new IllegalArgumentException("Le date non possono essere null");
        }

        return !target.isBefore(start) && !target.isAfter(end);
    }

    /*
     * setta l'anno in base al giorno
     */
    public int setYearOnPrecludeDay(MonthlyConfig mc, int day) {
        int year = mc.getMonthAndYear().getYear();

        if(day>=17 && day<=31){
            return year;
        }else{
            if( mc.getMonthAndYear().getMonthValue() == 12){
                return year +1;
            }
            return year;
        }
    }

    /*
     * asssegna al giorno il mese,
     * da 17 a 31 assegna il mese dello sviluppo del piano
     * da 1 a 16 assegna il mese successivo
     */
    public int setMonthOnPrecludeDay(MonthlyConfig mc, int day) {
        int monthOfPlan = mc.getMonthAndYear().getMonthValue();
        
        if(day>=17 && day<=31){
            return monthOfPlan;
        }else{
            return monthOfPlan+1;
        }
    }
}
