package me.slezica.tools;

public abstract class SilentRunnable implements Runnable {

    final public void run() {
        try {
            _run();
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
    
    protected abstract void _run() throws Exception;
}
