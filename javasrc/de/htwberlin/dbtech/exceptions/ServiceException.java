package de.htwberlin.dbtech.exceptions;

/**
 * @author Ingo Classen
 */
public class ServiceException extends RuntimeException {

    /**
     * Erzeugt eine Exception
     */
    public ServiceException() {
    }

    /**
     * Erzeugt eine Exception mit einer Nachricht.
     *
     * @param msg - die Nachricht
     */
    public ServiceException(String msg) {
        super(msg);
    }

    /**
     * Erzeugt eine Exception und verweist auf ein Throwable t
     *
     * @param t - das Throwable
     */
    public ServiceException(Throwable t) {
        super(t);
    }

    /**
     * Erzeugt eine Exception mit einer Nachricht und verweist auf ein Throwable t
     *
     * @param msg - die Nachricht
     * @param t   - das Throwable
     */
    public ServiceException(String msg, Throwable t) {
        super(msg, t);
    }

}