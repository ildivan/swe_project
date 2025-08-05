package server.firstleveldomainservices.secondleveldomainservices.monthlyconfigservice.precludedateservice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalocalizationinformations.ILocInfoFactory;
import server.datalayerservice.datalocalizationinformations.JsonDataLocalizationInformation;
import server.jsonfactoryservice.IJsonFactoryService;
import server.jsonfactoryservice.JsonFactoryService;

public class PrecludeDateService {

    private final ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory;
    private final IJsonFactoryService jsonFactoryService = new JsonFactoryService();
    private final IDataLayer<JsonDataLocalizationInformation> dataLayer;
 
  

    public PrecludeDateService(ILocInfoFactory<JsonDataLocalizationInformation> locInfoFactory,
    IDataLayer<JsonDataLocalizationInformation> dataLayer) {

        this.dataLayer = dataLayer;
        this.locInfoFactory = locInfoFactory;

    }

    /**
     * permette il salvataggio di una data prelusa 
     * @param precludeDate
     */
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

}
