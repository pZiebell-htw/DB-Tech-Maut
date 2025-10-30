package de.htwberlin.dbtech.aufgaben.ue02;

import java.sql.Connection;
import java.sql.PreparedStatement; // Wichtig: Import hinzufügen
import java.util.ArrayList; // <--- DIESEN IMPORT HINZUFÜGEN
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.htwberlin.dbtech.exceptions.DataException;

/**
 * Die Klasse realisiert die Mautverwaltung.
 * * @author Patrick Dohmeier
 */
public class MautVerwaltungImpl implements IMautVerwaltung {

    private static final Logger L = LoggerFactory.getLogger(MautVerwaltungImpl.class);
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
    public String getStatusForOnBoardUnit(long fzg_id) {
        String status = null;
        // Das SQL-Statement, um den Status basierend auf der fzg_id zu finden.
        // Das '?' ist ein Platzhalter.
        String sql = "SELECT status FROM FAHRZEUGGERAT WHERE fzg_id = ?";

        // Wir verwenden ein PreparedStatement, um SQL-Injection zu verhindern
        // und den Parameter sicher zu setzen.
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {

            // Setze den Wert für den ersten Platzhalter (?) auf die übergebene fzg_id
            pstmt.setLong(1, fzg_id);

            // Führe die Abfrage aus
            try (ResultSet rs = pstmt.executeQuery()) {

                // Prüfe, ob ein Datensatz gefunden wurde
                if (rs.next()) {
                    // Lese den Wert aus der Spalte "status"
                    status = rs.getString("status");
                }
            }
        } catch (SQLException e) {
            // Wirf eine DataException, falls ein SQL-Fehler auftritt
            throw new DataException(e);
        }

        return status;
    }

    @Override
    public int getUsernumber(int maut_id) {
        int nutzerId = 0; // Standardwert, falls nichts gefunden wird

        // SQL-Query mit JOINs über 3 Tabellen
        String sql = "SELECT T3.NUTZER_ID " +
                "FROM MAUTERHEBUNG T1 " +
                "JOIN FAHRZEUGGERAT T2 ON T1.FZG_ID = T2.FZG_ID " +
                "JOIN FAHRZEUG T3 ON T2.FZ_ID = T3.FZ_ID " +
                "WHERE T1.MAUT_ID = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {

            // Den maut_id Parameter in den Platzhalter (?) einfügen
            pstmt.setInt(1, maut_id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Den Wert aus der Spalte NUTZER_ID auslesen
                    nutzerId = rs.getInt("NUTZER_ID");
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }

        return nutzerId;
    }

    @Override
    public void registerVehicle(long fz_id, int sskl_id, int nutzer_id, String kennzeichen, String fin, int achsen,
                                int gewicht, String zulassungsland) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateStatusForOnBoardUnit(long fzg_id, String status) {
        String sql = "UPDATE FAHRZEUGGERAT SET STATUS = ? WHERE FZG_ID = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {

            // Parameter setzen:
            // 1. Platzhalter (?) ist der STATUS (String)
            pstmt.setString(1, status);
            // 2. Platzhalter (?) ist die FZG_ID (long)
            pstmt.setLong(2, fzg_id);

            // executeUpdate() wird für INSERT, UPDATE oder DELETE verwendet
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataException(e);
        }
    }

    @Override
    public void deleteVehicle(long fz_id) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Mautabschnitt> getTrackInformations(String abschnittstyp) {
        // TODO Auto-generated method stub
        return null;
    }
}