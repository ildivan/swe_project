package server.datalayerservice.datalocalizationinformations;

public interface ILocInfoFactory {
    IDataLocalizationInformation getActivityLocInfo();
    IDataLocalizationInformation getPlaceLocInfo();
    IDataLocalizationInformation getUserLocInfo();
    IDataLocalizationInformation getVolunteerLocInfo();
    IDataLocalizationInformation getConfigLocInfo();
    IDataLocalizationInformation getMonthlyConfigLocInfo();
    IDataLocalizationInformation getMonthlyPlanLocInfo();
    IDataLocalizationInformation getArchiveLocInfo();

}
