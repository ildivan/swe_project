package server.datalayerservice.interfaces;

import server.datalayerservice.datalayers.IDataLayer;
import server.datalayerservice.datalocalizationinformations.IDataLocalizationInformation;

@FunctionalInterface
public interface IDataLayerFunction<T extends IDataLocalizationInformation, R> {
    R apply(IDataLayer<T> dataLayer);
}

