package server.datalayerservice.interfaces;

import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalocalizationinformations.IDataLocalizationInformation;

@FunctionalInterface
public interface IDataLayerOperation<T extends IDataLocalizationInformation> {
    void execute(IDataLayer<T> dataLayer);
}
