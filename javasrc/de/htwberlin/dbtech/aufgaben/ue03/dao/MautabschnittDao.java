package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.htwberlin.dbtech.aufgaben.ue02.Mautabschnitt;
import de.htwberlin.dbtech.exceptions.DataException;

public class MautabschnittDao {

    private Connection connection;

    public MautabschnittDao(Connection connection) {
        this.connection = connection;
    }

    // --- Methode aus Übung 2 ---
    public List<Mautabschnitt> findByAbschnittstyp(String abschnittstyp) {
        List<Mautabschnitt> abschnitte = new ArrayList<>();
        String sql = "SELECT * FROM MAUTABSCHNITT WHERE ABSCHNITTSTYP = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, abschnittstyp);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Mautabschnitt m = new Mautabschnitt();
                    m.setAbschnitts_id(rs.getInt("ABSCHNITTS_ID"));
                    m.setLaenge(rs.getInt("LAENGE"));
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
        return abschnitte;
    }

    // --- NEUE Methode für Übung 3 ---
    public Double findLaenge(int abschnittsId) {
        String sql = "SELECT LAENGE FROM MAUTABSCHNITT WHERE ABSCHNITTS_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, abschnittsId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("LAENGE");
                }
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
        return null;
    }
}