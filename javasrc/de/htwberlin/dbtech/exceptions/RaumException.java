package de.htwberlin.dbtech.exceptions;

/**
 * @author Ingo Classen
 */
public class RaumException extends RuntimeException {

    /**
     * Erzeugt eine Exception
     */
    public RaumException() {
    }

    /**
     * Erzeugt eine Exception mit einer Nachricht.
     *
     * @param msg - die Nachricht
     */
    public RaumException(String msg) {
        super(msg);
    }

    /**
     * Erzeugt eine Exception und verweist auf ein Throwable t
     *
     * @param t - das Throwable
     */
    public RaumException(Throwable t) {
        super(t);
    }

    /**
     * Erzeugt eine Exception mit einer Nachricht und verweist auf ein Throwable t
     *
     * @param msg - die Nachricht
     * @param t   - das Throwable
     */
    public RaumException(String msg, Throwable t) {
        super(msg, t);
    }

}