package backend.server.domainlevel.domainmanagers.menumanager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import backend.server.genericservices.ReadWrite;
import backend.server.utils.IOUtil;

public abstract class MenuManager extends ReadWrite implements IMenuManager{
    static final String QUESTION = "\n\nInserire scelta: (0 per uscire)";
    protected Map<String, Boolean> vociVisibili = new LinkedHashMap<>();
    protected Map<String, Runnable> chiamateMetodi = new LinkedHashMap<>();

     /**
     * build the menu based on the visibility of the options
     * @return
     */
    private List<String> buildMenu(){
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
        List<String> menu = buildMenu();
        String Smenu = toString(menu);
        boolean sceltaValida = true;
        int choice = 1000;
        Runnable toReturn = null;
        
        do{
            choice = IOUtil.readInteger(Smenu + QUESTION);
        
            if (choice >= 0 && choice <= menu.size()) {
                if(choice == 0){
                    return toReturn;
                }
                sceltaValida = true;
                String sceltaStringa = menu.get(choice - 1);
                toReturn = chiamateMetodi.get(sceltaStringa);
                
            } else {
                sceltaValida = false;
                write("Scelta non valida, riprovare", false);
            }
        }while(!sceltaValida);
        return toReturn;
    }

    /*
     * toString method to print the menu
     */
    abstract String toString(List<String> menu);

    
}
