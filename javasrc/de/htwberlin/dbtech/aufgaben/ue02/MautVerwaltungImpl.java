package de.htwberlin.dbtech.aufgaben.ue02;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.htwberlin.dbtech.exceptions.DataException;

/**
 * Die Klasse realisiert die Mautverwaltung.
 * @author Patrick Dohmeier
 */
public class MautVerwaltungImpl implements IMautVerwaltung {

    private static final Logger L = LoggerFactory.getLogger(MautVerwaltungImpl.class); // Logger für Debugging
    private Connection connection; // Datenbankverbindung

    /**
     * Speichert die Datenbankverbindung in der Instanzvariablen.
     * Wird von den Tests aufgerufen (Dependency Injection).
     */
    @Override
    public void setConnection(Connection connection) {
        this.connection = connection; // Speichere die übergebene Connection
    }

    /**
     * Gibt die Datenbankverbindung zurück.
     * Wirft Exception falls keine Connection gesetzt wurde (Fail-Fast).
     */
    private Connection getConnection() {
        if (connection == null) { // Prüfe ob Connection gesetzt ist
            throw new DataException("Connection not set"); // Fehler falls keine Connection vorhanden
        }
        return connection; // Gib die Connection zurück
    }

    /**
     * METHODE 1: READ - Einfacher SELECT
     * Liest den Status eines Fahrzeuggeräts aus der Datenbank.
     * Gibt "active" oder "inactive" zurück, je nach Gerätezustand.
     */
    @Override
    public String getStatusForOnBoardUnit(long fzg_id) {
        String status = null; // Variable für das Ergebnis
        String sql = "SELECT status FROM FAHRZEUGGERAT WHERE fzg_id = ?"; // SQL-Query mit Platzhalter

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) { // Erstelle PreparedStatement (wird auto-geschlossen)
            pstmt.setLong(1, fzg_id); // Setze Parameter: erster Platzhalter (?) = fzg_id

            try (ResultSet rs = pstmt.executeQuery()) { // Führe Query aus, hole Ergebnis (wird auto-geschlossen)
                if (rs.next()) { // Prüfe ob eine Zeile gefunden wurde
                    status = rs.getString("status"); // Lese Wert aus Spalte "status"
                }
            }
        } catch (SQLException e) { // Fange SQL-Fehler ab
            throw new DataException(e); // Wirf DataException weiter
        }
        return status; // Gib Status zurück (oder null)
    }

    /**
     * METHODE 2: READ mit JOINs - Komplexe Abfrage über 3 Tabellen
     * Ermittelt die Nutzernummer für eine Mauterhebung im automatischen Verfahren.
     * JOIN-Pfad: MAUTERHEBUNG → FAHRZEUGGERAT → FAHRZEUG → NUTZER_ID
     */
    @Override
    public int getUsernumber(int maut_id) {
        int nutzerId = 0; // Standardwert falls nichts gefunden

        String sql = "SELECT f.NUTZER_ID " + // Hole NUTZER_ID aus Tabelle FAHRZEUG (Alias f)
                "FROM MAUTERHEBUNG m " + // Von Tabelle MAUTERHEBUNG (Alias m)
                "JOIN FAHRZEUGGERAT fg ON m.FZG_ID = fg.FZG_ID " + // Verbinde mit FAHRZEUGGERAT über FZG_ID
                "JOIN FAHRZEUG f ON fg.FZ_ID = f.FZ_ID " + // Verbinde mit FAHRZEUG über FZ_ID
                "WHERE m.MAUT_ID = ?"; // Filtere nach der gesuchten MAUT_ID

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) { // Erstelle PreparedStatement
            pstmt.setInt(1, maut_id); // Setze Parameter: maut_id

            try (ResultSet rs = pstmt.executeQuery()) { // Führe Query aus
                if (rs.next()) { // Wenn Zeile gefunden
                    nutzerId = rs.getInt("NUTZER_ID"); // Lese NUTZER_ID
                }
            }
        } catch (SQLException e) { // Fange Fehler ab
            throw new DataException(e); // Wirf Exception weiter
        }
        return nutzerId; // Gib Nutzer-ID zurück
    }

    /**
     * METHODE 3: CREATE - INSERT-Operation
     * registriert ein neues Fahrzeug in der Datenbank für einen Nutzer.
     * Setzt automatisch das aktuelle Systemdatum als Anmeldedatum (SYSDATE).
     */
    @Override
    public void registerVehicle(long fz_id, int sskl_id, int nutzer_id, String kennzeichen,
                                String fin, int achsen, int gewicht, String zulassungsland) {

        String sql = "INSERT INTO FAHRZEUG (FZ_ID, SSKL_ID, NUTZER_ID, KENNZEICHEN, FIN, " + // INSERT-Statement
                "ACHSEN, GEWICHT, ZULASSUNGSLAND, ANMELDEDATUM) " + // Alle Spalten
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)"; // Platzhalter für Werte, SYSDATE = aktuelles Datum

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) { // Erstelle PreparedStatement
            pstmt.setLong(1, fz_id); // Parameter 1: Fahrzeug-ID
            pstmt.setInt(2, sskl_id); // Parameter 2: Schadstoffklassen-ID
            pstmt.setInt(3, nutzer_id); // Parameter 3: Nutzer-ID
            pstmt.setString(4, kennzeichen); // Parameter 4: Kennzeichen
            pstmt.setString(5, fin); // Parameter 5: FIN
            pstmt.setInt(6, achsen); // Parameter 6: Anzahl Achsen
            pstmt.setInt(7, gewicht); // Parameter 7: Gewicht
            pstmt.setString(8, zulassungsland); // Parameter 8: Zulassungsland

            pstmt.executeUpdate(); // Führe INSERT aus (executeUpdate für INSERT/UPDATE/DELETE)
        } catch (SQLException e) { // Fange Fehler ab
            throw new DataException(e); // Wirf Exception weiter
        }
    }

    /**
     * METHODE 4: UPDATE - Daten ändern
     * Aktualisiert den Status eines Fahrzeuggeräts in der Datenbank.
     * Ändert z.B. von "active" auf "inactive" oder "defect".
     */
    @Override
    public void updateStatusForOnBoardUnit(long fzg_id, String status) {
        String sql = "UPDATE FAHRZEUGGERAT SET STATUS = ? WHERE FZG_ID = ?"; // UPDATE-Statement

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) { // Erstelle PreparedStatement
            pstmt.setString(1, status); // Parameter 1: neuer Status (für SET)
            pstmt.setLong(2, fzg_id); // Parameter 2: Fahrzeuggerät-ID (für WHERE)

            pstmt.executeUpdate(); // Führe UPDATE aus
        } catch (SQLException e) { // Fange Fehler ab
            throw new DataException(e); // Wirf Exception weiter
        }
    }

    /**
     * METHODE 5: DELETE - Datensatz löschen
     * Löscht ein Fahrzeug aus der Datenbank.
     * Funktioniert nur wenn keine Foreign-Key-Constraints verletzt werden.
     */
    @Override
    public void deleteVehicle(long fz_id) {
        String sql = "DELETE FROM FAHRZEUG WHERE FZ_ID = ?"; // DELETE-Statement

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) { // Erstelle PreparedStatement
            pstmt.setLong(1, fz_id); // Parameter 1: Fahrzeug-ID
            pstmt.executeUpdate(); // Führe DELETE aus
        } catch (SQLException e) { // Fange Fehler ab
            throw new DataException(e); // Wirf Exception weiter
        }
    }

    /**
     * METHODE 6: READ mit Liste - Mehrere Datensätze laden
     * Lädt alle Mautabschnitte eines bestimmten Typs (z.B. "B13" für Bundesstraße 13).
     * Erstellt für jeden Datensatz ein Objekt und gibt eine Liste zurück.
     */
    @Override
    public List<Mautabschnitt> getTrackInformations(String abschnittstyp) {
        List<Mautabschnitt> abschnitte = new ArrayList<>(); // Erstelle leere Liste (nie null zurückgeben!)

        String sql = "SELECT * FROM MAUTABSCHNITT WHERE ABSCHNITTSTYP = ?"; // SELECT alle Spalten

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) { // Erstelle PreparedStatement
            pstmt.setString(1, abschnittstyp); // Parameter 1: Abschnittstyp (z.B. "B13")

            try (ResultSet rs = pstmt.executeQuery()) { // Führe Query aus
                while (rs.next()) { // Iteriere über ALLE Zeilen (while statt if!)
                    Mautabschnitt m = new Mautabschnitt(); // Erstelle neues Objekt für diese Zeile

                    m.setAbschnitts_id(rs.getInt("ABSCHNITTS_ID")); // Setze ID aus Spalte ABSCHNITTS_ID
                    m.setLaenge(rs.getInt("LAENGE")); // Setze Länge aus Spalte LAENGE
                    m.setStart_koordinate(rs.getString("START_KOORDINATE")); // Setze Start-Koordinate
                    m.setZiel_koordinate(rs.getString("ZIEL_KOORDINATE")); // Setze Ziel-Koordinate
                    m.setName(rs.getString("NAME")); // Setze Name
                    m.setAbschnittstyp(rs.getString("ABSCHNITTSTYP")); // Setze Abschnittstyp

                    abschnitte.add(m); // Füge Objekt zur Liste hinzu
                }
            }
        } catch (SQLException e) { // Fange Fehler ab
            throw new DataException(e); // Wirf Exception weiter
        }
        return abschnitte; // Gib Liste zurück (leer oder mit Objekten)
    }
}