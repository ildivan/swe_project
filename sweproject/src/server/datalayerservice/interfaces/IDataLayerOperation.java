package server.datalayerservice.interfaces;

@FunctionalInterface
public interface IDataLayerOperation<T extends IDataLocalizationInformation> {
    void execute(IDataLayer<T> dataLayer);
}
