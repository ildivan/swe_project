import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;
import java.io.*;
import java.net.Socket;
import java.util.*;
import com.google.gson.Gson;
import server.Server;
import server.authservice.User;
import server.data.facade.implementation.TestJsonFacadeFactory;
import server.utils.ConfigType;
import server.utils.Message;
import static org.junit.Assert.*;



public class AuthenticationServiceTest {

    private  Server server;
    private  Thread serverThread;
    private  final int CLIENT_PORT = 5001;
    private  final Gson gson = new Gson();

    @Before
    public  void setUp() throws Exception {
        List<User> users = new ArrayList<User>();
        String hashedConfigPass = BCrypt.hashpw("pass", BCrypt.gensalt());
        String hashedFruitorePass = BCrypt.hashpw("pass", BCrypt.gensalt());

        users.add(new User("test_config", hashedConfigPass, "configuratore"));
        users.add(new User("test_fruitore", hashedFruitorePass, "fruitore"));

        server = new Server(ConfigType.NORMAL, users, new TestJsonFacadeFactory());

        serverThread = new Thread(() -> server.startServer(ConfigType.TEST));
        serverThread.setDaemon(true);
        serverThread.start();
        // Wait for server to start
        Thread.sleep(100);
    }

    @Test
    public void testLoginWithValidFruitore() throws Exception {
        Socket socket = new Socket("localhost", CLIENT_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        Message clearMsg = gson.fromJson(in.readLine(), Message.class);
        assertEquals("CLEAR", clearMsg.text);

        Message usernameMsg = gson.fromJson(in.readLine(), Message.class);
        assertEquals("Inserisci username:", usernameMsg.text);
        out.println("test_fruitore");
        
        Message passwordMsg = gson.fromJson(in.readLine(), Message.class);
        assertEquals("Inserisci password:", passwordMsg.text);
        out.println("pass");


        Message msg = gson.fromJson(in.readLine(), Message.class);
        assertFalse(msg.text.contains("Password sbagliata"));

        socket.close();
    }

    @Test
    public void testLoginWithInvalidUser() throws Exception {
        Socket socket = new Socket("localhost", CLIENT_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        Message clearMsg = gson.fromJson(in.readLine(), Message.class);
        assertEquals("CLEAR", clearMsg.text);

        Message insertUsernameMessage = gson.fromJson(in.readLine(), Message.class);
        assertEquals("Inserisci username:", insertUsernameMessage.text);
        out.println("invalid_user");

        Message invalidUserMsg = gson.fromJson(in.readLine(), Message.class);
        assertTrue(invalidUserMsg.text.contains("Utente inesistente"));

        out.println("n");
        
        Message closedMsg = gson.fromJson(in.readLine(), Message.class);
        assertTrue(closedMsg.text.contains("CONNESSIONE CHIUSA"));

        assertNull(in.readLine());
        socket.close();
    }

    @Test
    public void testLoginWithWrongPassword() throws Exception {
        Socket socket = new Socket("localhost", CLIENT_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        assertTrue(socket.isConnected());

        Message clearMsg = gson.fromJson(in.readLine(), Message.class);
        assertEquals("CLEAR", clearMsg.text);

        assertTrue(socket.isConnected());

        Message insertUsernameMessage = gson.fromJson(in.readLine(), Message.class);
        assertEquals("Inserisci username:", insertUsernameMessage.text);
        out.println("test_fruitore");

        assertTrue(socket.isConnected());
  
        for (int i = 0; i < 3; i++) {
            Message insertPasswordMessage = gson.fromJson(in.readLine(), Message.class);
            assertEquals("Inserisci password:", insertPasswordMessage.text);
            out.println("wrongpass");

            Message errorMsg = gson.fromJson(in.readLine(), Message.class);
            assertTrue(socket.isConnected());
            assertNotNull(errorMsg);
            if (i < 2) {
                assertTrue(errorMsg.text.contains("Password sbagliata"));
            } else {
                assertTrue(errorMsg.text.contains("Password sbagliata"));
                errorMsg = gson.fromJson(in.readLine(), Message.class);
                assertTrue(errorMsg.text.contains("Tentativi esauriti"));
            }
        }

        assertNull(in.readLine());
        socket.close();
    }

    @Test
    public void testLoginWithValidConfiguratoreOnClientPortFails() throws Exception {
        Socket socket = new Socket("localhost", CLIENT_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        Message clearMsg = gson.fromJson(in.readLine(), Message.class);
        assertEquals("CLEAR", clearMsg.text);

        Message insertUsernameMessage = gson.fromJson(in.readLine(), Message.class);
        assertEquals("Inserisci username:", insertUsernameMessage.text);
        out.println("test_config");

        Message msg = gson.fromJson(in.readLine(), Message.class);
        assertTrue(msg.text.contains("Terminale non corretto"));

        assertNull(in.readLine());
        socket.close();
    }

    @After
    public void tearDown() throws Exception {
        serverThread.interrupt();
    }
}
