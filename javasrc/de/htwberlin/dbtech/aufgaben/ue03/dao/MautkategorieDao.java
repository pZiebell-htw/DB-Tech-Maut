package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.utils.MautConstants;

public class MautkategorieDao {

    private Connection connection;

    public MautkategorieDao(Connection connection) {
        this.connection = connection;
    }

    public static class KategorieDaten {
        public int kategorieId;
        public double mautSatzJeKm;
    }

    public KategorieDaten findeKategorie(int ssklId, int achszahl) {
        String sql = "SELECT KATEGORIE_ID, MAUTSATZ_JE_KM FROM MAUTKATEGORIE "
                + "WHERE SSKL_ID = ? AND "
                + "( (ACHSZAHL = '" + MautConstants.ACHSZAHL_2 + "' AND ? = 2) "
                + "OR (ACHSZAHL = '" + MautConstants.ACHSZAHL_3 + "' AND ? = 3) "
                + "OR (ACHSZAHL = '" + MautConstants.ACHSZAHL_4 + "' AND ? = 4) "
                + "OR (ACHSZAHL = '" + MautConstants.ACHSZAHL_5_PLUS + "' AND ? >= 5) )";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, ssklId);
            pstmt.setInt(2, achszahl);
            pstmt.setInt(3, achszahl);
            pstmt.setInt(4, achszahl);
            pstmt.setInt(5, achszahl);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    KategorieDaten daten = new KategorieDaten();
                    daten.kategorieId = rs.getInt("KATEGORIE_ID");
                    daten.mautSatzJeKm = rs.getDouble("MAUTSATZ_JE_KM");
                    return daten;
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
        return null;
    }
}