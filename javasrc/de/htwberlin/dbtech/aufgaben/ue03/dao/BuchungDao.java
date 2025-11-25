package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import de.htwberlin.dbtech.exceptions.DataException;

public class BuchungDao {

    private Connection connection;

    public BuchungDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * Prüft, ob irgendeine offene Buchung (B_ID=1) für das Kennzeichen vorliegt.
     */
    public boolean hatOffeneBuchung(String kennzeichen) {
        String sql = "SELECT 1 FROM BUCHUNG WHERE KENNZEICHEN = ? AND B_ID = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, kennzeichen);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // true, wenn Datensatz gefunden
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }

    /**
     * Hilfsklasse, um mehrere Werte zurückzugeben
     */
    public static class BuchungsDaten {
        public long buchungId;
        public int kategorieId;
        public String achszahlKat; // z.B. ">= 5"
    }

    /**
     * Sucht eine konkrete offene Buchung für einen Abschnitt.
     * Gibt null zurück, wenn nichts gefunden wurde.
     */
    public BuchungsDaten findeOffeneBuchung(int mautAbschnitt, String kennzeichen) {
        String sql = "SELECT b.BUCHUNG_ID, b.KATEGORIE_ID, mk.ACHSZAHL "
                + "FROM BUCHUNG b JOIN MAUTKATEGORIE mk ON b.KATEGORIE_ID = mk.KATEGORIE_ID "
                + "WHERE b.ABSCHNITTS_ID = ? AND b.KENNZEICHEN = ? AND b.B_ID = 1";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, mautAbschnitt);
            pstmt.setString(2, kennzeichen);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BuchungsDaten daten = new BuchungsDaten();
                    daten.buchungId = rs.getLong("BUCHUNG_ID");
                    daten.kategorieId = rs.getInt("KATEGORIE_ID");
                    daten.achszahlKat = rs.getString("ACHSZAHL");
                    return daten;
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
        return null;
    }

    /**
     * Setzt eine Buchung auf abgeschlossen (B_ID=3).
     */
    public void schliesseBuchung(long buchungId) {
        String sql = "UPDATE BUCHUNG SET B_ID = 3, BEFAHRUNGSDATUM = ? WHERE BUCHUNG_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setLong(2, buchungId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }
}