package server.datalayerservice.datalocalizationinformations;

public interface IDataLocalizationInformation<T extends IDataLocalizationInformation<T>> {
    T getDataLocalizationInformation();
}