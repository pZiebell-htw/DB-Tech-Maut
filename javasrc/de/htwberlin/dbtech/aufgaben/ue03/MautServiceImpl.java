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
        // KORREKTUR: Long statt Integer verwenden, da die IDs sehr groß sind
        Long fahrzeugId = null;

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
                    // KORREKTUR: getLong statt getInt!
                    fahrzeugId = rs.getLong("FZ_ID");
                } else {
                    // NEIN: Das Fahrzeug wurde nicht gefunden -> Exception werfen
                    throw new UnkownVehicleException("Fahrzeug mit Kennzeichen " + kennzeichen + " ist unbekannt.");
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }

        // ---------------------------------------------------------
        // SCHRITT 2: Ist das Fahrzeug mit der korrekten Achszahl unterwegs?
        // ---------------------------------------------------------
        String sqlCheckAxles = "SELECT ACHSEN FROM FAHRZEUG WHERE FZ_ID = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sqlCheckAxles)) {
            // KORREKTUR: setLong statt setInt!
            pstmt.setLong(1, fahrzeugId); // Wir nutzen die ID aus Schritt 1

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int dbAchsen = rs.getInt("ACHSEN"); // Achsen sind klein genug für int
                    // Vergleich: Gemeldete Achsen vs. Datenbank-Achsen
                    if (dbAchsen != achszahl) {
                        // NEIN: Daten stimmen nicht überein -> Exception
                        throw new InvalidVehicleDataException("Achszahl inkorrekt! Gemeldet: " + achszahl + ", Erwartet: " + dbAchsen);
                    }
                    // JA: Alles okay, es geht weiter...
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }

        // ---------------------------------------------------------
        // SCHRITT 3: In welchem Verfahren befindet sich das Fahrzeug?
        // ---------------------------------------------------------
        boolean hatFahrzeugGeraet = false;
        // Wir prüfen einfach, ob eine ID in der Tabelle FAHRZEUGGERAT existiert
        String sqlCheckOBU = "SELECT FZG_ID FROM FAHRZEUGGERAT WHERE FZ_ID = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sqlCheckOBU)) {
            pstmt.setLong(1, fahrzeugId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    hatFahrzeugGeraet = true;
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }

        // Verzweigung basierend auf dem Ergebnis
        if (hatFahrzeugGeraet) {
            // =========================================================
            // SCHRITT 4a: Automatisches Verfahren
            // =========================================================
            // TODO: Maut berechnen und in MAUTERHEBUNG speichern

        } else {
            // =========================================================
            // SCHRITT 4b: Manuelles Verfahren
            // =========================================================
            // TODO: Offene Buchung suchen und schließen

        }
    }



}
