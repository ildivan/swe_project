package server.datalayerservice;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import server.GsonFactoryService;
import server.objects.interfaceforservices.IActionService;

public class JSONService {
    private static final Gson gson = (Gson) GsonFactoryService.Service.GET_GSON.start();

    public enum Service {
        CREATE_JSON((params) -> JSONService.createJson(params[0])),  // Serializzazione in Json
        CREATE_OBJECT((params) -> {
            // Verifica che il secondo parametro sia una Class
            if (params.length < 2 || !(params[1] instanceof Class)) {
                throw new IllegalArgumentException("Second parameter must be a Class type.");
            }
            return JSONService.createObject((JsonObject) params[0], (Class<?>) params[1]);
        });

        private final IActionService<?> service;

        Service(IActionService<?> service) {
            this.service = service;
        }

        @SuppressWarnings("unchecked")
        public <T> T start(Object... params) {
            return (T) service.apply(params);
        }
    }

    public static <T> JsonObject createJson(T object) {
        return gson.toJsonTree(object).getAsJsonObject();
    }

    public static <T> T createObject(JsonObject json, Class<T> c) {
        try {
            return gson.fromJson(json, c);
        } catch (JsonSyntaxException e) {
            // Gestire l'errore, ad esempio restituendo null o lanciando una RuntimeException
            e.printStackTrace();
            return null;
        }
    }
}
