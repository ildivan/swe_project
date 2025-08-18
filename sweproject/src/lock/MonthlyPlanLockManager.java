package lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class MonthlyPlanLockManager {
    private static final ReentrantLock lock = new ReentrantLock();

    public static void lock() {
        lock.lock();
    }

    public static void unlock() {
        lock.unlock();
    }

    public static boolean tryLock() {
        return lock.tryLock();
    }

    public static boolean isLocked() {
        return lock.isLocked();
    }

    public static boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return lock.tryLock(timeout, unit);
    }

    public static boolean isHeldByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }
}
