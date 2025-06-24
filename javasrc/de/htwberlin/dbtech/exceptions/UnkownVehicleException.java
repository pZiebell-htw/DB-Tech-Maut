package de.htwberlin.dbtech.exceptions;

/**
 * @author Ingo Classen
 */
public class UnkownVehicleException extends RuntimeException {

    /**
     * Erzeugt eine Exception
     */
    public UnkownVehicleException() {
    }

    /**
     * Erzeugt eine Exception mit einer Nachricht.
     *
     * @param msg - die Nachricht
     */
    public UnkownVehicleException(String msg) {
        super(msg);
    }

    /**
     * Erzeugt eine Exception und verweist auf ein Throwable t
     *
     * @param t - das Throwable
     */
    public UnkownVehicleException(Throwable t) {
        super(t);
    }

    /**
     * Erzeugt eine Exception mit einer Nachricht und verweist auf ein Throwable t
     *
     * @param msg - die Nachricht
     * @param t   - das Throwable
     */
    public UnkownVehicleException(String msg, Throwable t) {
        super(msg, t);
    }

}