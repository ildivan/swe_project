import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import server.Server;
import server.authservice.User;
import server.utils.ConfigType;
import static org.junit.Assert.*;

public class ServerTest {

    private static Server server;
    private static Thread serverThread;
    private static final int CLIENT_PORT = 5001;
    private static final int SERVER_PORT = 6001;

    @BeforeClass
    public static void setUp() throws Exception {
        List<User> users = new ArrayList<User>();
        users.add(new User("test_config", "pass", "configuratore"));
        users.add(new User("test_fruitore", "pass", "fruitore"));
        server = new Server(ConfigType.NORMAL, users);

        serverThread = new Thread(new Runnable() {
            public void run() {
                server.startServer(ConfigType.NORMAL);
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Wait for server to start
        Thread.sleep(1500);
    }

    @Test
    public void testClientPortOpen() throws Exception {
        Socket socket = new Socket("localhost", CLIENT_PORT);
        assertTrue(socket.isConnected());
        socket.close();
    }

    @Test
    public void testServerPortOpen() throws Exception {
        Socket socket = new Socket("localhost", SERVER_PORT);
        assertTrue(socket.isConnected());
        socket.close();
    }

    @Test
    public void testMultipleConnections() throws Exception {
        Socket socket1 = new Socket("localhost", CLIENT_PORT);
        Socket socket2 = new Socket("localhost", CLIENT_PORT);
        assertTrue(socket1.isConnected());
        assertTrue(socket2.isConnected());
        socket1.close();
        socket2.close();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        serverThread.interrupt();
    }
}
