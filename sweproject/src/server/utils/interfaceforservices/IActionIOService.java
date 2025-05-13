package server.utils.interfaceforservices;

@FunctionalInterface
public interface IActionIOService<R> {
    R apply(String message, Object... params);
}



