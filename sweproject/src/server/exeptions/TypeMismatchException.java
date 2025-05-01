package server.exeptions;

public class TypeMismatchException extends RuntimeException {
    public TypeMismatchException(String expected, String actual) {
        super("Tipo errato: atteso " + expected + ", ma ottenuto " + actual);
    }
}
