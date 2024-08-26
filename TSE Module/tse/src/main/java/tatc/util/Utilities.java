package tatc.util;

import org.hipparchus.util.FastMath;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Class containing static methods and constants for general use inside the tse
 */
public class Utilities {

    /**
     * Earth radius in km
     */
    public static final double EARTH_RADIUS_KM = 6378.14;
    /**
     * Earth gravitational constant in m^3/s^2
     */
    public static final double GRAV_CONSTANT = 3.986004418e14;

    /**
     * Converts True Longitude Time of the Ascending to Right Ascension of the Ascending Node in degrees
     * @param trueLSTAN_secs True Longitude Time of the Ascending Node in seconds
     * @param JD_UTC Julian date in seconds
     * @return the RAAN in degrees
     */
    public static double trueLSTANToRAAN(double trueLSTAN_secs, double JD_UTC){
        //Calculate time in Terristrial Time (JD TT)
        //http://stjarnhimlen.se/comp/time.html
        double JD_TT = JD_UTC + 69.2/(60.0*60.0*24.0);

        //Calculate mean position of Sun
        //reference: https://en.wikipedia.org/wiki/Position_of_the_Sun, http://aa.usno.navy.mil/faq/docs/SunApprox.php
        double n = JD_TT - 2451545.0; //  n, the number of days (positive or negative) since Greenwich noon, Terrestrial Time, on 1 January 2000 (J2000.0).

        double L_deg = 280.460+0.98564736*n; // mean longitude

        double g_deg = 357.528+0.98560028*n; // mean anomaly

        double lambda_deg =L_deg+1.915* FastMath.sin(FastMath.toRadians(g_deg)) +0.020*FastMath.sin(2*FastMath.toRadians(g_deg));

        double eps_deg = 23.439-0.0000004*n;

        double RA_deg = FastMath.toDegrees(FastMath.atan2(FastMath.cos(FastMath.toRadians(eps_deg))*FastMath.sin(FastMath.toRadians(lambda_deg)), FastMath.cos(FastMath.toRadians(lambda_deg))));

        //reference: Earth Observation Mission CFI SOftware Concentions Document 29/10/2015 EO-MA-DMS-GS-0001
        double RAAN_deg = trueLSTAN_secs * (360.0/(24.0*60*60)) + RA_deg - 180.0;

        return (RAAN_deg%360); // return in degrees
    }


    /**
     * Method to convert a date to Julian Day.
     * Algorithm from 'Practical Astronomy with your Calculator or Spreadsheet', 4th ed., Duffet-Smith and Zwart, 2011.
     * Credits: https://gist.github.com/jiffyclub/1294443
     * @param year the year as integer. Years preceding 1 A.D. should be 0 or negative. The year before 1 A.D. is 0,
     *             10 B.C. is year -9.
     * @param month the month as integer, Jan = 1, Feb. = 2, etc.
     * @param day the day, may contain fractional part.
     * @return the julian day
     */
    public static double dateToJD(int year, int month, double day){
        int yearp;
        int monthp;
        if (month == 1 || month == 2){
            yearp = year - 1;
            monthp = month + 12;
        }else{
            yearp = year;
            monthp = month;
        }

        //this checks where we are in relation to October 15, 1582, the beginning of the Gregorian calendar.
        double B;
        if ((year < 1582) || (year == 1582 && month < 10) || (year == 1582 && month == 10 && day < 15)){
            // before start of Gregorian calendar
            B = 0;
        }else{
            // after start of Gregorian calendar
            double A = Utilities.trunc(yearp / 100.);
            B = 2 - A + Utilities.trunc(A / 4.);
        }

        double C;
        if (yearp < 0){
            C = Utilities.trunc((365.25 * yearp) - 0.75);
        } else {
            C = Utilities.trunc(365.25 * yearp);
        }

        double D = Utilities.trunc(30.6001 * (monthp + 1));

        double jd = B + C + D + day + 1720994.5;

        return jd;

    }

