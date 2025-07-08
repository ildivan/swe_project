package frontend;

public class TerminalUser extends Terminal {
    
    public TerminalUser(String hostname) {
        super(hostname, 5001);
    }

    public static void main(String[] args) {
        FrontEndUtils.clearConsole();
        Terminal terminal = new TerminalUser("localhost");
        terminal.run();
    }
}


