package no.npolar.common.eventcalendar;

import java.util.*;
//import java.sql.SQLException;
//import java.text.ParseException;
import java.text.SimpleDateFormat;
//import org.opencms.file.CmsDataAccessException;
//import org.opencms.file.CmsResource;
//import org.opencms.util.CmsUUID;
//import org.opencms.main.CmsException;
//import org.opencms.xml.A_CmsXmlDocument;
//import org.opencms.xml.content.*;
//import org.opencms.file.CmsObject;
//import org.opencms.file.CmsProperty;
//import org.opencms.file.CmsPropertyDefinition;
//import org.opencms.jsp.CmsJspXmlContentBean;
//import org.opencms.relations.CmsCategory;

/**
 * A range, described by a start point, end point and type.
 * 
 * @author Paul-Inge Flakstad <flakstad at npolar.no>
 */
public class CollectorTimeRange {
    /** Range type: unspecified. */
    public static final int RANGE_UNSPECIFIED = -1;
    /** Range type: specific year. */
    public static final int RANGE_YEAR = 0;
    /** Range type: specific month. */
    public static final int RANGE_MONTH = 1;
    /** Range type: specific date. */
    public static final int RANGE_DATE = 2;
    /**
     * Range type used to catch everything within a week.
     * @deprecated Supported dropped.
     */
    public static final int RANGE_WEEK = 3;
    /** Range type used to catch anything that's active, or will be in the future. */
    public static final int RANGE_UPCOMING_AND_IN_PROGRESS = 4;
    /** Range type used to catch anything that has expired. */
    public static final int RANGE_EXPIRED = 5;
    /** Range type used to catch <em>everything</em>. */
    public static final int RANGE_CATCH_ALL = 99;
    
    //public static final Date CATCH_ALL_ABSOLUTE_MINIMUM = new Date(Long.parseLong("-377648787600000")); // 9999-12-31 BC
    /** The default absolute minimum, 2000-01-01 AD 00:00:00, used by {@link #RANGE_CATCH_ALL} and {@link #RANGE_EXPIRED} range types. */
    public static final Date DEFAULT_ABS_MIN = new Date(new Long("946681200000")); // 2000-01-01 AD 00:00:00
    /** The default absolute maximum, 2099-12-31 AD 23:59:59, used by {@link #RANGE_CATCH_ALL} and {@link #RANGE_UPCOMING_AND_IN_PROGRESS} range types. */
    public static final Date DEFAULT_ABS_MAX = new Date(new Long("4102441199000")); // 2099-12-31 AD 23:59:59
    
    private static final String[] names = { 
        "UNSPECIFIED",
        "YEAR",
        "MONTH",
        "DATE",
        "WEEK",
        "UPCOMING_AND_IN_PROGRESS",
        "EXPIRED",
        "CATCH_ALL"
    };
    
    private long start = 0;
    private long end = 0;
    private int rangeType = RANGE_UNSPECIFIED;
    //private long min = DEFAULT_ABS_MIN.getTime();
    //private long max = DEFAULT_ABS_MAX.getTime();
    //private Date date = null;
    
    /**
     * Creates a new TimeRange instance, with the given start and stop values.
     * 
     * @param start the start value
     * @param end the end value
     */
    public CollectorTimeRange(int start, int end) {
        this.start = start;
        this.end = end;
    }
    