    /**
     * This method converts time in seconds to HH:mm:ss format
     * @param seconds the time in seconds
     * @return the time in string HH:mm:ss (24 hour format)
     */
    public static String secondsToHHmmss(double seconds) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(tz);
        return df.format(new Date((long) (seconds*1000L)));
    }


    /**
     * Method to convert hh:mm:ss to decimal seconds
     * @param hhmmss the hh:mm:ss string
     * @return the conversion of the given string to seconds
     */
    public static double hhmmssToSeconds(String hhmmss){
        String split[]  = hhmmss.split(":");
        return Integer.valueOf(split[0]) * 3600 + Integer.valueOf(split[1]) * 60 + Integer.valueOf(split[2]);
    }

    /**
     * Method to convert a string containing a duration in ISO 8601 format to seconds
     * @param duration the string containing a duration in ISO 8601 format
     * @return the conversion of the given duration to seconds
     */
    public static double DurationToSeconds(String duration){
        Duration d =Duration.parse(duration);
        return d.getSeconds();
    }

    /**
     * Method to convert a string containing a date in ISO 8601 format to year, month, day, hour, minute and second
     * @param DateTime the string containing a date in ISO 8601 format
     * @return the absolute date object containing the year, month, day, hour, minute and second
     */
    public static AbsoluteDate DateTimeToAbsoluteDate(String DateTime){
        LocalDateTime date = LocalDateTime.parse(DateTime.substring(0,DateTime.length() - 1));
        return new AbsoluteDate(date.getYear(),date.getMonthValue(),date.getDayOfMonth(),date.getHour(),date.getMinute(),date.getSecond());
    }


    /**
     * Method that truncates a given number. Truncation of positive and negative real numbers can be done using the
     * floor and ceil functions, respectively.
     * @param number the number to truncate
     * @return the truncated number
     */
    public static double trunc(double number){
        if (number==0.0){
            return 0.0;
        }else if (number<0.0){
            return FastMath.ceil(number);
        }else{
            return FastMath.floor(number);
        }
    }

    /**
     * Method that obtains the inclination for a circular Sun Synchronous Orbit given its altitude in m
     * @param h the altitude in m
     * @return the inclination in radians
     */
    public static double incSSO(double h) {
        double kh = 10.10949D;
        double cosi = Math.pow((6378137.0D + h) / 6378137.0D, 3.5D) / -kh;
        return FastMath.acos(cosi);
    }

    /**
     * Method that obtains the inclination for an elliptic Sun Synchronous Orbit given its altitude in m
     * and eccentricity
     * @param h the altitude in m
     * @param e the eccentricity
     * @return the inclination in radians
     */
    public static double incSSO(double h, double e) {
        double RE_km = 6378.137;
        double h_km = h / 1000.0;
        double cosi = (360.0/365.25)/(-2.06474e14)/Math.pow(h_km+RE_km,-3.5)/Math.pow(1-Math.pow(e,2),-2);
        return FastMath.acos(cosi);
    }

    /**
     * Method that maps a real value from 0 to 1 to a particular value from a list of ordered values.
     * Examples:
     * realValue=0.3 and list =[1,3], this method would return 1
     * realValue=0.9 and list =["hello","how","are","you","?"], this method would return "?"
     * realValue=0.5 and list =[1,2,3], this method would return 2
     * @param list the list of ordered values
     * @param realValue the real number from 0 to 1
     * @param <T> the type of objects inside the list
     * @return the mapped value
     */
    public static <T> T obtainValueFromListAndRealValue(ArrayList<T> list, double realValue){
        T value=null;
        double step = 1.0/list.size();
        int index=0;
        for (double i=1.0/list.size(); i<=1; i = i+step){
            if (realValue<=i){
                value = list.get(index);
                break;
            }else{
                index++;
            }
        }
        return value;
    }

    /**
     * Method that returns a list of n unique integers from 0 to N-1. If randomize = true, the returned list is random.
     * If randomize = false, the returned list of integers contains all the values from 0 to n-1.
     * @param n the number of integers to be returned
     * @param N the upper bound of the integers to be returned
     * @param randomize the randomization flag
     * @return list of n unique integers from 0 to N-1. The list will be random if randomize = true.
     */
    public static ArrayList<Integer> uniqueRandomIntegers(int n, int N, boolean randomize){
        if (n>N){
            throw new IllegalArgumentException("N has to be greater than n");
        }
        ArrayList<Integer> list = new ArrayList<>();
        ArrayList<Integer> listToReturn = new ArrayList<>();
        for (int i=0; i<N; i++) {
            list.add(i);
        }
        if (randomize){
            Collections.shuffle(list);
        }
        for (int i=0; i<n; i++) {
            listToReturn.add(list.get(i));
        }
        return listToReturn;
    }


    /**
     * Given a list of possible planes and two real values from 0 to 1 that contain information about the number of
     * planes (p) and relative spacing (f) of a walker constellation returns an array list of size 2 containing the
     * number of planes (p) and relative spacing (f) of the walker constellation.
     * First maps the plane real value to the list of possible planes (for instance, if possiblePlanes=[2,4], p=2 if
     * pReal<0.5 and p=4 if pReal>0.5).
     * Once "p" is selected, the possible relative spacing (f) values are all the integers from 0 to (p-1). Then phaseReal
     * is used to select the final f, following the same fashion.
     * @param nsatHomo the number of satellites in the constellation
     * @param tradespacePlanes the list of possible planes for a homogeneous walker constellation defined in the TSR
     * @param planeReal the real values from 0 to 1 that contains information about the number of planes (p)
     * @param phaseReal the real values from 0 to 1 that contains information about the relative spacing (f)
     * @return an array list containing the number of planes(p) and relative spacing(f)
     */
    public static ArrayList<Integer> obtainPlanesAndPhasingFromChromosome(int nsatHomo, List<Integer> tradespacePlanes, double planeReal, double phaseReal){
        List<Integer> divisorPlanes = Factor.divisors(nsatHomo);
        List<Integer> possiblePlanes;
        if (tradespacePlanes != null) {
            possiblePlanes = Combinatorics.intersection(divisorPlanes, tradespacePlanes);
        } else {
            possiblePlanes = divisorPlanes;
        }
        //need to convert the real value that's between [0,1] to the number of planes.
        HashMap<Double, Integer> mappedPlanes = new HashMap<>();

        int counterPlanes = 0; //counter for possible planes index

        for (double i = 1. / (2*possiblePlanes.size()); i <= 1; i += 1. / possiblePlanes.size()) {
            //for (double i = 0; i <= 1; i += 1. / (possiblePlanes.size() - 1)) {
            mappedPlanes.put(i, possiblePlanes.get(counterPlanes));
            counterPlanes = counterPlanes + 1;
        }

        //read in the real value of planes from the solution
        int p = -1;
        double minDistancePlanes = Double.POSITIVE_INFINITY;
        Iterator<Double> iter1 = mappedPlanes.keySet().iterator();
        while (iter1.hasNext()) {
            double val = iter1.next();
            if (Math.abs(planeReal - val) < minDistancePlanes) {
                minDistancePlanes = Math.abs(planeReal - val);
                p = mappedPlanes.get(val);
            }
        }

        if (p == -1) {
            throw new IllegalStateException("Error in number of planes p = -1");
        }

        //The available number of phases is listed below
        //copying available number of planes to phases array
        //then we subtract -1 from each value to get the number of phases
        List<Integer> possiblePhases = new ArrayList<>();

        for (int i = 0; i < p; i++) {
            possiblePhases.add(i);
        }

        HashMap<Double, Integer> mappedPhases = new HashMap<>();

        int counterPhases = 0; //counter for possible planes index

        for (double i = 1. / (2*possiblePhases.size()); i <= 1; i += 1. / possiblePhases.size()) {
            //for (double i = 0; i <= 1; i += 1. / (possiblePhases.size() - 1)) {
            mappedPhases.put(i, possiblePhases.get(counterPhases));
            counterPhases = counterPhases + 1;
        }

        //read in the real value of phases from the solution
        int f = -1;
        double minDistancePhases = Double.POSITIVE_INFINITY;
        Iterator<Double> iter = mappedPhases.keySet().iterator();
        while (iter.hasNext()) {
            double val = iter.next();
            if (Math.abs(phaseReal - val) < minDistancePhases) {
                minDistancePhases = Math.abs(phaseReal - val);
                f = mappedPhases.get(val);
            }
        }

        if (f == -1) {
            throw new IllegalStateException("Error in number of phases q = -1");
        }

        ArrayList<Integer> planeAndPhase = new ArrayList<>();
        planeAndPhase.add(p);
        planeAndPhase.add(f);
        return planeAndPhase;
    }
}
