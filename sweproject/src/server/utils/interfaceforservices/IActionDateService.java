package server.utils.interfaceforservices;

@FunctionalInterface
public interface IActionDateService<R> {
    R apply(Object... params);
}



