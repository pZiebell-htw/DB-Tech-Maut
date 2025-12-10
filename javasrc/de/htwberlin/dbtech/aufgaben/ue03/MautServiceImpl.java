package de.htwberlin.dbtech.aufgaben.ue03;

import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.exceptions.AlreadyCruisedException;
import de.htwberlin.dbtech.exceptions.InvalidVehicleDataException;
import de.htwberlin.dbtech.exceptions.UnkownVehicleException;
import de.htwberlin.dbtech.utils.MautConstants;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Die Klasse realisiert den MautService.
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

    private void pruefeAchszahl(int erwartet, int gemeldet) throws InvalidVehicleDataException {
        if (erwartet <= 4) {
            if (erwartet != gemeldet) {
                throw new InvalidVehicleDataException(
                        "Achszahl inkorrekt! Gemeldet: " + gemeldet + ", Erwartet: " + erwartet);
            }
        } else {
            if (gemeldet < 5) {
                throw new InvalidVehicleDataException(
                        "Achszahl inkorrekt! Gemeldet: " + gemeldet + ", Erwartet: >=5");
            }
        }
    }

    @Override
    public void berechneMaut(int mautAbschnitt, int achszahl, String kennzeichen)
            throws UnkownVehicleException, InvalidVehicleDataException, AlreadyCruisedException {

        Long fahrzeugId = null;

        String sqlCheckVehicle = "SELECT FZ_ID FROM FAHRZEUG WHERE KENNZEICHEN = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sqlCheckVehicle)) {
            pstmt.setString(1, kennzeichen);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    fahrzeugId = rs.getLong("FZ_ID");
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }

        String sqlCheckOBU = "SELECT fzg.FZG_ID, f.ACHSEN "
                + "FROM FAHRZEUG f LEFT JOIN FAHRZEUGGERAT fzg ON f.FZ_ID = fzg.FZ_ID "
                + "WHERE f.FZ_ID = ? AND f.ABMELDEDATUM IS NULL "
                + "AND (fzg.STATUS = '" + MautConstants.FZG_STATUS_ACTIVE + "' OR fzg.STATUS IS NULL)";

        Long fzgGeratId = null;
        Integer dbAchsen = null;

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

        if (istAutomatik) {
            try {
                if (dbAchsen == null) {
                    throw new DataException("Fahrzeugdaten konnten nicht gelesen werden");
                }

                pruefeAchszahl(dbAchsen, achszahl);

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

                String sqlKat = "SELECT KATEGORIE_ID, MAUTSATZ_JE_KM FROM MAUTKATEGORIE "
                        + "WHERE SSKL_ID = ? AND "
                        + "( (ACHSZAHL = '" + MautConstants.ACHSZAHL_2 + "' AND ? = 2) "
                        + "OR (ACHSZAHL = '" + MautConstants.ACHSZAHL_3 + "' AND ? = 3) "
                        + "OR (ACHSZAHL = '" + MautConstants.ACHSZAHL_4 + "' AND ? = 4) "
                        + "OR (ACHSZAHL = '" + MautConstants.ACHSZAHL_5_PLUS + "' AND ? >= 5) )";

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

                double laengeKm = laengeMeter / MautConstants.METER_TO_KM;
                double kostenEuro = (laengeKm * mautSatzJeKm) / MautConstants.CENT_TO_EURO;
                kostenEuro = Math.round(kostenEuro * 100.0) / 100.0;

                long newMautId;
                String sqlMaxMaut = "SELECT NVL(MAX(MAUT_ID),0) + 1 AS NEUE_ID FROM MAUTERHEBUNG";
                try (PreparedStatement pstmt = getConnection().prepareStatement(sqlMaxMaut);
                     ResultSet rs = pstmt.executeQuery()) {
                    rs.next();
                    newMautId = rs.getLong("NEUE_ID");
                }

                String sqlInsert = "INSERT INTO MAUTERHEBUNG (MAUT_ID, ABSCHNITTS_ID, FZG_ID, KATEGORIE_ID, BEFAHRUNGSDATUM, KOSTEN) "
                        + "VALUES (?,?,?,?, SYSDATE, ?)";
                try (PreparedStatement pstmt = getConnection().prepareStatement(sqlInsert)) {
                    pstmt.setLong(1, newMautId);
                    pstmt.setInt(2, mautAbschnitt);
                    pstmt.setLong(3, fzgGeratId);
                    pstmt.setInt(4, kategorieId);
                    pstmt.setDouble(5, kostenEuro);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                throw new DataException(e);
            }

        } else {
            try {
                String sqlCheckManual = "SELECT 1 FROM BUCHUNG WHERE KENNZEICHEN = ? AND B_ID = "
                        + MautConstants.BUCHUNG_STATUS_OFFEN;
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
                    throw new UnkownVehicleException(
                            "Fahrzeug mit Kennzeichen " + kennzeichen + " ist unbekannt.");
                }

                String sqlOpenBooking = "SELECT b.BUCHUNG_ID, b.KATEGORIE_ID, mk.ACHSZAHL "
                        + "FROM BUCHUNG b JOIN MAUTKATEGORIE mk ON b.KATEGORIE_ID = mk.KATEGORIE_ID "
                        + "WHERE b.ABSCHNITTS_ID = ? AND b.KENNZEICHEN = ? AND b.B_ID = "
                        + MautConstants.BUCHUNG_STATUS_OFFEN;

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

                if (buchungId == null) {
                    throw new AlreadyCruisedException(
                            "Keine offene Buchung fuer Abschnitt " + mautAbschnitt + " gefunden");
                }

                boolean achsenOk;
                if (MautConstants.ACHSZAHL_2.equals(buchungsAchszahlKat)) {
                    achsenOk = (achszahl == 2);
                } else if (MautConstants.ACHSZAHL_3.equals(buchungsAchszahlKat)) {
                    achsenOk = (achszahl == 3);
                } else if (MautConstants.ACHSZAHL_4.equals(buchungsAchszahlKat)) {
                    achsenOk = (achszahl == 4);
                } else if (MautConstants.ACHSZAHL_5_PLUS.equals(buchungsAchszahlKat)) {
                    achsenOk = (achszahl >= 5);
                } else {
                    achsenOk = false;
                }

                if (!achsenOk) {
                    throw new InvalidVehicleDataException(
                            "Achszahl stimmt nicht mit gebuchter Kategorie ueberein");
                }

                String sqlUpdate = "UPDATE BUCHUNG SET B_ID = " + MautConstants.BUCHUNG_STATUS_ABGESCHLOSSEN
                        + ", BEFAHRUNGSDATUM = SYSDATE WHERE BUCHUNG_ID = ?";
                try (PreparedStatement pstmt = getConnection().prepareStatement(sqlUpdate)) {
                    pstmt.setLong(1, buchungId);
                    pstmt.executeUpdate();
                }

            } catch (SQLException e) {
                throw new DataException(e);
            }
        }
    }
}