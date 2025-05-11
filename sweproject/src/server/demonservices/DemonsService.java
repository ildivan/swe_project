package server.demonservices;

import java.util.ArrayList;
import java.util.List;

import server.demonservices.demons.MonthlyPlanDemon;


public class DemonsService implements Runnable{

    private List<IDemon> demons;

    @Override
    public void run() {
    buildDemons();
    runDemons();
       
    }

    private void runDemons() {
        for (IDemon demon : demons) {
            Thread thread = new Thread(demon);
            thread.start();
            System.out.println("Demone partito");
        }
    }

    private void buildDemons() {
        this.demons = new ArrayList<>();
        demons.add(new MonthlyPlanDemon());
        System.out.println("Demone costruito");
    }

    
    
}
