package de.htwberlin.dbtech.aufgaben.ue03;

import java.sql.Connection;
import java.util.List;

import de.htwberlin.dbtech.aufgaben.ue02.IMautVerwaltung;
import de.htwberlin.dbtech.aufgaben.ue02.Mautabschnitt;
import de.htwberlin.dbtech.aufgaben.ue03.IMautService;
import de.htwberlin.dbtech.aufgaben.ue03.dao.BuchungDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.FahrzeugDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.FahrzeugDao.FahrzeugDaten;
import de.htwberlin.dbtech.aufgaben.ue03.dao.FahrzeuggeratDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.MautabschnittDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.MauterhebungDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.MautkategorieDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.MautkategorieDao.KategorieDaten;
import de.htwberlin.dbtech.aufgaben.ue03.dao.BuchungDao.BuchungsDaten;
import de.htwberlin.dbtech.exceptions.AlreadyCruisedException;
import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.exceptions.InvalidVehicleDataException;
import de.htwberlin.dbtech.exceptions.UnkownVehicleException;

public class MautServiceImplDao implements IMautVerwaltung, IMautService {

    private Connection connection;

    // Bestehende DAOs
    private FahrzeugDao fahrzeugDao;
    private FahrzeuggeratDao fahrzeuggeratDao;
    private MauterhebungDao mauterhebungDao;
    private MautabschnittDao mautabschnittDao;

