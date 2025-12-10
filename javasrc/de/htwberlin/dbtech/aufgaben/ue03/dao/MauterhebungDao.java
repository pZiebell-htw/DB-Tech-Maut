package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.htwberlin.dbtech.exceptions.DataException;

public class MauterhebungDao {

    private Connection connection;

    public MauterhebungDao(Connection connection) {
        this.connection = connection;
    }

    public int findNutzerIdByMautId(int maut_id) {
        int nutzerId = 0;
        String sql = "SELECT f.NUTZER_ID " +
                "FROM MAUTERHEBUNG m " +
                "JOIN FAHRZEUGGERAT fg ON m.FZG_ID = fg.FZG_ID " +
                "JOIN FAHRZEUG f ON fg.FZ_ID = f.FZ_ID " +
                "WHERE m.MAUT_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, maut_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nutzerId = rs.getInt("NUTZER_ID");
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
        return nutzerId;
    }

    public void insertMauterhebung(int abschnittsId, long fzgGeratId, int kategorieId, double kosten) {
        long newMautId;
        String sqlMax = "SELECT NVL(MAX(MAUT_ID),0) + 1 AS NEUE_ID FROM MAUTERHEBUNG";
        String sqlInsert = "INSERT INTO MAUTERHEBUNG (MAUT_ID, ABSCHNITTS_ID, FZG_ID, KATEGORIE_ID, BEFAHRUNGSDATUM, KOSTEN) "
                + "VALUES (?,?,?,?, SYSDATE, ?)";

        try {
            try (PreparedStatement pstmt = connection.prepareStatement(sqlMax);
                 ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                newMautId = rs.getLong("NEUE_ID");
            }

            try (PreparedStatement pstmt = connection.prepareStatement(sqlInsert)) {
                pstmt.setLong(1, newMautId);
                pstmt.setInt(2, abschnittsId);
                pstmt.setLong(3, fzgGeratId);
                pstmt.setInt(4, kategorieId);
                pstmt.setDouble(5, kosten);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }
}