    /**
     * Creates a new range of the specified type.
     * <p>
     * The specific end points of the range is derived from given date, which 
     * must provide the necessary info/hint.
     * <p>
     * The date will indicate:
     * <ul>
     * <li>{@link #RANGE_CATCH_ALL}: start of time range (or pass <code>null</code> to use default, {@link #DEFAULT_ABS_MIN})</li>
     * <li>{@link #RANGE_EXPIRED}: end of time range</li>
     * <li>{@link #RANGE_UPCOMING_AND_IN_PROGRESS}: start date</li>
     * <li>{@link #RANGE_YEAR}: the range year</li>
     * <li>{@link #RANGE_MONTH}: the range month</li>
     * <li>{@link #RANGE_DATE}: the range date</li>
     * </ul>
     * For catch-all, a start date or null (= use default) can be passed.
     * 
     * @param rangeType The type.
     * @param date The date/timestamp to base this rangeType on. For catch-all, it indicates the start of the time range. For expired, it indicates the end.
     */
    public CollectorTimeRange(int rangeType, Date date) {
        
        Calendar c = new GregorianCalendar(TimeZone.getDefault());
        c.setTime(date);
        this.rangeType = rangeType;
        
        if (RANGE_UPCOMING_AND_IN_PROGRESS == this.rangeType) {
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 0, 0, 0);
            this.start = c.getTimeInMillis();
            this.end = DEFAULT_ABS_MAX.getTime();
        } else if (RANGE_EXPIRED == this.rangeType) {
            this.start = DEFAULT_ABS_MIN.getTime();
            this.end = date.getTime();
        } else if (RANGE_YEAR == this.rangeType) {
            c.set(Calendar.DATE, 1);
            c.set(Calendar.MONTH, Calendar.JANUARY);
            c.set(c.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
            this.start = c.getTimeInMillis();
            c.set(c.get(Calendar.YEAR), Calendar.DECEMBER, 31, 23, 59, 59);
            this.end = c.getTimeInMillis();
        } else if (RANGE_MONTH == this.rangeType) {
            c.set(Calendar.DATE, 1);
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1, 0, 0, 0);
            this.start = c.getTimeInMillis();
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.getActualMaximum(Calendar.DATE), 23, 59, 59);
            this.end = c.getTimeInMillis();
        } else if (RANGE_DATE == this.rangeType) {
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 0, 0, 0);
            this.start = c.getTimeInMillis();
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 23, 59, 59);
            this.end = c.getTimeInMillis();
        } else {
            // Default / RANGE_CATCH_ALL
            if (date != null)
                this.start = date.getTime();
            else
                this.start = DEFAULT_ABS_MIN.getTime();
            this.end = DEFAULT_ABS_MAX.getTime();
        }
    }
    
    /**
     * Sets the range's start time.
     * 
     * @param end The start time.
     * @return This instance, updated.
     */
    public CollectorTimeRange adjustStart(long start) {
        this.start = start;
        return this;
    }
    
    /**
     * Sets the range's end time.
     * 
     * @param end The end time.
     * @return This instance, updated.
     */
    public CollectorTimeRange adjustEnd(long end) {
        this.end = end;
        return this;
    }
    /*
    public CollectorTimeRange setMin(long min) {
        this.min = min;
        return this;
    }
    
    public CollectorTimeRange setMax(long max) {
        this.max = max;
        return this;
    }
    
    public long getMin() { return min; }
    public long getMax() { return max; }
    */
    
    /**
     * Gets the "catch all" range.
     * 
     * @return The "catch all" range.
     */
    public static CollectorTimeRange getCatchAllRange() {
        return new CollectorTimeRange(RANGE_CATCH_ALL, new Date());
    }
    
    /**
     * Determines whether or not the given date is included by this range.
     * 
     * @param d The date to check.
     * @return True if the given date is included by this range, false otherwise.
     */
    public boolean includes(Date d) {
        try {
            long m = d.getTime();
            return start <= m && end >= m;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets the range type.
     * 
     * @return The range type.
     */
    public int getRange() {
        return rangeType;
    }
    
    /**
     * Gets this range's start value, as a long.
     * 
     * @return this range's start value, as a long.
     */
    public long getStart() {
        return start;
    }
    
    /**
     * Gets this range's end value, as a long.
     * 
     * @return this range's end value, as a long.
     */
    public long getEnd() {
        return end;
    }
    
    /**
     * Returns the collector type name + the date span.
     * 
     * @return The collector type name + the date span.
     */
    @Override
    public String toString() {
        String s = "" + rangeType + "_";
        
        try {
            s += names[rangeType+1];
        } catch (ArrayIndexOutOfBoundsException e) {
            s += names[names.length-1];
        }
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        s += " [" + df.format(new Date(getStart())) + " - " + df.format(new Date(getEnd())) + "]";
        
        return s;
    }
    
    
    
    /*
     * Determines a rangeType based on the given time variables' existence and validity.
     * <p>
     * Example:
     * <ul>
     * <li>getRange(null, null, null, "2000") will return {@link #RANGE_YEAR}</li>
     * <li>getRange(null, null, "-1", "2000") will return {@link #RANGE_YEAR}</li>
     * <li>getRange(null, null, "0", "2010") will return {@link #RANGE_MONTH}</li>
     * <li>getRange("20", null, "9", "2010") will return {@link #RANGE_DATE}</li>
     * <li>getRange(null, "32", null, "2011") will return {@link #RANGE_WEEK}</li>
     * </ul>
     * 
     * @param date the date
     * @param week the week
     * @param month the month
     * @param year the year
     * @return one of the constant rangeType values, or -1 if no rangeType could be determined
     * 
     * @see #RANGE_DATE
     * @see #RANGE_WEEK
     * @see #RANGE_MONTH
     * @see #RANGE_YEAR
     *
    public static int getRange(String date, String week, String month, String year) {
        EventCalendar calendar = new EventCalendar();
        calendar.setTime(new Date()); 
        calendar.setFirstDayOfWeek(Calendar.MONDAY); // Set start of week to Monday
        int m = calendar.get(Calendar.MONTH);
        int y = calendar.get(Calendar.YEAR);
        int d = calendar.get(Calendar.DATE);
        int w = calendar.get(Calendar.WEEK_OF_YEAR);
        
        if (date != null && month != null && year != null) {
            d = Integer.valueOf(date);
            if (d < 1 && d <= calendar.getMaximum(Calendar.DATE))
                throw new IllegalArgumentException("Illegal number for date: " + d + ". " +
                        "Value must be in the rangeType 1-" + calendar.getMaximum(Calendar.DATE) + ".");
            m = Integer.valueOf(month);
            if (m < 0 || m > 11)
                throw new IllegalArgumentException("Illegal number for month: " + m + ". Value must be in the rangeType 0 to 11.");
            y = Integer.valueOf(year);
            if (y < 0)
                throw new IllegalArgumentException("Illegal number for year: " + y + ". Value cannot be negative.");
            return RANGE_DATE;
        }
        
        else if (month != null && year != null) {
            m = Integer.valueOf(month);
            if (m < -1 | m > 11)
                throw new IllegalArgumentException("Illegal number for month: " + m + ". Value must be in the rangeType -1 (all) to 11.");
            y = Integer.valueOf(year);
            if (y < 0)
                throw new IllegalArgumentException("Illegal number for year: " + y + ". Value cannot be negative.");
            if (m == -1) 
                return RANGE_YEAR;
            else
                return RANGE_MONTH;
        }
        
        else if (year != null && week != null) {
            y = Integer.valueOf(year);
            if (y < 0)
                throw new IllegalArgumentException("Illegal number for year: " + y + ". Value cannot be negative.");
            w = Integer.valueOf(week);
            if (w < 1 || w > calendar.getMaximum(Calendar.WEEK_OF_YEAR))
                throw new IllegalArgumentException("Illegal number for week: " + w + "." +
                        " Value must be in the rangeType 1-" + calendar.getMaximum(Calendar.WEEK_OF_YEAR) + ".");
            calendar.set(Calendar.YEAR, y);
            calendar.set(Calendar.WEEK_OF_YEAR, w);
            m = calendar.get(Calendar.MONTH);
            calendar.timeWarp(EventCalendar.WARP_FIRST_DAY_OF_WEEK);
            d = calendar.get(Calendar.DATE);
            return RANGE_WEEK;
            //out.println("<h4>Initially: " + testFormat.format(calendar.getTime()) + "</h4>");
        }
        
        else if (year != null) {
            y = Integer.valueOf(year);
            if (y < 0)
                throw new IllegalArgumentException("Illegal number for year: " + y + ". Value cannot be negative.");
            return RANGE_YEAR;
        }
        
        return -1;
    }*/
}
