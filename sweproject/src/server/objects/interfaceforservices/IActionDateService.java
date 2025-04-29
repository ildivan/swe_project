package server.objects.interfaceforservices;

@FunctionalInterface
public interface IActionDateService<R> {
    R apply(Object... params);
}



