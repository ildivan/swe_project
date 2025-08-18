package server.daemonservices;

public interface IDaemon extends Runnable{
    void tick();
}
