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
import java.sql.Timestamp;

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
                // JA: Das Fahrzeug ist in der Fahrzeugtabelle bekannt. Wir merken uns die ID.
                // KORREKTUR: getLong statt getInt!
                    fahrzeugId = rs.getLong("FZ_ID");
                }
                // WICHTIG: Kein else-Zweig mit UnkownVehicleException mehr!
                // Laut Spezifikation ist ein Fahrzeug auch dann relevant,
                // wenn es zwar nicht registriert ist, aber eine offene
                // Buchung im manuellen Verfahren besitzt. Die Entscheidung,
                // ob das Fahrzeug wirklich "unbekannt" ist, wird daher
                // erst nach der Pruefung auf automatische _und_ manuelle
                // Verfahren getroffen.
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
        // ---------------------------------------------------------
        // SCHRITT 2 / 3: Verfahren & Achszahlpruefung je nach Verfahren
        // ---------------------------------------------------------
        // Wir pruefen zunaechst, ob das Fahrzeug ein aktives Fahrzeuggeraet
        // besitzt und damit im automatischen Verfahren unterwegs ist.
        String sqlCheckOBU = "SELECT fzg.FZG_ID, f.ACHSEN "

                + "FROM FAHRZEUG f LEFT JOIN FAHRZEUGGERAT fzg ON f.FZ_ID = fzg.FZ_ID "
                + "WHERE f.FZ_ID = ? AND f.ABMELDEDATUM IS NULL "
                + "AND (fzg.STATUS = 'active' OR fzg.STATUS IS NULL)";
        Long fzgGeratId = null;
        Integer dbAchsen = null;
        // Die Pruefung auf ein Fahrzeuggeraet und das Auslesen der echten
        // Achszahl ist nur sinnvoll, wenn wir ueberhaupt ein Fahrzeug in der
        // Fahrzeugtabelle gefunden haben. Fehlt der Fahrzeugeintrag komplett
        // (z.B. im reinen manuellen Verfahren), wird dieser Block uebersprungen
        // und spaeter nur das manuelle Verfahren betrachtet.
        if (fahrzeugId != null) {
            try (PreparedStatement pstmt = getConnection().prepareStatement(sqlCheckOBU)) {
                pstmt.setLong(1, fahrzeugId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        dbAchsen = rs.getInt("ACHSEN");
                        long tmpFzg = rs.getLong("FZG_ID");
                        if (!rs.wasNull()) {
                            fzgGeratId = tmpFzg;
                        }
                    }
                }
            } catch (SQLException e) {
                throw new DataException(e);
            }
        }
        boolean istAutomatik = (fzgGeratId != null);
        // ---------------------------------------------------------
        // Verfahrensspezifische Logik
        // ---------------------------------------------------------
        if (istAutomatik) {
        // =========================================================
        // SCHRITT 4a: Automatisches Verfahren
        // =========================================================
            try {
            // Zunaechst: Achszahlpruefung geme4df Spezifikation fuer
            // das automatische Verfahren ("echte" Achszahl aus der
            // Fahrzeugtabelle gegen die gemessene Achszahl pruefen).
                if (dbAchsen == null) {
                // sollte bei einem gueltigen Fahrzeug im Automatikverfahren
                // nicht vorkommen, da dann ein Datensatz in FAHRZEUG
                // existieren muss
                    throw new DataException("Fahrzeugdaten konnten nicht gelesen werden");
                }
                if (dbAchsen <= 4) {
                    if (dbAchsen != achszahl) {
                        throw new InvalidVehicleDataException(
                                "Achszahl inkorrekt! Gemeldet: " + achszahl + ", Erwartet: " + dbAchsen);
                    }
                } else {
                // Fahrzeuge mit 5 oder mehr Achsen werden nur gegen ">= 5" geprueft.
                    if (achszahl < 5) {
                        throw new InvalidVehicleDataException(
                                "Achszahl inkorrekt! Gemeldet: " + achszahl + ", Erwartet: >=5");
                    }
                }
                // 1) Schadstoffklasse fuer das Fahrzeug ermitteln
                String sqlSskl = "SELECT SSKL_ID FROM FAHRZEUG WHERE FZ_ID = ?";
                int ssklId;
                try (PreparedStatement pstmt = getConnection().prepareStatement(sqlSskl)) {
                    pstmt.setLong(1, fahrzeugId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new DataException("Fahrzeugdaten nicht gefunden");
                        }
                        ssklId = rs.getInt("SSKL_ID");
                    }
                }
                // 2) Passende Mautkategorie in Abhaengigkeit von Achsen suchen
                String sqlKat =
                        "SELECT KATEGORIE_ID, MAUTSATZ_JE_KM FROM MAUTKATEGORIE "
                                + "WHERE SSKL_ID = ? AND "
                                + "( (ACHSZAHL = '= 2' AND ? = 2) "
                                + "OR (ACHSZAHL = '= 3' AND ? = 3) "
                                + "OR (ACHSZAHL = '= 4' AND ? = 4) "
                                + "OR (ACHSZAHL = '>= 5' AND ? >= 5) )";
                int kategorieId;
                double mautSatzJeKm;
                try (PreparedStatement pstmt = getConnection().prepareStatement(sqlKat)) {
                    pstmt.setInt(1, ssklId);
                    pstmt.setInt(2, achszahl);
                    pstmt.setInt(3, achszahl);
                    pstmt.setInt(4, achszahl);
                    pstmt.setInt(5, achszahl);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new DataException("Keine passende Mautkategorie gefunden");
                        }
                        kategorieId = rs.getInt("KATEGORIE_ID");
                        mautSatzJeKm = rs.getDouble("MAUTSATZ_JE_KM");
                    }
                }
                // 3) Abschnittslaenge ermitteln
                String sqlLen = "SELECT LAENGE FROM MAUTABSCHNITT WHERE ABSCHNITTS_ID = ?";
                double laengeMeter;
                try (PreparedStatement pstmt = getConnection().prepareStatement(sqlLen)) {
                    pstmt.setInt(1, mautAbschnitt);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new DataException("Mautabschnitt nicht gefunden: " + mautAbschnitt);
                        }
                        laengeMeter = rs.getDouble("LAENGE");
                    }
                }
                // 4) Kosten berechnen (MAUTSATZ_JE_KM ist in Cent / km angegeben)
                double laengeKm = laengeMeter / 1000.0;
                double kostenEuro = (laengeKm * mautSatzJeKm) / 100.0;
                // auf 2 Nachkommastellen runden
                kostenEuro = Math.round(kostenEuro * 100.0) / 100.0;
                // 5) Neue MAUT_ID bestimmen
                long newMautId;
                String sqlMaxMaut = "SELECT NVL(MAX(MAUT_ID),0) + 1 AS NEUE_ID FROM MAUTERHEBUNG";
                try (PreparedStatement pstmt = getConnection().prepareStatement(sqlMaxMaut);
                     ResultSet rs = pstmt.executeQuery()) {
                    rs.next();
                    newMautId = rs.getLong("NEUE_ID");
                }
                // 6) Datensatz in MAUTERHEBUNG einfuegen
                String sqlInsert = "INSERT INTO MAUTERHEBUNG (MAUT_ID, ABSCHNITTS_ID, FZG_ID, KATEGORIE_ID, BEFAHRUNGSDATUM, KOSTEN) "
                        + "VALUES (?,?,?,?,?,?)";
                try (PreparedStatement pstmt = getConnection().prepareStatement(sqlInsert)) {
                    pstmt.setLong(1, newMautId);
                    pstmt.setInt(2, mautAbschnitt);
                    pstmt.setLong(3, fzgGeratId);
                    pstmt.setInt(4, kategorieId);
                    pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                    pstmt.setDouble(6, kostenEuro);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                throw new DataException(e);
            }

        } else {
        // =========================================================
        // SCHRITT 4b: Manuelles Verfahren
        // =========================================================
            try {
            // 0) Pruefen, ob das Fahrzeug ueberhaupt im manuellen Verfahren
            // unterwegs ist: dazu muss es mindestens eine offene Buchung
            // (B_ID = 1) fuer das Kennzeichen geben, unabhaengig vom
            // Mautabschnitt.
                String sqlCheckManual = "SELECT 1 FROM BUCHUNG WHERE KENNZEICHEN = ? AND B_ID = 1";
                boolean hatOffeneBuchung = false;
                try (PreparedStatement pstmt = getConnection().prepareStatement(sqlCheckManual)) {
                    pstmt.setString(1, kennzeichen);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            hatOffeneBuchung = true;
                        }
                    }
                }
                if (!hatOffeneBuchung) {
                // Weder automatisches Verfahren (kein aktives Fahrzeuggeraet)
                // noch eine offene Buchung im manuellen Verfahren vorhanden:
                // Das Fahrzeug ist aus Sicht des Systems unbekannt.
                    throw new UnkownVehicleException(
                            "Fahrzeug mit Kennzeichen " + kennzeichen + " ist unbekannt.");
                }
                // 1) Offene Buchung fuer das Kennzeichen und den Abschnitt suchen
                String sqlOpenBooking = "SELECT b.BUCHUNG_ID, b.KATEGORIE_ID, mk.ACHSZAHL "
                        + "FROM BUCHUNG b JOIN MAUTKATEGORIE mk ON b.KATEGORIE_ID = mk.KATEGORIE_ID "
                        + "WHERE b.ABSCHNITTS_ID = ? AND b.KENNZEICHEN = ? AND b.B_ID = 1";
                Long buchungId = null;
                int buchungsKategorieId = 0;
                String buchungsAchszahlKat = null;
                try (PreparedStatement pstmt = getConnection().prepareStatement(sqlOpenBooking)) {
                    pstmt.setInt(1, mautAbschnitt);
                    pstmt.setString(2, kennzeichen);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            buchungId = rs.getLong("BUCHUNG_ID");
                            buchungsKategorieId = rs.getInt("KATEGORIE_ID");
                            buchungsAchszahlKat = rs.getString("ACHSZAHL");
                        }
                    }
                }
                // Falls keine offene Buchung existiert, liegt eine Doppelbefahrung vor
                if (buchungId == null) {
                    throw new AlreadyCruisedException("Keine offene Buchung fuer Abschnitt " + mautAbschnitt + " gefunden");
                }
                // 2) Achszahl anhand der in der Kategorie hinterlegten Regel pruefen
                boolean achsenOk;
                if ("= 2".equals(buchungsAchszahlKat)) {
                    achsenOk = (achszahl == 2);
                } else if ("= 3".equals(buchungsAchszahlKat)) {
                    achsenOk = (achszahl == 3);
                } else if ("= 4".equals(buchungsAchszahlKat)) {
                    achsenOk = (achszahl == 4);
                } else if (">= 5".equals(buchungsAchszahlKat)) {
                    achsenOk = (achszahl >= 5);
                } else {
                    achsenOk = false;
                }
                if (!achsenOk) {
                    throw new InvalidVehicleDataException("Achszahl stimmt nicht mit gebuchter Kategorie ueberein");
                }
                // 3) Buchungsstatus auf "abgeschlossen" setzen und Befahrungszeitpunkt setzen
                String sqlUpdate = "UPDATE BUCHUNG SET B_ID = 3, BEFAHRUNGSDATUM = ? WHERE BUCHUNG_ID = ?";
                try (PreparedStatement pstmt = getConnection().prepareStatement(sqlUpdate)) {
                    pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                    pstmt.setLong(2, buchungId);
                    pstmt.executeUpdate();
                }

            } catch (SQLException e) {
                throw new DataException(e);
            }
        }
    }
}