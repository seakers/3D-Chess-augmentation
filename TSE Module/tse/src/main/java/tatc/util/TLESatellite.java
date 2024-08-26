package tatc.util;

import org.hipparchus.util.FastMath;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * This class is a container for a single set of TLE data.
 * Adapted from Orekit 8.0 source code to be able to read the planet labs database in tse/resources/planet_mc.tle
 * Link to the source: https://www.orekit.org/static/jacoco/org.orekit.propagation.analytical.tle/TLE.java.html
 */
public class TLESatellite {

    /** Pattern for line 1. */
    private static final Pattern LINE_1_PATTERN =
            Pattern.compile("1 [ 0-9]{5}[A-Z] [ 0-9]{5}[ A-Z]{3} [ 0-9]{5}[.][ 0-9]{8} (?:(?:[ 0+-][.][ 0-9]{8})|(?: [ +-][.][ 0-9]{7})) " +
                    "[ +-][ 0-9]{5}[+-][ 0-9] [ +-][ 0-9]{5}[+-][ 0-9] [ 0-9] [ 0-9]{4}[ 0-9]");

    /** Pattern for line 2. */
    private static final Pattern LINE_2_PATTERN =
            Pattern.compile("2 [ 0-9]{5} [ 0-9]{3}[.][ 0-9]{4} [ 0-9]{3}[.][ 0-9]{4} [ 0-9]{7} " +
                    "[ 0-9]{3}[.][ 0-9]{4} [ 0-9]{3}[.][ 0-9]{4} [ 0-9]{2}[.][ 0-9]{13}[ 0-9]");

    /** International symbols for parsing. */
    private static final DecimalFormatSymbols SYMBOLS =
            new DecimalFormatSymbols(Locale.US);


    /** The satellite number. */
    private final int satelliteNumber;

    /** Classification (U for unclassified). */
    private final char classification;

    /** Launch year. */
    private final int launchYear;

    /** Launch number. */
    private final int launchNumber;

    /** Piece of launch (from "A" to "ZZZ"). */
    private final String launchPiece;

    /** Type of ephemeris. */
    private final int ephemerisType;

    /** Element number. */
    private final int elementNumber;

    /** the TLE current date. */
    private final AbsoluteDate epoch;

    /** Mean motion (rad/s). */
    private final double meanMotion;

    /** Mean motion first derivative (rad/s²). */
    private final double meanMotionFirstDerivative;

    /** Mean motion second derivative (rad/s³). */
    private final double meanMotionSecondDerivative;

    /** Eccentricity. */
    private final double eccentricity;

    /** Inclination (rad). */
    private final double inclination;

    /** Argument of perigee (rad). */
    private final double pa;

    /** Right Ascension of the Ascending node (rad). */
    private final double raan;

    /** Mean anomaly (rad). */
    private final double meanAnomaly;

    /** Revolution number at epoch. */
    private final int revolutionNumberAtEpoch;

    /** Ballistic coefficient. */
    private final double bStar;

    /** First line. */
    private String line1;

    /** Second line. */
    private String line2;

