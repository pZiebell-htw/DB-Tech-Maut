package de.htwberlin.dbtech.exceptions;

/**
 * @author Ingo Classen
 */
public class DataException extends RuntimeException {

    /**
     * Erzeugt eine Exception
     */
    public DataException() {
    }

    /**
     * Erzeugt eine Exception mit einer Nachricht.
     *
     * @param msg - die Nachricht
     */
    public DataException(String msg) {
        super(msg);
    }

    /**
     * Erzeugt eine Exception und verweist auf ein Throwable t
     *
     * @param t - das Throwable
     */
    public DataException(Throwable t) {
        super(t);
    }

    /**
     * Erzeugt eine Exception mit einer Nachricht und verweist auf ein Throwable t
     *
     * @param msg - die Nachricht
     * @param t   - das Throwable
     */
    public DataException(String msg, Throwable t) {
        super(msg, t);
    }

}