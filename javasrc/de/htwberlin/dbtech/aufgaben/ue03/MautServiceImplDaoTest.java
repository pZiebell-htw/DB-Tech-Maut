package de.htwberlin.dbtech.aufgaben.ue03;

import de.htwberlin.dbtech.aufgaben.ue02.Mautabschnitt;
import de.htwberlin.dbtech.exceptions.AlreadyCruisedException;
import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.exceptions.InvalidVehicleDataException;
import de.htwberlin.dbtech.exceptions.UnkownVehicleException;
import de.htwberlin.dbtech.utils.DbCred;
import de.htwberlin.dbtech.utils.DbUnitUtils;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.RowOutOfBoundsException;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.ext.oracle.OracleDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 * Die Klasse enthaelt die Testfaelle fuer die Methoden des Mautservice /
 * Mauterhebung (DAO Implementierung).
 * Sie testet SOWOHL die Verwaltung (Übung 2) ALS AUCH die Berechnung (Übung 3).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MautServiceImplDaoTest {
    private static final Logger L = LoggerFactory.getLogger(MautServiceImplDaoTest.class);
    private static IDatabaseConnection dbTesterCon = null;

    // Wir nutzen hier direkt die Implementierungsklasse, um Zugriff auf alle Methoden zu haben
    private static final MautServiceImplDao maut = new MautServiceImplDao();

    @BeforeClass
    public static void setUp() {
        L.debug("setup: start");
        try {
            IDatabaseTester dbTester = new JdbcDatabaseTester(DbCred.driverClass, DbCred.url, DbCred.user,
                    DbCred.password,
                    DbCred.schema);
            dbTesterCon = dbTester.getConnection();
            dbTesterCon.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new OracleDataTypeFactory());

            // WICHTIG: Wir laden hier das Dataset für Übung 3/4, da dieses auch die Buchungen enthält
            IDataSet pre = new CsvDataSet(new File("test-data/ue03-04"));

            dbTester.setDataSet(pre);
            DatabaseOperation.CLEAN_INSERT.execute(dbTesterCon, pre);
            maut.setConnection(dbTesterCon.getConnection());
        } catch (Exception e) {
            DbUnitUtils.closeDbUnitConnectionQuietly(dbTesterCon);
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void tearDown() {
        L.debug("tearDown: start");
        DbUnitUtils.closeDbUnitConnectionQuietly(dbTesterCon);
    }

    // ==========================================================================
    // TEIL 1: Tests für Übung 2 (Verwaltung / CRUD)
    // ==========================================================================

    @org.junit.Test
    public void test01_Mautverwaltung_Status() {
        String status = maut.getStatusForOnBoardUnit(5456921154L);
        Assert.assertEquals("Der Status des Fahrzeuggeraetes stimmt nicht mit dem erwarteten Status ueberein!",
                "inactive", status);
    }

    @org.junit.Test
    public void test02_Mautverwaltung_Usernumber() {
        int nutzer_id = maut.getUsernumber(1015);
        Assert.assertEquals("Die Nutzernummer stimmt nicht mit der erwarteten ueberein!", 1000002, nutzer_id);
    }

    @org.junit.Test
    public void test03_Mautverwaltung_TrackInfo() {
        List<Mautabschnitt> m = maut.getTrackInformations("B13");
        Assert.assertEquals("Die Anzahl der geladenen Mautabschnittsobjekt stimmt nicht ueberein!", 3, m.size());
    }

    @org.junit.Test
    public void test04_Mautverwaltung_Register() throws Exception {
        maut.registerVehicle(100441556794622L, 3, 1000009, "935 GDD", "WD60554718", 5, 12500, "Spanien");
        int actual = countRecords(dbTesterCon.getConnection(), "FAHRZEUG", "fz_id = 100441556794622");
        Assert.assertEquals("Fahrzeug 100441556794622 ist nicht in der Datenbank!", 1, actual);
    }

    @org.junit.Test
    public void test05_Mautverwaltung_Delete() throws Exception {
        maut.deleteVehicle(100441556794623L);
        int actual = countRecords(dbTesterCon.getConnection(), "FAHRZEUG", "fz_id = 100441556794623");
        Assert.assertEquals("Fahrzeug 100441556794623 ist noch in der Datenbank!", 0, actual);
    }

    @org.junit.Test
    public void test06_Mautverwaltung_UpdateStatus() {
        maut.updateStatusForOnBoardUnit(1696502191, "inactive");
        Assert.assertEquals("Der Status des Fahrzeuggerätes stimmt nicht mit dem erwarteten Status ueberein!",
                "inactive", maut.getStatusForOnBoardUnit(1696502191));
    }

    // ==========================================================================
    // TEIL 2: Tests für Übung 3 (Berechnung / Logik)
    // ==========================================================================

    @org.junit.Test(expected = UnkownVehicleException.class)
    public void test07_Mauterhebung_Unknown() throws Exception {
        // Unbekanntes Fahrzeug (weder registriert noch offene Buchung)
        maut.berechneMaut(1200, 4, "LDS 677");
    }

    @org.junit.Test(expected = InvalidVehicleDataException.class)
    public void test08_Mauterhebung_InvalidData_Auto() throws Exception {
        // Automatik: Falsche Achszahl (gemeldet 4, Datenbank sagt mehr/weniger oder anders)
        // HH 8499 ist im Testdatensatz vorhanden
        maut.berechneMaut(1200, 4, "HH 8499");
    }

    @org.junit.Test(expected = InvalidVehicleDataException.class)
    public void test09_Mauterhebung_InvalidData_Manual() throws Exception {
        // Manuell: Falsche Achszahl gebucht
        maut.berechneMaut(1200, 3, "B CV 8890");
    }

    @org.junit.Test(expected = AlreadyCruisedException.class)
    public void test10_Mauterhebung_DoubleCruising() throws Exception {
        // Doppelbefahrung
        maut.berechneMaut(4174, 10, "DV 9413 NJ");
    }

    @org.junit.Test
    public void test11_Mauterhebung_Success_Manual() throws Exception {
        // Erfolg Manuell: Buchung abschließen
        maut.berechneMaut(1200, 4, "B CV 8890");

        QueryDataSet databaseDataSet = new QueryDataSet(dbTesterCon);
        databaseDataSet.addTable("BUCHUNG", "select * from BUCHUNG order by BUCHUNG_ID asc");
        ITable actualTable = databaseDataSet.getTable("BUCHUNG");

        // Prüfen ob B_ID auf 3 gesetzt wurde (Indiz 3 in der Tabelle entspricht der genutzten Buchung)
        Assert.assertEquals("Die Buchung ist nicht auf abgeschlossen gesetzt worden", "3",
                actualTable.getValue(3, "B_ID").toString());
    }

    @org.junit.Test
    public void test12_Mauterhebung_Success_Auto() throws Exception {
        // Erfolg Automatik: Neue Mauterhebung speichern
        maut.berechneMaut(1433, 5, "M 6569");

        try {
            QueryDataSet databaseDataSet = new QueryDataSet(dbTesterCon);
            databaseDataSet.addTable("MAUTERHEBUNG", "select * from MAUTERHEBUNG order by maut_id asc");
            ITable actualTable = databaseDataSet.getTable("MAUTERHEBUNG");

            // Wir prüfen den letzten Eintrag (Index 18 basierend auf den Testdaten)
            Assert.assertEquals("Die Berechnung der Maut war nicht korrekt", "0.68",
                    actualTable.getValue(18, "KOSTEN").toString().replace(',', '.'));
        } catch (RowOutOfBoundsException e) {
            Assert.fail("Es wurde keine Mauterhebung im Automatischen Verfahren gespeichert");
        }
    }

    /**
     * Hilfsfunktion zum Zählen von Datensätzen
     */
    int countRecords(Connection c, String tableName, String condition) {
        String sql = "select count(*) as anzahl from " + tableName + " where " + condition;
        try (Statement stmt = c.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                rs.next();
                return rs.getInt("anzahl");
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }
}