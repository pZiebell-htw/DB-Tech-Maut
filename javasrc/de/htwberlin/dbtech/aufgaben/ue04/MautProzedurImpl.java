package de.htwberlin.dbtech.aufgaben.ue04;

import de.htwberlin.dbtech.aufgaben.ue03.IMautService;
import de.htwberlin.dbtech.exceptions.AlreadyCruisedException;
import de.htwberlin.dbtech.exceptions.InvalidVehicleDataException;
import de.htwberlin.dbtech.exceptions.UnkownVehicleException;
import de.htwberlin.dbtech.exceptions.DataException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Diese Klasse ruft die gespeicherte Prozedur BERECHNEMAUT im Package
 * MAUT_SERVICE auf und prüft gemäß der Paket-Spezifikation die Bedingungen zur
 * Mauterhebung. Sind alle Vorraussetzungen erfüllt wird die Maut für ein
 * bestimmtes Fahrzeug auf einem Mautabschnitt erhoben und gespeichert.
 **/

public class MautProzedurImpl implements IMautService {

	private Connection connection;

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	protected Connection getConnection() {
		if (connection == null)
			throw new IllegalStateException(
					"Connection has not been set on DAO before usage");
		return connection;
	}

	@Override
	public void berechneMaut(int mautAbschnitt, int achszahl, String kennzeichen)
			throws UnkownVehicleException, InvalidVehicleDataException, AlreadyCruisedException {
		try (CallableStatement cstmt = getConnection().prepareCall("CALL maut_service.BERECHNEMAUT(?,?,?)")) {
			cstmt.setInt(1, mautAbschnitt);
			cstmt.setInt(2, achszahl);
			cstmt.setString(3, kennzeichen);
			cstmt.execute();
		} catch (SQLException exp) {
			if (exp.getSQLState().equals("72000")
					&& exp.getErrorCode() == 20001) {
				throw new UnkownVehicleException(exp);
			} else if (exp.getSQLState().equals("72000")
					&& exp.getErrorCode() == 20002) {
				throw new InvalidVehicleDataException(exp.getMessage());
			} else if (exp.getSQLState().equals("72000")
					&& exp.getErrorCode() == 20003) {
				throw new AlreadyCruisedException(exp.getMessage());
			} else {
				throw new DataException(exp);
			}
		}
	}
}