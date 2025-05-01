package server.ioservice;

import java.io.IOException;
import java.net.Socket;

import server.messages.Message;

public interface IExternalFormatter {
    public void setConnection(Socket socket) throws IOException;
    public void write(Message message);
    public Message read() throws IOException;
}
