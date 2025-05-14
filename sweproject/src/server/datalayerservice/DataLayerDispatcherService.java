package server.datalayerservice;

import server.datalayerservice.interfaces.IDataLayerFunction;
import server.datalayerservice.interfaces.IDataLayerOperation;
import server.datalayerservice.interfaces.IDataLayer;
import server.datalayerservice.interfaces.IDataLocalizationInformation;

/**
 * classe che si occupa di gestire le operazioni sul datalayer capendo prima
 * ch tipo di datalayer sta utilizzando in base al tipo d informazioni di localizzazione
 * che riceve
 * 
 * riceve anche una lambda expression relativa all'operazione da eseguire
 * 
 * esempio:
 * --> con valore ritornato 
 * List<JsonObject> allUsers = DataLayerDispatcher.start(info, layer -> layer.getAll(info));
 * 
 * --> senza valore ritornato
 * DataLayerDispatcher.start(info, layer -> layer.add(jo, info));
 * 
 * nella lambda è NON è necessario che il nome del "layer" sia quello indicato qua sotto ◊
 * 
 * STATICA perche non ha bisogno di essere instanziata ne cambia se viene cambiato il datalayer
 */
public class DataLayerDispatcherService {

    /**
     * Esegue un'operazione sul data layer corretto, scelto in base al tipo di localizzazione fornito.
     *
     * @param localizationInfo Le informazioni per determinare il tipo di persistenza da usare.
     * @param operation L'operazione da eseguire sul data layer selezionato.
     * @param <T> Il tipo delle informazioni di localizzazione.
     */
    public static <T extends IDataLocalizationInformation> void start(T localizationInfo,IDataLayerOperation<T> operation) {
        assert localizationInfo != null;
        assert operation != null;
        IDataLayer<T> layer = getLayer(localizationInfo);
        operation.execute(layer);
    }

    /**
     * Esegue un'operazione sul data layer corretto e restituisce un risultato,
     * scegliendo il tipo di persistenza in base alle informazioni di localizzazione.
     *
     * @param localizationInfo Le informazioni per determinare il tipo di persistenza da usare.
     * @param operation L'operazione da eseguire.
     * @param <T> Il tipo delle informazioni di localizzazione.
     * @param <R> Il tipo del risultato.
     * @return Il risultato dell'operazione eseguita.
     */
    public static <T extends IDataLocalizationInformation, R> R startWithResult(T localizationInfo,IDataLayerFunction<T, R> operation) {
        assert localizationInfo != null;
        assert operation != null;
        IDataLayer<T> layer = getLayer(localizationInfo);
        return operation.apply(layer);
    }

    /**
     * Restituisce l'implementazione del data layer in base al tipo di localizzazione fornito.
     *
     * @param info Le informazioni di localizzazione.
     * @param <T> Il tipo delle informazioni.
     * @return L'istanza del data layer corrispondente.
     */
    private static <T extends IDataLocalizationInformation> IDataLayer<T> getLayer(T info) {
        if (info instanceof JsonDataLocalizationInformation) {
            return createJsonDataLayer();
        }
        // Altri tipi...
        throw new IllegalArgumentException("Tipo non supportato");
    }

    @SuppressWarnings("unchecked")
    private static <T extends IDataLocalizationInformation> IDataLayer<T> createJsonDataLayer() {
        return (IDataLayer<T>) new JsonDataLayer();
    }
}

