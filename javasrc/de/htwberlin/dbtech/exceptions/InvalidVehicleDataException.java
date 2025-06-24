package de.htwberlin.dbtech.exceptions;

/**
 * @author Ingo Classen
 */
public class InvalidVehicleDataException extends RuntimeException {

    /**
     * Erzeugt eine Exception
     */
    public InvalidVehicleDataException() {
    }

    /**
     * Erzeugt eine Exception mit einer Nachricht.
     *
     * @param msg - die Nachricht
     */
    public InvalidVehicleDataException(String msg) {
        super(msg);
    }

    /**
     * Erzeugt eine Exception und verweist auf ein Throwable t
     *
     * @param t - das Throwable
     */
    public InvalidVehicleDataException(Throwable t) {
        super(t);
    }

    /**
     * Erzeugt eine Exception mit einer Nachricht und verweist auf ein Throwable t
     *
     * @param msg - die Nachricht
     * @param t   - das Throwable
     */
    public InvalidVehicleDataException(String msg, Throwable t) {
        super(msg, t);
    }

}