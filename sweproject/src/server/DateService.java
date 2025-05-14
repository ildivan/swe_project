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
            if(monthOfPlan == 12){
                return 1;
            }
            return monthOfPlan+1;
        }
    }

    /*
     * setta l'anno in base al giorno per il volontario
     */
    public int setYearOnPrecludeDayVolunteer(MonthlyConfig mc, int day) {
        int year = mc.getMonthAndYear().getYear();
        int month = mc.getMonthAndYear().getMonthValue();

        if(day>=17 && day<=31){
            if(month == 12){
                return year+1;
            }
            return year;
        }else{
            if(month == 11 || month == 12){
                return year +1;
            }
            return year;
        }
    }

    /*
     * asssegna al giorno il mese per il volontario
     * da 17 a 31 assegna il mese dello sviluppo del piano
     * da 1 a 16 assegna il mese successivo
     */
    public int setMonthOnPrecludeDayVolunteer(MonthlyConfig mc, int day) {
        int monthOfPlan = mc.getMonthAndYear().getMonthValue();
        
        if(day>=17 && day<=31){
            if(monthOfPlan == 12){
                return 1;
            }
            return monthOfPlan+1;
        }else{
            if(monthOfPlan == 11){
                return 1;
            }
            if(monthOfPlan == 12){
                return 2;
            }
            return monthOfPlan+2;
        }
    }
}
