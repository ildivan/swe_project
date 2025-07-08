package frontend;

public class TerminalBackend extends Terminal {
    
    public TerminalBackend(String hostname) {
        super(hostname, 6001);
    }

    public static void main(String[] args) {
        FrontEndUtils.clearConsole();
        Terminal terminal = new TerminalBackend("localhost");
        terminal.run();
    }
}

