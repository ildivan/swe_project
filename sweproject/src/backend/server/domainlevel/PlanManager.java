package backend.server.domainlevel;

import java.time.LocalDate;
import java.util.*;

public class PlanManager {
    private String areaOfIntrest; //ambito territoriale di competenza

    //private ArrayList<Place> places; //elenco di luoghi su cui poi creare le visite
    private PlacesManager places; //elenco di luoghi su cui poi creare le visite
    
    //private Map<User, Set<LocalDate>> volunteers; // elenco di volontari con le rispettive date in cui sono disponibili
    private VolunteerManager volunteers; // elenco di volontari con le rispettive date in cui sono disponibili
    
    
    //private ArrayList<Activity> activities; //elenco delle visite sui luoghi (es luogo a visita completa il sabato)
    private ActivityManager activities; //elenco delle visite sui luoghi (es luogo a visita completa il sabato)
    /*elenco delle attività proposte mensili (struttura creata usando le 4 var sopra)
    qua dentro ci sara una struttura che orende una visita e la associa alla data effettiva solo se
    si puo fare qualcos ain quel gionro e se il volontario ha dato disponibilita tale giornoxs
    */
    private MonthlyPlanManager monthlyActivity; //classe che mi gestisce la creazione del piano mensile
    
    private boolean configured = false; //flag che indica se è stata configurato l'oggetto,
    //con area di interesse, luoghi; sara controllata da volontari e fruitori prima di fare qualsiasi cosa

    // public PlanManager(){
    //     this.areaOfIntrest = "";
    //     this.places = new ArrayList<>();
    //     this.volunteers = new HashMap<>();
    //     this.activities = new ArrayList<>();
    //     this.monthlyActivity = new MonthlyPlan();
    // }
}
