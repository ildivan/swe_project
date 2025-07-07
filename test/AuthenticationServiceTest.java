import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.*;
import java.net.Socket;
import java.util.*;
import com.google.gson.Gson;
import server.Server;
import server.authservice.User;
import server.utils.ConfigType;
import server.utils.Message;
import static org.junit.Assert.*;

// filepath: c:\Users\ivanp\Documents\GitHub\swe_project\test\AuthenticationServiceTest.java





public class AuthenticationServiceTest {

    private static Server server;
    private static Thread serverThread;
    private static final int CLIENT_PORT = 5001;
    private static final Gson gson = new Gson();

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
    public void testLoginWithValidFruitore() throws Exception {
        Socket socket = new Socket("localhost", CLIENT_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        Message clearMsg = gson.fromJson(in.readLine(), Message.class);
        assertEquals("CLEAR", clearMsg.text);

        out.println("test_fruitore");
  
        out.println("pass");

        assertFalse(socket.isClosed());

        socket.close();
    }

    @Test
    public void testLoginWithInvalidUser() throws Exception {
        Socket socket = new Socket("localhost", CLIENT_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Read CLEAR message
        Message clearMsg = gson.fromJson(in.readLine(), Message.class);
        assertEquals("CLEAR", clearMsg.text);

        Message insertUsernameMessage = gson.fromJson(in.readLine(), Message.class);
        assertEquals("Inserisci username:", insertUsernameMessage.text);
        out.println("invalid_user");
        Message invalidUserMsg = gson.fromJson(in.readLine(), Message.class);
        System.out.println("Invalid user message: " + invalidUserMsg.text);
        assertTrue(invalidUserMsg.text.contains("Utente inesistente"));

        out.println("n");
        
        Message closedMsg = gson.fromJson(in.readLine(), Message.class);
        assertTrue(closedMsg.text.contains("CONNESSIONE CHIUSA"));
        assertTrue(socket.isClosed());
        socket.close();
    }

    @Test
    public void testLoginWithWrongPassword() throws Exception {
        Socket socket = new Socket("localhost", CLIENT_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Read CLEAR message
        Message clearMsg = gson.fromJson(in.readLine(), Message.class);
        assertEquals("CLEAR", clearMsg.text);

        Message insertUsernameMessage = gson.fromJson(in.readLine(), Message.class);
        assertEquals("Inserisci username:", insertUsernameMessage.text);
        out.println("test_fruitore");
        // Send wrong password 3 times
        for (int i = 0; i < 3; i++) {
            Message insertPasswordMessage = gson.fromJson(in.readLine(), Message.class);
            assertEquals("Inserisci password:", insertPasswordMessage.text);
            out.println("wrongpass");
            Message errorMsg = gson.fromJson(in.readLine(), Message.class);
            if (i < 2) {
                assertTrue(errorMsg.text.contains("Password sbagliata"));
            } else {
                assertTrue(errorMsg.text.contains("Password sbagliata"));
                errorMsg = gson.fromJson(in.readLine(), Message.class);
                assertTrue(errorMsg.text.contains("Tentativi esauriti"));
            }
        }

        assertTrue(socket.isClosed());
        socket.close();
    }

    @Test
    public void testLoginWithValidConfiguratoreOnClientPortFails() throws Exception {
        Socket socket = new Socket("localhost", CLIENT_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        Message clearMsg = gson.fromJson(in.readLine(), Message.class);
        assertEquals("CLEAR", clearMsg.text);

        out.println("test_config");

        out.println("pass");

        Message msg = gson.fromJson(in.readLine(), Message.class);
        assertTrue(msg.text.contains("Terminale non corretto"));

        socket.close();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        serverThread.interrupt();
    }
}
