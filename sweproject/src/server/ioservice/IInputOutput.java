package server.ioservice;

public interface IInputOutput {
    public int readInteger(String message);
    public int readIntegerWithMinMax(String message, int min, int max);
    public String readString(String message);
    public boolean readBoolean(String message);
    public void writeMessage (String message, boolean responseRequired);
}
