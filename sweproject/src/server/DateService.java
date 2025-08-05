package server;

import java.time.*;

import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;

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

    /**
     * ottengo l'anno in base al mese e l'anno in cui sto ottenendo i dati
     * poiche inserisco nel mese i+3 controllo se sono in ottobre novembre o dicembre
     * @param month mese in cui sto inserendo la data
     * @return inf needs to be incremented 
     */
    public boolean setYearOnPrecludeDay(int month) {
        if(month == 10 || month == 11 || month == 12){
            return true;
        }

        return false;
    }

    /**
     * ottengo l'anno in base al mese e l'anno in cui sto ottenendo i dati
     * poiche inserisco nel mese i+2 controllo se sono in novembre o dicembre
     * @param month mese in cui sto inserendo la data
     * @param firstPlanConfigured
     * @return inf needs to be incremented 
     */
    public boolean incrementYearOnDisponibilityDayVolunteer(int month, boolean firstPlanConfigured) {
        if(!firstPlanConfigured){
            if(month == 12){
                return true;
            }
        }else{
            if(month == 11 || month == 12){
                return true;
            }
        }
            
            return false;
    }

     /**
     * ottengo l'anno in base al mese e l'anno in cui sto ottenendo i dati
     * poiche inserisco nel mese i+3 controllo se sono in novembre o dicembre
     * @param month mese in cui sto inserendo la data
     * @param firstPlanConfigured
     * @return inf needs to be incremented 
     */
    public boolean incrementYearOnPrecludeDay(int month, boolean firstPlanConfigured) {
        if(!firstPlanConfigured){
            if(month == 11 || month == 12){
                return true;
            }
        }else{
            if(month==10 || month == 11 || month == 12){
                return true;
            }
        }
            
            return false;
    }

    /**
     * setta il mese in base al giorno scelto per l'iscrizione
     * @param dateOfPlan
     * @param choseDay
     * @return
     */
    public int setMonthOnDayOfSubscription(LocalDate dateOfPlan, int choseDay) {

        int month = dateOfPlan.getMonthValue();

        if(choseDay >= 17 && choseDay <= 31){
            return month;
        }else{
            if(month == 12){
                return 1;
            }
            return month + 1;
        }
    }

    /**
     * setta l'anno in base al giorno scelto per l'iscrizione
     * @param dateOfPlan
     * @param choseDay
     * @return
     */
    public int setYearOnDayOfSubscription(LocalDate dateOfPlan, int choseDay) {
        int year = dateOfPlan.getYear();
        if(choseDay >= 17 && choseDay <= 31){
            return year;
        }else{
            if(dateOfPlan.getMonthValue() == 12){
                return year + 1;
            }
            return year;
        }
    }

    /**
     * metodo per ottenre il massimo numero del giorno
     * @param mc
     * @param numberOfMonthToIncrement
     */
    public int getMaxNumDay(MonthlyConfig mc, int numberOfMonthToIncrement) {
        Month month = mc.getMonthAndYear().getMonth();
        Month nextMonth = month.plus(numberOfMonthToIncrement);
        int year = mc.getMonthAndYear().getYear();

        boolean isLeapYear = YearMonth.of(year, 1).isLeapYear();

        return nextMonth.length(isLeapYear);

    }
}
