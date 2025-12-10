package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.utils.MautConstants;

public class BuchungDao {

    private Connection connection;

    public BuchungDao(Connection connection) {
        this.connection = connection;
    }

    public boolean hatOffeneBuchung(String kennzeichen) {
        String sql = "SELECT 1 FROM BUCHUNG WHERE KENNZEICHEN = ? AND B_ID = "
                + MautConstants.BUCHUNG_STATUS_OFFEN;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, kennzeichen);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }

    public static class BuchungsDaten {
        public long buchungId;
        public int kategorieId;
        public String achszahlKat;
    }

    public BuchungsDaten findeOffeneBuchung(int mautAbschnitt, String kennzeichen) {
        String sql = "SELECT b.BUCHUNG_ID, b.KATEGORIE_ID, mk.ACHSZAHL "
                + "FROM BUCHUNG b JOIN MAUTKATEGORIE mk ON b.KATEGORIE_ID = mk.KATEGORIE_ID "
                + "WHERE b.ABSCHNITTS_ID = ? AND b.KENNZEICHEN = ? AND b.B_ID = "
                + MautConstants.BUCHUNG_STATUS_OFFEN;

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

    public void schliesseBuchung(long buchungId) {
        String sql = "UPDATE BUCHUNG SET B_ID = " + MautConstants.BUCHUNG_STATUS_ABGESCHLOSSEN
                + ", BEFAHRUNGSDATUM = SYSDATE WHERE BUCHUNG_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, buchungId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }
}