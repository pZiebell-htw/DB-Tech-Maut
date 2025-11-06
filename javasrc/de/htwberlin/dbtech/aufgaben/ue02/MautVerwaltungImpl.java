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
        String sql = "SELECT f.NUTZER_ID " +
                "FROM MAUTERHEBUNG m " +
                "JOIN FAHRZEUGGERAT fg ON m.FZG_ID = fg.FZG_ID " +
                "JOIN FAHRZEUG f ON fg.FZ_ID = f.FZ_ID " +
                "WHERE m.MAUT_ID = ?";

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
    public void registerVehicle(long fz_id, int sskl_id, int nutzer_id, String kennzeichen, String fin, int achsen, int gewicht, String zulassungsland) {

        // Der Testkommentar erwähnt ein "Systemdatum".
        // Wir verwenden SYSDATE, die Oracle-Funktion für das aktuelle Datum.
        String sql = "INSERT INTO FAHRZEUG (FZ_ID, SSKL_ID, NUTZER_ID, KENNZEICHEN, FIN, ACHSEN, GEWICHT, ZULASSUNGSLAND, ANMELDEDATUM) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, fz_id);
            pstmt.setInt(2, sskl_id);
            pstmt.setInt(3, nutzer_id);
            pstmt.setString(4, kennzeichen);
            pstmt.setString(5, fin);
            pstmt.setInt(6, achsen);
            pstmt.setInt(7, gewicht);
            pstmt.setString(8, zulassungsland);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataException(e);
        }
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
        String sql = "DELETE FROM FAHRZEUG WHERE FZ_ID = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, fz_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }

    @Override
    public List<Mautabschnitt> getTrackInformations(String abschnittstyp) {
        // 1. Eine leere Liste erstellen (behebt die NullPointerException)
        List<Mautabschnitt> abschnitte = new ArrayList<>();

        String sql = "SELECT * FROM MAUTABSCHNITT WHERE ABSCHNITTSTYP = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, abschnittstyp);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Mautabschnitt m = new Mautabschnitt();

                    // KORREKTUR 1:
                    // Verwende setAbschnitts_id (mit Unterstrich)
                    // Verwende rs.getInt() (da die Methode ein int erwartet)
                    m.setAbschnitts_id(rs.getInt("ABSCHNITTS_ID"));

                    // KORREKTUR 2:
                    // Verwende rs.getInt() (da die Methode ein int erwartet)
                    m.setLaenge(rs.getInt("LAENGE"));

                    // Diese waren schon korrekt
                    m.setStart_koordinate(rs.getString("START_KOORDINATE"));
                    m.setZiel_koordinate(rs.getString("ZIEL_KOORDINATE"));
                    m.setName(rs.getString("NAME"));
                    m.setAbschnittstyp(rs.getString("ABSCHNITTSTYP"));

                    abschnitte.add(m);
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }

        // KORREKTUR 3:
        // Gib die Liste zurück, nicht null. (Das war der NullPointerException-Fehler)
        return abschnitte;
    }
}