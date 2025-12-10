package de.htwberlin.dbtech.utils;

/**
 * Enthält Konstanten für die Mautberechnung.
 * Vermeidet Hardcoding von Status-Werten und Kategorien.
 *
 * @author Gruppe 13
 */
public class MautConstants {


    // Status: Buchung ist offen (noch nicht befahren)
    public static final int BUCHUNG_STATUS_OFFEN = 1;

    // Status: Buchung wurde abgeschlossen (befahren)
    public static final int BUCHUNG_STATUS_ABGESCHLOSSEN = 3;


    // Status: Fahrzeuggerät ist aktiv
    public static final String FZG_STATUS_ACTIVE = "active";

    // Status: Fahrzeuggerät ist inaktiv
    public static final String FZG_STATUS_INACTIVE = "inactive";


    // Kategorie: Exakt 2 Achsen
    public static final String ACHSZAHL_2 = "= 2";

    // Kategorie: Exakt 3 Achsen
    public static final String ACHSZAHL_3 = "= 3";

    // Kategorie: Exakt 4 Achsen
    public static final String ACHSZAHL_4 = "= 4";

    // Kategorie: 5 oder mehr Achsen
    public static final String ACHSZAHL_5_PLUS = ">= 5";


    /** Faktor zur Umrechnung von Metern in Kilometer */
    public static final double METER_TO_KM = 1000.0;

    // Faktor zur Umrechnung von Cent in Euro
    public static final double CENT_TO_EURO = 100.0;


    // Private Constructor verhindert Instanziierung
    private MautConstants() {
        throw new IllegalStateException("Utility class - do not instantiate");
    }
}