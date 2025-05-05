package tatc.util;
import java.time.*;
import org.apache.commons.math3.util.FastMath;

/** Utilities for mapping LTAN/LTDN ↔ RAAN. */
public final class OrbitalTimeUtils {

    private OrbitalTimeUtils() {}   // static only

    /**
     * Compute the Greenwich Mean Sidereal Time (GMST) at a UTC date‑time.
     * Accuracy: ~0.1° – good enough to place the plane at the correct local‑time slot.
     */
    public static double gmstDeg(final ZonedDateTime utc) {
        // Julian centuries from J2000
        final double jd  = julianDate(utc);
        final double t   = (jd - 2451545.0) / 36525.0;
        // GMST in seconds (IAU 2006, truncated)
        double gmstSec =
                67310.54841 +
                (876600.0 * 3600 + 8640184.812866) * t +
                0.093104 * t * t -
                6.2e-6 * t * t * t;
        gmstSec = ((gmstSec % 86400) + 86400) % 86400;      // wrap 0–86400 s
        return gmstSec / 240.0;       // 240 s per degree
    }

    /** Julian Date (UTC) */
    private static double julianDate(ZonedDateTime utc) {
        ZonedDateTime z = utc.withZoneSameInstant(ZoneOffset.UTC);
        int Y = z.getYear();
        int M = z.getMonthValue();
        int D = z.getDayOfMonth();
        int A = (14 - M) / 12;
        Y += 4800 - A;
        M += 12 * A - 3;
        int JDN = D + (153*M + 2)/5 + 365*Y + Y/4 - Y/100 + Y/400 - 32045;
        double dayFrac = (z.getHour()
                        + z.getMinute()/60.0
                        + (z.getSecond() + z.getNano()*1e-9)/3600.0)/24.0;
        return JDN + dayFrac;
    }

    public static String ltanToIsoTime(double ltanHours) {
        int hours = (int) ltanHours;
        int minutes = (int) ((ltanHours - hours) * 60);
        int seconds = (int) ((((ltanHours - hours) * 60) - minutes) * 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    

    /**
     * Returns the RAAN [deg 0‑360) that yields the requested **Local‑Time‑of‑Ascending‑Node**
     * at the given epoch.
     *
     * LTAN = 10.5  → 10 h 30 m local solar time.  
     * Common values: 6  (dawn), 10.5 ( AM SSO ), 13.5 ( PM SSO ), 18 (dusk).
     */
    public static double raanFromLTAN(double ltanHours, ZonedDateTime epochUtc) {
        double gmst = gmstDeg(epochUtc);
        //  λ_subsat = RAAN − GMST            (longitude of ascending node)
        //  LocalSolarTime = (GMST − λ_subsat)/15 + 12
        //  → RAAN = 2·GMST − (LTAN − 12)·15
        double raan = 2.0*gmst - (ltanHours - 12.0)*15.0;
        raan = (raan % 360.0 + 360.0) % 360.0;
        return raan;
    }
}
