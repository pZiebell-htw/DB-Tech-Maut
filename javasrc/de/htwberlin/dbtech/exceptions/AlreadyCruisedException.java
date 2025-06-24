package de.htwberlin.dbtech.exceptions;

/**
 * @author Ingo Classen
 */
public class AlreadyCruisedException extends RuntimeException {

    /**
     * Erzeugt eine Exception
     */
    public AlreadyCruisedException() {
    }

    /**
     * Erzeugt eine Exception mit einer Nachricht.
     *
     * @param msg - die Nachricht
     */
    public AlreadyCruisedException(String msg) {
        super(msg);
    }

    /**
     * Erzeugt eine Exception und verweist auf ein Throwable t
     *
     * @param t - das Throwable
     */
    public AlreadyCruisedException(Throwable t) {
        super(t);
    }

    /**
     * Erzeugt eine Exception mit einer Nachricht und verweist auf ein Throwable t
     *
     * @param msg - die Nachricht
     * @param t   - das Throwable
     */
    public AlreadyCruisedException(String msg, Throwable t) {
        super(msg, t);
    }

}