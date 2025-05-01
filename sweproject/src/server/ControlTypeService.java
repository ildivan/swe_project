package server;

import server.datalayerservice.JSONService;
import server.exeptions.TypeMismatchException;
import server.messages.Message;

public class ControlTypeService {

    // public enum Service {
    //     CONTROL_AND_GET(params -> {
    //         if (params.length != 2 || !(params[0] instanceof Message) || !(params[1] instanceof Class<?>)) {
    //             throw new IllegalArgumentException("Parametri non validi per CONTROL_AND_GET");
    //         }
    //         return ControlTypeService.controlAndGet((Message) params[0], (Class<?>) params[1]);
    //     });

    //     private IActionService<?> service;

    //     Service(IActionService<?> service) {
    //         this.service = service;
    //     }

    //     // il problem compile time viene ignorato poiche a run time il tipo viene deciso e controllato
    //     // in modo sicuro
    //     @SuppressWarnings("unchecked")
    //     public <T> T start(Object... params) {
    //         // Il tipo viene deciso dinamicamente, quindi il casting avviene in modo sicuro
    //         return (T) service.apply(params);
    //     }
    // }
    

    /**
     * Deserializza il contenuto del Message e controlla che il tipo risultante
     * corrisponda a quello atteso. Lancia un'eccezione se il tipo non è coerente.
     *
     * @param message il messaggio JSON da deserializzare
     * @param expectedClass la classe attesa del contenuto
     * @param <T> il tipo atteso
     * @return il contenuto deserializzato, tipizzato come T
     * @throws IllegalArgumentException se il risultato è nullo o il tipo non è coerente
     */
    private  <T> T safeDeserializeOrThrow(Message message, Class<T> expectedClass) {
        Object result = JSONService.Service.CREATE_OBJECT.start(
            message.getJsonObject(),
            message.getContainedClass()
        );

        if (result == null) {
            throw new IllegalArgumentException("Deserializzazione fallita: risultato nullo.");
        }

        if (!expectedClass.isInstance(result)) {
            throw new TypeMismatchException(
                expectedClass.getName(),
                result.getClass().getName()
            );
        }

        return expectedClass.cast(result);  // Cast sicuro
    }

    /**
     * Controlla il tipo del messaggio e restituisce il risultato deserializzato.
     * Se il tipo non corrisponde, lancia un'eccezione.
     * 
     * @param message
     * @param expectedClass
     * @param <T> il tipo atteso
     * @return il risultato deserializzato
     */
    public <T> T controlAndGet(Message message, Class<T> expectedClass) {
        // Chiamata alla deserializzazione sicura
        return safeDeserializeOrThrow(message, expectedClass);
    }
}
