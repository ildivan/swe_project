package server.objects.interfaceforservices.iointerfaces;

@FunctionalInterface
public interface IActionIOService<R> {
    R apply(String message, Object... params);
}



