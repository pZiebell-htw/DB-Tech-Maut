package de.htwberlin.dbtech.aufgaben.ue03;

import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.exceptions.AlreadyCruisedException;
import de.htwberlin.dbtech.exceptions.InvalidVehicleDataException;
import de.htwberlin.dbtech.exceptions.UnkownVehicleException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Die Klasse realisiert den AusleiheService.
 * 
 * @author Patrick Dohmeier
 */
public class MautServiceImpl implements IMautService {

	private static final Logger L = LoggerFactory.getLogger(MautServiceImpl.class);
	private Connection connection;

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	private Connection getConnection() {
		if (connection == null) {
			throw new DataException("Connection not set");
		}
		return connection;
	}

    @Override
    public void berechneMaut(int mautAbschnitt, int achszahl, String kennzeichen)
            throws UnkownVehicleException, InvalidVehicleDataException, AlreadyCruisedException {

        // Variable für die Fahrzeug-ID (brauchen wir später für weitere Schritte)
        Integer fahrzeugId = null;

        // ---------------------------------------------------------
        // SCHRITT 1: Ist das Fahrzeug bekannt?
        // ---------------------------------------------------------
        String sqlCheckVehicle = "SELECT FZ_ID FROM FAHRZEUG WHERE KENNZEICHEN = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sqlCheckVehicle)) {
            // Das Kennzeichen aus den Methodenparametern setzen
            pstmt.setString(1, kennzeichen);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // JA: Das Fahrzeug ist bekannt. Wir merken uns die ID.
                    fahrzeugId = rs.getInt("FZ_ID");
                } else {
                    // NEIN: Das Fahrzeug wurde nicht gefunden -> Exception werfen
                    throw new UnkownVehicleException("Fahrzeug mit Kennzeichen " + kennzeichen + " ist unbekannt.");
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }

        // TODO: Hier geht es weiter mit Schritt 2: Achszahl prüfen...
    }



}
