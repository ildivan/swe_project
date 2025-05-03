package server.datalayerservice.interfaces;

@FunctionalInterface
public interface IDataLayerFunction<T extends IDataLocalizationInformation, R> {
    R apply(IDataLayer<T> dataLayer);
}

