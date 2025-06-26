package server.datalayerservice.datalocalizationinformations;

public interface ILocInfoFactory <T extends IDataLocalizationInformation<T>>{
    T getActivityLocInfo();
    T getPlaceLocInfo();
    T getUserLocInfo();
    T getVolunteerLocInfo();
    T getConfigLocInfo();
    T getMonthlyConfigLocInfo();
    T getMonthlyPlanLocInfo();
    T getArchiveLocInfo();
    T getSubscriptionLocInfo();
    T getChangedActivitiesLocInfo();
    T getChangedPlacesLocInfo();
    T getChangedVolunteersLocInfo();
}