    // Neue DAOs für Übung 3
    private BuchungDao buchungDao;
    private MautkategorieDao mautkategorieDao;

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
        // Alle DAOs initialisieren
        this.fahrzeugDao = new FahrzeugDao(connection);
        this.fahrzeuggeratDao = new FahrzeuggeratDao(connection);
        this.mauterhebungDao = new MauterhebungDao(connection);
        this.mautabschnittDao = new MautabschnittDao(connection);
        this.buchungDao = new BuchungDao(connection);
        this.mautkategorieDao = new MautkategorieDao(connection);
    }

    private void checkConnection() {
        if (connection == null) {
            throw new DataException("Connection not set");
        }
    }

    // --- Methoden aus Übung 2 (IMautVerwaltung) ---

    @Override
    public String getStatusForOnBoardUnit(long fzg_id) {
        checkConnection();
        return fahrzeuggeratDao.findStatusByFzgId(fzg_id);
    }

    @Override
    public int getUsernumber(int maut_id) {
        checkConnection();
        return mauterhebungDao.findNutzerIdByMautId(maut_id);
    }

    @Override
    public void registerVehicle(long fz_id, int sskl_id, int nutzer_id, String kennzeichen, String fin, int achsen,
                                int gewicht, String zulassungsland) {
        checkConnection();
        fahrzeugDao.insert(fz_id, sskl_id, nutzer_id, kennzeichen, fin, achsen, gewicht, zulassungsland);
    }

    @Override
    public void updateStatusForOnBoardUnit(long fzg_id, String status) {
        checkConnection();
        fahrzeuggeratDao.updateStatus(fzg_id, status);
    }

    @Override
    public void deleteVehicle(long fz_id) {
        checkConnection();
        fahrzeugDao.delete(fz_id);
    }

    @Override
    public List<Mautabschnitt> getTrackInformations(String abschnittstyp) {
        checkConnection();
        return mautabschnittDao.findByAbschnittstyp(abschnittstyp);
    }

    // --- Methode für Übung 3 (IMautService) ---

    @Override
    public void berechneMaut(int mautAbschnitt, int achszahl, String kennzeichen)
            throws UnkownVehicleException, InvalidVehicleDataException, AlreadyCruisedException {

        checkConnection();

        // SCHRITT 1: Ist das Fahrzeug bekannt?
        Long fahrzeugId = fahrzeugDao.findIdByKennzeichen(kennzeichen);

        // SCHRITT 2: Fahrzeugdaten (Achsen) und OBU-Status laden
        Integer dbAchsen = null;
        Long fzgGeratId = null;

        if (fahrzeugId != null) {
            FahrzeugDaten fDaten = fahrzeugDao.findFahrzeugDaten(fahrzeugId);
            if (fDaten != null) {
                dbAchsen = fDaten.achsen;
                fzgGeratId = fDaten.fzgGeratId;
            }
        }

        boolean istAutomatik = (fzgGeratId != null);

        // SCHRITT 3: Verfahrensspezifische Logik
        if (istAutomatik) {
            // --- Automatisches Verfahren ---

            if (dbAchsen == null) {
                throw new DataException("Fahrzeugdaten konnten nicht gelesen werden");
            }

            // Achszahl prüfen
            if (dbAchsen <= 4) {
                if (dbAchsen != achszahl) {
                    throw new InvalidVehicleDataException(
                            "Achszahl inkorrekt! Gemeldet: " + achszahl + ", Erwartet: " + dbAchsen);
                }
            } else {
                if (achszahl < 5) {
                    throw new InvalidVehicleDataException(
                            "Achszahl inkorrekt! Gemeldet: " + achszahl + ", Erwartet: >=5");
                }
            }

            // Schadstoffklasse holen
            Integer ssklId = fahrzeugDao.findSsklId(fahrzeugId);
            if (ssklId == null) {
                throw new DataException("Fahrzeugdaten (SSKL) nicht gefunden");
            }

            // Mautkategorie holen
            KategorieDaten katDaten = mautkategorieDao.findeKategorie(ssklId, achszahl);
            if (katDaten == null) {
                throw new DataException("Keine passende Mautkategorie gefunden");
            }

            // Abschnittslänge holen
            Double laengeMeter = mautabschnittDao.findLaenge(mautAbschnitt);
            if (laengeMeter == null) {
                throw new DataException("Mautabschnitt nicht gefunden: " + mautAbschnitt);
            }

            // Kosten berechnen
            double laengeKm = laengeMeter / 1000.0;
            double kostenEuro = (laengeKm * katDaten.mautSatzJeKm) / 100.0;
            kostenEuro = Math.round(kostenEuro * 100.0) / 100.0;

            // Speichern
            mauterhebungDao.insertMauterhebung(mautAbschnitt, fzgGeratId, katDaten.kategorieId, kostenEuro);

        } else {
            // --- Manuelles Verfahren ---

            // Prüfen, ob überhaupt eine offene Buchung vorliegt (sonst UnkownVehicleException)
            boolean hatOffeneBuchung = buchungDao.hatOffeneBuchung(kennzeichen);

            if (!hatOffeneBuchung) {
                throw new UnkownVehicleException("Fahrzeug mit Kennzeichen " + kennzeichen + " ist unbekannt.");
            }

            // Konkrete Buchung für den Abschnitt suchen
            BuchungsDaten buchungsDaten = buchungDao.findeOffeneBuchung(mautAbschnitt, kennzeichen);

            if (buchungsDaten == null) {
                throw new AlreadyCruisedException("Keine offene Buchung fuer Abschnitt " + mautAbschnitt + " gefunden");
            }

            // Achszahl prüfen
            boolean achsenOk;
            String katAchsen = buchungsDaten.achszahlKat;

            if ("= 2".equals(katAchsen)) {
                achsenOk = (achszahl == 2);
            } else if ("= 3".equals(katAchsen)) {
                achsenOk = (achszahl == 3);
            } else if ("= 4".equals(katAchsen)) {
                achsenOk = (achszahl == 4);
            } else if (">= 5".equals(katAchsen)) {
                achsenOk = (achszahl >= 5);
            } else {
                achsenOk = false;
            }

            if (!achsenOk) {
                throw new InvalidVehicleDataException("Achszahl stimmt nicht mit gebuchter Kategorie ueberein");
            }

            // Buchung schließen
            buchungDao.schliesseBuchung(buchungsDaten.buchungId);
        }
    }
}