package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.htwberlin.dbtech.exceptions.DataException;

public class FahrzeuggeratDao {

    private Connection connection;

    public FahrzeuggeratDao(Connection connection) {
        this.connection = connection;
    }

    public String findStatusByFzgId(long fzg_id) {
        String status = null;
        String sql = "SELECT status FROM FAHRZEUGGERAT WHERE fzg_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, fzg_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    status = rs.getString("status");
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
        return status;
    }

    public void updateStatus(long fzg_id, String status) {
        String sql = "UPDATE FAHRZEUGGERAT SET STATUS = ? WHERE FZG_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setLong(2, fzg_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }
}