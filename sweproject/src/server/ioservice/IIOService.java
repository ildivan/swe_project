package server.ioservice;

import server.messages.Message;

public interface IIOService {
    public void write(Message message);
    public int readInteger(Message message);
    public int readIntegerWithMinMax(Message message);
    public String readString(Message message);
    public boolean readBoolean(Message message);
    public boolean continueChoice(Message message);
}
