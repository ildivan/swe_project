package server.firstleveldomainservices.secondleveldomainservices.monthlyplanservice;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lock.MonthlyPlanLockManager;
import server.DateService;
import server.data.Activity;
import server.data.User;
import server.data.Volunteer;
import server.data.facade.FacadeHub;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.MonthlyConfig;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.PlanState;
import server.utils.ConfigType;
import server.utils.Configs;

public class MonthlyPlanBuilder {
  
    private transient DateService dateService = new DateService();
    private final ConfigType configType;
    private final FacadeHub data;


    public MonthlyPlanBuilder(ConfigType configType, FacadeHub data) {
        this.configType = configType;
        this.data = data;
    }

    public boolean buildMonthlyPlan() {
        boolean lockAcquired = false;
        try {
            // Provo ad acquisire il lock entro 2 secondi
            lockAcquired = MonthlyPlanLockManager.tryLock(2, TimeUnit.SECONDS);

            if (!lockAcquired) {
                System.out.println("Impossibile generare il piano mensile: risorsa già in uso.");
                return false;
            }

            boolean firstMonthlyPlan = data.getConfigFacade().checkIfFirstMonthlyPlan(configType);
            LocalDate today = dateService.getTodayDate().withDayOfMonth(16);

            // Evita race condition modificando lo stato del piano in configurazione
            MonthlyConfig mc = data.getMonthlyConfigFacade().getMonthlyConfig();
            mc = setIsBeingConfigured(mc, PlanState.DISPONIBILITA_APERTE, false);
            mc = setIsBeingConfigured(mc, PlanState.GENERAZIONE_PIANO, true);

            MonthlyPlan monthlyPlan = new MonthlyPlan(today,
                data.getPrecludeDateFacade(),
                data);

            List<Activity> activities = data.getActivitiesFacade().getActivities();

            monthlyPlan.generateMonthlyPlan(activities);

            data.getMonthlyPlanFacade().erasePreviousPlan(monthlyPlan);

            if (firstMonthlyPlan) {
                updateConfigAfterFirstPlanGenerated();
            }

            refreshData(mc, today);

            //aggiorno mappa per permetter di modificare, garantisco la sequenzialità
            setIsBeingConfigured(mc, PlanState.GENERAZIONE_PIANO, false);
            setIsBeingConfigured(mc, PlanState.MODIFICHE_APERTE, true);

            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread interrotto durante l’attesa del lock.");
            return false;

        } finally {
            if (lockAcquired) {
                MonthlyPlanLockManager.unlock();
            }
        }
    }


    /**
     * metodo per applicare le modifiche effettuate nel mese precedente
     */
    private void refreshData(MonthlyConfig mc, LocalDate date) {
        
        data.getMonthlyConfigFacade().updateMonthlyConfigAfterPlan(mc, date);
        data.getPlacesFacade().refreshChangedPlaces();
        data.getActivitiesFacade().refreshChangedActivities();
        refreshUsers();
        refreshVolunteers();
        data.getPrecludeDateFacade().refreshPrecludeDates(date);
    }

    /**
     * method to refresh users
     */
    private void refreshUsers() {
        List<User> users = data.getUsersFacade().getUsers();

        for (User user : users) {
            if(user.isDeleted()){
                data.getUsersFacade().deleteUser(user.getName());

                //se è un volontario elimina anche il volontario
                if(user.getRole().equalsIgnoreCase("volontario")){
                    data.getVolunteersFacade().deleteVolunteer(user.getName());
                }
            }

            if(!user.isActive()){
                data.getUsersFacade().modifyUser(
                    user.getName(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(true),
                    Optional.empty()
                );
            }
        }
    }

    /**
     * aggiorno i config indicando che il piano è stato generato la prima volta
     */
    private void updateConfigAfterFirstPlanGenerated() {
        Configs configs = data.getConfigFacade().getConfig(configType);
        configs.setFirstPlanConfigured(true);
        data.getConfigFacade().save(configs, configType);
    }

    /**
     * metodo epr modificare il fatto che si sta iniziando a configurare il piano mensile
     * @param mc
     * @param isBeingConfigured
     */
    private MonthlyConfig setIsBeingConfigured(MonthlyConfig mc, PlanState isBeingConfigured, Boolean value) {

        Map<PlanState, Boolean> stateMap = mc.getPlanStateMap();
        stateMap.put(isBeingConfigured, value);
        mc.setPlanStateMap(stateMap);
        data.getMonthlyConfigFacade().saveMonthlyConfig(mc);

        return mc;

    }

    /**
     * metodo per permettere un nuovo inserimento di date precluse hai volontari
     */
    private void refreshVolunteers() {
        
        List<Volunteer> volunteers = data.getVolunteersFacade().getVolunteers();
        Set<String> newDays;

        for (Volunteer volunteer : volunteers) {
            if(volunteer.getDisponibilityDaysCurrent() == null) {
                newDays = new LinkedHashSet<>();
            }else{
                newDays = volunteer.getDisponibilityDaysCurrent();
            }

            data.getVolunteersFacade().modifyVolunteer(
                volunteer.getName(), 
                Optional.empty(),
                Optional.of(newDays),
                Optional.of(new LinkedHashSet<>())
            );
        }
       

    }

}