    /** Simple constructor from unparsed two lines.
     * <p>The static method {@link #isFormatOK(String, String)} should be called
     * before trying to build this object.<p>
     * @param line1 the first element (69 char String)
     * @param line2 the second element (69 char String)
     */
    public TLESatellite(final String line1, final String line2) {

        // identification
        satelliteNumber = parseInteger(line1, 2, 5);
        final int satNum2 = parseInteger(line2, 2, 5);
        if (satelliteNumber != satNum2) {
            throw new IllegalArgumentException("Lines 1 and 2 do not refer to the same object");
        }
        classification  = line1.charAt(7);
//        launchYear      = parseYear(line1, 9);
//        launchNumber    = parseInteger(line1, 11, 3);
//        launchPiece     = line1.substring(14, 17).trim();
        launchYear = 0;
        launchNumber = 0;
        launchPiece = "";
        ephemerisType   = parseInteger(line1, 62, 1);
        elementNumber   = parseInteger(line1, 64, 4);

        // Date format transform (nota: 27/31250 == 86400/100000000)
        final int    year      = parseYear(line1, 18);
        final int    dayInYear = parseInteger(line1, 20, 3);
        final long   df        = 27l * parseInteger(line1, 24, 8);
        final int    secondsA  = (int) (df / 31250l);
        final double secondsB  = (df % 31250l) / 31250.0;
        epoch           = new AbsoluteDate(0,0,0,0,0,0);


        // mean motion development
        meanMotion                 = parseDouble(line2, 52, 11);
        meanMotionFirstDerivative  = parseDouble(line1, 33, 10);
        meanMotionSecondDerivative = Double.parseDouble((line1.substring(44, 45) + '.' +
                line1.substring(45, 50) + 'e' +
                line1.substring(50, 52)).replace(' ', '0'));

        eccentricity = Double.parseDouble("." + line2.substring(26, 33).replace(' ', '0'));
        inclination  = parseDouble(line2, 8, 8);
        pa           = parseDouble(line2, 34, 8);
        raan         = Double.parseDouble(line2.substring(17, 25).replace(' ', '0'));
        meanAnomaly  = parseDouble(line2, 43, 8);

        revolutionNumberAtEpoch = parseInteger(line2, 63, 5);
        bStar = Double.parseDouble((line1.substring(53, 54) + '.' +
                line1.substring(54, 59) + 'e' +
                line1.substring(59, 61)).replace(' ', '0'));

        // save the lines
        this.line1 = line1;
        this.line2 = line2;

    }

    /** Get the first line.
     * @return first line
     */
    public String getLine1() {
        return line1;
    }

    /** Get the second line.
     * @return second line
     */
    public String getLine2() {
        return line2;
    }


    /** Parse a double.
     * @param line line to parse
     * @param start start index of the first character
     * @param length length of the string
     * @return value of the double
     */
    private double parseDouble(final String line, final int start, final int length) {
        final String field = line.substring(start, start + length).trim();
        return field.length() > 0 ? Double.parseDouble(field.replace(' ', '0')) : 0;
    }

    /** Parse an integer.
     * @param line line to parse
     * @param start start index of the first character
     * @param length length of the string
     * @return value of the integer
     */
    private int parseInteger(final String line, final int start, final int length) {
        final String field = line.substring(start, start + length).trim();
        return field.length() > 0 ? Integer.parseInt(field.replace(' ', '0')) : 0;
    }

    /** Parse a year written on 2 digits.
     * @param line line to parse
     * @param start start index of the first character
     * @return value of the year
     */
    private int parseYear(final String line, final int start) {
        final int year = 2000 + parseInteger(line, start, 2);
        return (year > 2056) ? (year - 100) : year;
    }

    /** Get the satellite id.
     * @return the satellite number
     */
    public int getSatelliteNumber() {
        return satelliteNumber;
    }

    /** Get the classification.
     * @return classification
     */
    public char getClassification() {
        return classification;
    }

    /** Get the launch year.
     * @return the launch year
     */
    public int getLaunchYear() {
        return launchYear;
    }

    /** Get the launch number.
     * @return the launch number
     */
    public int getLaunchNumber() {
        return launchNumber;
    }

    /** Get the launch piece.
     * @return the launch piece
     */
    public String getLaunchPiece() {
        return launchPiece;
    }

    /** Get the element number.
     * @return the element number
     */
    public int getElementNumber() {
        return elementNumber;
    }

    /** Get the TLE current date.
     * @return the epoch
     */
    public AbsoluteDate getDate() {
        return epoch;
    }

    /** Get the mean motion.
     * @return the mean motion (rad/s)
     */
    public double getMeanMotion() {
        return meanMotion;
    }

    /** Get the mean motion first derivative.
     * @return the mean motion first derivative (rad/s²)
     */
    public double getMeanMotionFirstDerivative() {
        return meanMotionFirstDerivative;
    }

    /** Get the mean motion second derivative.
     * @return the mean motion second derivative (rad/s³)
     */
    public double getMeanMotionSecondDerivative() {
        return meanMotionSecondDerivative;
    }

    /** Get the altitude.
     * @return the altitude
     */
    public double getAltitude() {
        double orbPeriod = 24*60*60/getMeanMotion(); // seconds
        double sma = FastMath.pow( (Utilities.GRAV_CONSTANT*(FastMath.pow(orbPeriod/(2* FastMath.PI),2))) , (1.0/3.0));
        return sma/1000-Utilities.EARTH_RADIUS_KM;
    }

