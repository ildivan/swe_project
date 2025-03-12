package backend.server.services;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;

public class ConfigService extends Service<Void>{
   // private static final String GONFIG_MENU = "\n1) Inserire nuovo volotario\n2) Inserire nuovo luogo\n3) Mostra volontari\n4) Mostra luoghi";
    private static final String QUESTION = "\n\nInserire scelta: ";
    private final Map<String, Boolean> vociVisibili = new LinkedHashMap<>();


    private final Map<String, Runnable> chiamateMetodi = new LinkedHashMap<>();
    /*
     * per quello che puo fare in base alla data io farei una mappa con key un boolean che ha
     * true se la voce puo essere eseguita false se no, e ogni volta si controlla la data, modificano 
     * le key in base alla data e si mostrano all'utente solo le key che hanno true
     */
    public ConfigService(Socket socket, Gson gson){
        super(socket);

        vociVisibili.put("Aggiungi Volontario", true);
        vociVisibili.put("Aggiungi Luogo", true);
        vociVisibili.put("Mostra Volontari", true);
        vociVisibili.put("Mostra Luoghi", true);
        

        chiamateMetodi.put("Aggiungi Volontario", this::addVolunteer);
        chiamateMetodi.put("Aggiungi Luogo", this::addPlace);
        chiamateMetodi.put("Mostra Volontari", this::showVolunteers);
        chiamateMetodi.put("Mostra Luoghi", this::showPlaces);
    }

    public Void applyLogic() throws IOException {
        boolean continuare = true;
        do{
            //ANDR GESTITO IL TEMPO IN QUELCHE MODO QUA
            startMenu();
            continuare = continueChoice();
        }while(continuare);
        write("\nArrivederci!\n", false);

        return null;
    }
                
    private void startMenu() throws IOException {
        //da implemetare il controllo sull'inserimento dell'intero
        List<String> menu = buildMenu();
        String Smenu = menuToString(menu);
        boolean sceltaValida = true;
        
        do{
            write(Smenu + QUESTION, true);
            int choice = Integer.parseInt(read());
    
            if (choice >= 1 && choice <= menu.size()) {
                sceltaValida = true;
                String sceltaStringa = menu.get(choice - 1);
                chiamateMetodi.get(sceltaStringa).run();
                
            } else {
                sceltaValida = false;
                write("Scelta non valida, riprovare", false);
            }
        }while(!sceltaValida);
        
    }

    private List<String> buildMenu() {
        
        List<String> opzioniVisibili = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : vociVisibili.entrySet()) {
            if (entry.getValue()) {
                write(String.valueOf(entry.getValue()), false);
                opzioniVisibili.add(entry.getKey());
            }
        }

        return opzioniVisibili;
        
    }

    private String menuToString(List<String> menu) {
        String menuOut = "";
        for (int i = 0; i < menu.size(); i++) {
            menuOut += ((i + 1) + ") " + menu.get(i)+"\n");
        }
        return menuOut;
    }


    private boolean continueChoice() throws IOException {
        write("Proseguire? (s/n)",true);
        String choice = read();
        if(choice.equals("n")){
            return false;
        }
        return true;
    }

    private void addVolunteer() {
        write("addVOl",false);
    }   

    private void addPlace() {
        write("addPlace",false);
    }

    private void showVolunteers() {
        write("showVol",false);
    }

    private void showPlaces() {
        write("showPla",false);
    }
    
}
