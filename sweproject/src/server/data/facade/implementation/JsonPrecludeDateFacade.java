package server.data.facade.implementation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import server.data.facade.interfaces.IPrecludeDateFacade;
import server.data.json.datalayer.datalayers.JsonDataLayer;
import server.data.json.datalayer.datalocalizationinformations.IJsonLocInfoFactory;
import server.data.json.datalayer.datalocalizationinformations.JsonDataLocalizationInformation;
import server.data.json.datalayer.datareadwrite.IJsonReadWrite;
import server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.PrecludeDates;
import server.jsonfactoryservice.JsonFactoryService;

public class JsonPrecludeDateFacade implements IPrecludeDateFacade{

    private JsonDataLayer dataLayer;
    private final IJsonLocInfoFactory locInfoFactory;
    private final JsonFactoryService jsonFactoryService = new JsonFactoryService();

    public JsonPrecludeDateFacade(IJsonReadWrite readWrite, 
                            IJsonLocInfoFactory locInfoFactory) {
        this.dataLayer = new JsonDataLayer(readWrite);
        this.locInfoFactory = locInfoFactory;
    }

    /**
     * permette il salvataggio di una data prelusa 
     * @param precludeDate
     */
    @Override
    public void savePrecludeDate(LocalDate precludeDate){
        LocalDate dateOfPlanRelated = getDateOfPlanRelated(precludeDate);

        JsonDataLocalizationInformation locInfo = locInfoFactory.getPrecludeDatesLocInfo();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = dateOfPlanRelated.format(formatter);
        locInfo.setKey(formattedDate);

        PrecludeDates precludeDates;
        if(!dataLayer.exists(locInfo)){
            Set<LocalDate> precludeDatesList = new HashSet<>();
            precludeDatesList.add(precludeDate);
            precludeDates = new PrecludeDates(dateOfPlanRelated, precludeDatesList);
            dataLayer.add(jsonFactoryService.createJson(precludeDates), locInfo);
        }else{
            precludeDates = jsonFactoryService.createObject(dataLayer.get(locInfo), PrecludeDates.class);
            precludeDates.addPrecludeDate(precludeDate);
            dataLayer.modify(jsonFactoryService.createJson(precludeDates), locInfo);
        }
    }

    private LocalDate getDateOfPlanRelated(LocalDate precludeDate) {
        int month = precludeDate.getMonth().minus(1).getValue();
        int year = precludeDate.getYear();
        if(month == 12){
            year =- 1;
        }

        return LocalDate.of(year, month, 16);
    }

    /**
     * controlla se una data è preclusa per il mese relativo al piano che si sta costruendo
     * @param possiblePrecludeDate
     * @param dateOfPlanGeneration
     * @return
     */
    @Override
    public boolean checkIfIsPrecludeDate(LocalDate possiblePrecludeDate, LocalDate dateOfPlanGeneration) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getPrecludeDatesLocInfo();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = dateOfPlanGeneration.format(formatter);
        locInfo.setKey(formattedDate);

        if(dataLayer.get(locInfo)==null){
            return false;
        }else{
            PrecludeDates precludeDates = jsonFactoryService.createObject(dataLayer.get(locInfo), PrecludeDates.class);
            if(precludeDates.getPrecludeDates().contains(possiblePrecludeDate)){
                return true;
            }
            return false;
        }
    }

     /**
     * method to refrehs preclude dates
     */
    @Override
    public void refreshPrecludeDates(LocalDate dateOfPlan) {
        JsonDataLocalizationInformation locInfo = locInfoFactory.getPrecludeDatesLocInfo();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = dateOfPlan.format(formatter);
        locInfo.setKey(formattedDate);

        dataLayer.delete(locInfo);
    }


}
