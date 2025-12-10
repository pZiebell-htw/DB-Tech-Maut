package de.htwberlin.dbtech.aufgaben.ue03;

import java.sql.Connection;
import java.util.List;

import de.htwberlin.dbtech.aufgaben.ue02.IMautVerwaltung;
import de.htwberlin.dbtech.aufgaben.ue02.Mautabschnitt;
import de.htwberlin.dbtech.aufgaben.ue03.dao.BuchungDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.FahrzeugDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.FahrzeugDao.FahrzeugDaten;
import de.htwberlin.dbtech.aufgaben.ue03.dao.FahrzeuggeratDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.MautabschnittDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.MauterhebungDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.MautkategorieDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.MautkategorieDao.KategorieDaten;
import de.htwberlin.dbtech.aufgaben.ue03.dao.BuchungDao.BuchungsDaten;
import de.htwberlin.dbtech.utils.MautConstants;
import de.htwberlin.dbtech.exceptions.AlreadyCruisedException;
import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.exceptions.InvalidVehicleDataException;
import de.htwberlin.dbtech.exceptions.UnkownVehicleException;

public class MautServiceImplDao implements IMautVerwaltung, IMautService {

    private Connection connection;
    private FahrzeugDao fahrzeugDao;
    private FahrzeuggeratDao fahrzeuggeratDao;
    private MauterhebungDao mauterhebungDao;
    private MautabschnittDao mautabschnittDao;
    private BuchungDao buchungDao;
    private MautkategorieDao mautkategorieDao;

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
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

    @Override
    public void berechneMaut(int mautAbschnitt, int achszahl, String kennzeichen)
            throws UnkownVehicleException, InvalidVehicleDataException, AlreadyCruisedException {

        checkConnection();

        Long fahrzeugId = fahrzeugDao.findIdByKennzeichen(kennzeichen);

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

        if (istAutomatik) {
            if (dbAchsen == null) {
                throw new DataException("Fahrzeugdaten konnten nicht gelesen werden");
            }

            pruefeAchszahl(dbAchsen, achszahl);

            Integer ssklId = fahrzeugDao.findSsklId(fahrzeugId);
            if (ssklId == null) {
                throw new DataException("Fahrzeugdaten (SSKL) nicht gefunden");
            }

            KategorieDaten katDaten = mautkategorieDao.findeKategorie(ssklId, achszahl);
            if (katDaten == null) {
                throw new DataException("Keine passende Mautkategorie gefunden");
            }

            Double laengeMeter = mautabschnittDao.findLaenge(mautAbschnitt);
            if (laengeMeter == null) {
                throw new DataException("Mautabschnitt nicht gefunden: " + mautAbschnitt);
            }

            double laengeKm = laengeMeter / MautConstants.METER_TO_KM;
            double kostenEuro = (laengeKm * katDaten.mautSatzJeKm) / MautConstants.CENT_TO_EURO;
            kostenEuro = Math.round(kostenEuro * 100.0) / 100.0;

            mauterhebungDao.insertMauterhebung(mautAbschnitt, fzgGeratId, katDaten.kategorieId, kostenEuro);

        } else {
            boolean hatOffeneBuchung = buchungDao.hatOffeneBuchung(kennzeichen);

            if (!hatOffeneBuchung) {
                throw new UnkownVehicleException("Fahrzeug mit Kennzeichen " + kennzeichen + " ist unbekannt.");
            }

            BuchungsDaten buchungsDaten = buchungDao.findeOffeneBuchung(mautAbschnitt, kennzeichen);

            if (buchungsDaten == null) {
                throw new AlreadyCruisedException("Keine offene Buchung fuer Abschnitt " + mautAbschnitt + " gefunden");
            }

            boolean achsenOk;
            String katAchsen = buchungsDaten.achszahlKat;

            if (MautConstants.ACHSZAHL_2.equals(katAchsen)) {
                achsenOk = (achszahl == 2);
            } else if (MautConstants.ACHSZAHL_3.equals(katAchsen)) {
                achsenOk = (achszahl == 3);
            } else if (MautConstants.ACHSZAHL_4.equals(katAchsen)) {
                achsenOk = (achszahl == 4);
            } else if (MautConstants.ACHSZAHL_5_PLUS.equals(katAchsen)) {
                achsenOk = (achszahl >= 5);
            } else {
                achsenOk = false;
            }

            if (!achsenOk) {
                throw new InvalidVehicleDataException("Achszahl stimmt nicht mit gebuchter Kategorie ueberein");
            }

            buchungDao.schliesseBuchung(buchungsDaten.buchungId);
        }
    }
}