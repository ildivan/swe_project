package server.datalayerservice.datalocalizationinformations;

public interface ILocInfoFactory {
    JsonDataLocalizationInformation getActivityLocInfo();
    JsonDataLocalizationInformation getPlaceLocInfo();
    JsonDataLocalizationInformation getUserLocInfo();
    JsonDataLocalizationInformation getVolunteerLocInfo();
    JsonDataLocalizationInformation getConfigLocInfo();
    JsonDataLocalizationInformation getMonthlyConfigLocInfo();
    JsonDataLocalizationInformation getMonthlyPlanLocInfo();
    JsonDataLocalizationInformation getArchiveLocInfo();

}
