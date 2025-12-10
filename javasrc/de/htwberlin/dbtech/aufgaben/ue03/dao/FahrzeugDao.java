package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.utils.MautConstants;

public class FahrzeugDao {

    private Connection connection;

    public FahrzeugDao(Connection connection) {
        this.connection = connection;
    }

    public void insert(long fz_id, int sskl_id, int nutzer_id, String kennzeichen, String fin, int achsen, int gewicht,
                       String zulassungsland) {
        String sql = "INSERT INTO FAHRZEUG (FZ_ID, SSKL_ID, NUTZER_ID, KENNZEICHEN, FIN, ACHSEN, GEWICHT, ZULASSUNGSLAND, ANMELDEDATUM) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

    public void delete(long fz_id) {
        String sql = "DELETE FROM FAHRZEUG WHERE FZ_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, fz_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }

    public Long findIdByKennzeichen(String kennzeichen) {
        String sql = "SELECT FZ_ID FROM FAHRZEUG WHERE KENNZEICHEN = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, kennzeichen);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("FZ_ID");
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
        return null;
    }

    public Integer findSsklId(long fzgId) {
        String sql = "SELECT SSKL_ID FROM FAHRZEUG WHERE FZ_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, fzgId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("SSKL_ID");
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
        return null;
    }

    public static class FahrzeugDaten {
        public Integer achsen;
        public Long fzgGeratId;
    }

    public FahrzeugDaten findFahrzeugDaten(long fzgId) {
        String sql = "SELECT fzg.FZG_ID, f.ACHSEN "
                + "FROM FAHRZEUG f LEFT JOIN FAHRZEUGGERAT fzg ON f.FZ_ID = fzg.FZ_ID "
                + "WHERE f.FZ_ID = ? AND f.ABMELDEDATUM IS NULL "
                + "AND (fzg.STATUS = '" + MautConstants.FZG_STATUS_ACTIVE + "' OR fzg.STATUS IS NULL)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, fzgId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    FahrzeugDaten daten = new FahrzeugDaten();
                    daten.achsen = rs.getInt("ACHSEN");
                    long tmpId = rs.getLong("FZG_ID");
                    if (!rs.wasNull()) {
                        daten.fzgGeratId = tmpId;
                    }
                    return daten;
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
        return null;
    }
}