    /** Get the eccentricity.
     * @return the eccentricity
     */
    public double getE() {
        return eccentricity;
    }

    /** Get the inclination.
     * @return the inclination (rad)
     */
    public double getI() {
        return inclination;
    }

    /** Get the argument of perigee.
     * @return omega (rad)
     */
    public double getPerigeeArgument() {
        return pa;
    }

    /** Get Right Ascension of the Ascending node.
     * @return the raan (rad)
     */
    public double getRaan() {
        return raan;
    }

    /** Get the mean anomaly.
     * @return the mean anomaly (rad)
     */
    public double getMeanAnomaly() {
        return meanAnomaly;
    }

    /** Get the revolution number.
     * @return the revolutionNumberAtEpoch
     */
    public int getRevolutionNumberAtEpoch() {
        return revolutionNumberAtEpoch;
    }

    /** Get the ballistic coefficient.
     * @return bStar
     */
    public double getBStar() {
        return bStar;
    }

    /** Compute the checksum of the first 68 characters of a line.
     * @param line line to check
     * @return checksum
     */
    private static int checksum(final CharSequence line) {
        int sum = 0;
        for (int j = 0; j < 68; j++) {
            final char c = line.charAt(j);
            if (Character.isDigit(c)) {
                sum += Character.digit(c, 10);
            } else if (c == '-') {
                ++sum;
            }
        }
        return sum % 10;
    }


    /** Check the lines format validity.
     * @param line1 the first element
     * @param line2 the second element
     * @return true if format is recognized (non null lines, 69 characters length,
     * line content), false if not
     */
    public static boolean isFormatOK(final String line1, final String line2) {

        if (line1 == null || line1.length() != 69 ||
                line2 == null || line2.length() != 69) {
            return false;
        }

        if (!(LINE_1_PATTERN.matcher(line1).matches() &&
                LINE_2_PATTERN.matcher(line2).matches())) {
            return false;
        }

        // check sums
        final int checksum1 = checksum(line1);
        if (Integer.parseInt(line1.substring(68)) != (checksum1 % 10)) {
            throw new IllegalArgumentException("check sum error");
        }

        final int checksum2 = checksum(line2);
        if (Integer.parseInt(line2.substring(68)) != (checksum2 % 10)) {
            throw new IllegalArgumentException("check sum error");
        }

        return true;

    }

    /** Check if this tle equals the provided tle.
     * <p>Due to the difference in precision between object and string
     * representations of TLE, it is possible for this method to return false
     * even if string representations returned by {@link #toString()}
     * are equal.</p>
     * @param o other tle
     * @return true if this tle equals the provided tle
     */
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TLESatellite)) {
            return false;
        }
        final TLESatellite tle = (TLESatellite) o;
        return satelliteNumber == tle.satelliteNumber &&
                classification == tle.classification &&
                launchYear == tle.launchYear &&
                launchNumber == tle.launchNumber &&
                Objects.equals(launchPiece, tle.launchPiece) &&
                ephemerisType == tle.ephemerisType &&
                elementNumber == tle.elementNumber &&
                Objects.equals(epoch, tle.epoch) &&
                meanMotion == tle.meanMotion &&
                meanMotionFirstDerivative == tle.meanMotionFirstDerivative &&
                meanMotionSecondDerivative == tle.meanMotionSecondDerivative &&
                eccentricity == tle.eccentricity &&
                inclination == tle.inclination &&
                pa == tle.pa &&
                raan == tle.raan &&
                meanAnomaly == tle.meanAnomaly &&
                revolutionNumberAtEpoch == tle.revolutionNumberAtEpoch &&
                bStar == tle.bStar;
    }

    /** Get a hashcode for this tle.
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(satelliteNumber,
                classification,
                launchYear,
                launchNumber,
                launchPiece,
                ephemerisType,
                elementNumber,
                epoch,
                meanMotion,
                meanMotionFirstDerivative,
                meanMotionSecondDerivative,
                eccentricity,
                inclination,
                pa,
                raan,
                meanAnomaly,
                revolutionNumberAtEpoch,
                bStar);
    }

}
