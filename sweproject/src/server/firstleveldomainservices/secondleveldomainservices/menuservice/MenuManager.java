package server.firstleveldomainservices.secondleveldomainservices.menuservice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import server.ioservice.IInputOutput;
import server.ioservice.IOService;



public abstract class MenuManager implements MenuService{
    static final String QUESTION = "\n\nInserire scelta: (0 per uscire)";
    protected Map<String, Boolean> vociVisibili = new LinkedHashMap<>();
    protected Map<String, Runnable> chiamateMetodi = new LinkedHashMap<>();


    protected void add_to_menu(String menuVoiceText, Runnable callback) {
        vociVisibili.put(menuVoiceText, true);
        chiamateMetodi.put(menuVoiceText, callback);
    }

     /**
     * build the menu based on the visibility of the options
     * @return
     */
    protected List<String> buildMenu(){
        Map<String,Boolean> showableMap = buildMenuVisibility(this.vociVisibili);
        List<String> showableList = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : showableMap.entrySet()) {
            if (entry.getValue()) {
                //   write(String.valueOf(entry.getValue()), false);
                showableList.add(entry.getKey());
            }
        }

        return showableList;
    }

    /**
     * decide witch menu voice to show based on the date
     * @param map
     * @return
     */
    protected abstract Map<String,Boolean> buildMenuVisibility(Map<String, Boolean> map);
    

    /**
     * start the menu keeping the user in a loop until he decides to exit
     * 
     */
    @Override
    public Runnable startMenu(){
        IInputOutput ioService = new IOService();
        List<String> menu = buildMenu();
        String Smenu = toString(menu);
        boolean sceltaValida = true;
        int choice = 1000;
        Runnable toReturn = null;
        
        do{
            choice = ioService.readInteger(Smenu + QUESTION);
        
            if (choice >= 0 && choice <= menu.size()) {
                if(choice == 0){
                    return toReturn;
                }
                sceltaValida = true;
                String sceltaStringa = menu.get(choice - 1);
                toReturn = chiamateMetodi.get(sceltaStringa);
                
            } else {
                sceltaValida = false;
                ioService.writeMessage("Scelta non valida, riprovare", false);
            }
        }while(!sceltaValida);
        return toReturn;
    }

    /*
     * toString method to print the menu
     */
    protected abstract String toString(List<String> menu);

    
}
