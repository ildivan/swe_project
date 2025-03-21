package backend.server.domainlevel.domainservices;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import backend.server.Configs;
import backend.server.genericservices.Service;
import backend.server.genericservices.DataLayer.DataLayer;
import backend.server.genericservices.DataLayer.JSONDataContainer;
import backend.server.genericservices.DataLayer.JSONDataManager;

public class ConfigService extends Service<Void>{
   // private static final String GONFIG_MENU = "\n1) Inserire nuovo volotario\n2) Inserire nuovo luogo\n3) Mostra volontari\n4) Mostra luoghi";
    private static final String QUESTION = "\n\nInserire scelta: ";
    private final Map<String, Boolean> vociVisibili = new LinkedHashMap<>();
    private final Map<String, Runnable> chiamateMetodi = new LinkedHashMap<>();
    private DataLayer dataLayer = new JSONDataManager();
  

    public ConfigService(Socket socket, Gson gson){
        super(socket);
        //TODO avere un json che contiene le varie chiamate hai metodi e se sono visibili o no
        vociVisibili.put("Modifica numero massimo di persone iscrivibili mediante una singola iscrizione", true);
        vociVisibili.put("Aggiungi Volontario", true);
        vociVisibili.put("Aggiungi Luogo", true);
        vociVisibili.put("Mostra Volontari", true);
        vociVisibili.put("Mostra Luoghi", true);
        
        chiamateMetodi.put("Modifica numero massimo di persone iscrivibili mediante una singola iscrizione", this::modNumMaxSub);
        chiamateMetodi.put("Aggiungi Volontario", this::addVolunteer);
        chiamateMetodi.put("Aggiungi Luogo", this::addPlace);
        chiamateMetodi.put("Mostra Volontari", this::showVolunteers);
        chiamateMetodi.put("Mostra Luoghi", this::showPlaces);
    }

    public Void applyLogic() throws IOException {
        
        boolean continuare = true;
        do{
            //ANDR GESTITO IL TEMPO IN QUELCHE MODO QUA
            if(!checkIfUserConfigured()){
                if(firstTimeConfiguration()){
                    write("Configurazione base completata", false);
                }else{
                    write("Errore durante la configurazione", false);
                    return null;
                }
            };
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

    //todo fare una classe menu che mi gestisce il JSON del menu
    private List<String> buildMenu() {
        
        List<String> opzioniVisibili = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : vociVisibili.entrySet()) {
            if (entry.getValue()) {
                //   write(String.valueOf(entry.getValue()), false);
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


    private boolean checkIfUserConfigured() {
        write("primo metodo", false);
        JsonObject JO = new JsonObject();
        JO = dataLayer.get(new JSONDataContainer("JF/configs.json", "configs", "normalFunctionConfigs", "configType"));
        // if(JO.isEmpty()){
        //    // return false;
        // }
        write(String.format("%b",JO.get("userConfigured").getAsBoolean() ), false);
        return JO.get("userConfigured").getAsBoolean();
    }

    private boolean firstTimeConfiguration(){
        try {
        write("Prima configurazione necessaria:", false);
        String areaOfIntrest = configureArea();
        Integer maxSubscriptions = configureMaxSubscriptions();

        //richiesta la configurazone dei luoghi e delle attivita
        //in base a come vanno le due cose sopra, modifico i config in un metodo a parte
        Configs configs = new Configs();
        configs.setUserConfigured(true);
        configs.setAreaOfIntrest(areaOfIntrest);
        configs.setMaxSubscriptions(maxSubscriptions);

        String StringJO = new String();
        StringJO = gson.toJson(configs);
        JsonObject JO = gson.fromJson(StringJO, JsonObject.class);

        JSONDataContainer dataContainer = new JSONDataContainer("JF/configs.json", JO, "configs","normalFunctionConfigs", "configType");
        dataLayer.modify(dataContainer);

        return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
    }

    private String configureArea() throws IOException{
        write("Inserire luogo di esercizio", true);
        String areaOfIntrest = null;
        areaOfIntrest = read();
        return areaOfIntrest;
    }
    
    private Integer configureMaxSubscriptions() throws IOException{
        write("Inserire numero massimo di iscrizioni contemporanee ad una iniziativa", true);
        Integer maxSubscriptions = Integer.parseInt(read());
        return maxSubscriptions;
    }


    private void modNumMaxSub(){
        write("\nInserire nuovo numero di iscrizioni massime",true);
        Integer n = 0;
        try {
            n = Integer.parseInt(read());
        } catch (NumberFormatException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JsonObject oldConfigsJO = dataLayer.get(new JSONDataContainer("JF/configs.json", "configs", "normalFunctionConfigs", "configType"));
        Configs configs = gson.fromJson(oldConfigsJO, Configs.class);
        configs.setMaxSubscriptions(n);
        JsonObject newConfigsJO = gson.toJsonTree(configs).getAsJsonObject();

        dataLayer.modify(new JSONDataContainer("JF/configs.json", newConfigsJO, "configs","normalFunctionConfigs", "configType"));


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
