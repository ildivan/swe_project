package server.objects.interfaceforservices;

@FunctionalInterface
public interface IActionService<R> {
    R apply(Object... params);
}



