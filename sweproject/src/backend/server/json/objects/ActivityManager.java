package backend.server.json.objects;

import java.time.LocalDate;
import java.util.*;

public class ActivityManager {
    private String areaOfIntrest; //ambito territoriale di competenza
    private ArrayList<Place> places; //elenco di luoghi su cui poi creare le visite
    private Map<User, Set<LocalDate>> volunteers; // elenco di volontari con le rispettive date in cui sono disponibili
    private ArrayList<Activity> activities; //elenco delle visite sui luoghi (es luogo a visita completa il sabato)
    /*elenco delle attività proposte mensili (struttura creata usando le 4 var sopra)
    qua dentro ci sara una struttura che orende una visita e la associa alla data effettiva solo se
    si puo fare qualcos ain quel gionro e se il volontario ha dato disponibilita tale giornoxs
    */
    private MonthlyActivity monthlyActivity; 
}
