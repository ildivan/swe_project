package server.data.json.datalayer.datalocalizationinformations;

public interface IJsonLocInfoFactory {
    JsonDataLocalizationInformation getActivityLocInfo();
    JsonDataLocalizationInformation getPlaceLocInfo();
    JsonDataLocalizationInformation getUserLocInfo();
    JsonDataLocalizationInformation getVolunteerLocInfo();
    JsonDataLocalizationInformation getConfigLocInfo();
    JsonDataLocalizationInformation getMonthlyConfigLocInfo();
    JsonDataLocalizationInformation getMonthlyPlanLocInfo();
    JsonDataLocalizationInformation getArchiveLocInfo();
    JsonDataLocalizationInformation getSubscriptionLocInfo();
    JsonDataLocalizationInformation getChangedActivitiesLocInfo();
    JsonDataLocalizationInformation getChangedPlacesLocInfo();
    JsonDataLocalizationInformation getPrecludeDatesLocInfo();
}
