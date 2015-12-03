/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package no.npolar.common.eventcalendar;
import java.util.*;
//import java.sql.SQLException;
//import java.text.ParseException;
import java.text.SimpleDateFormat;
//import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsResource;
//import org.opencms.util.CmsUUID;
import org.opencms.main.CmsException;
//import org.opencms.xml.A_CmsXmlDocument;
//import org.opencms.xml.content.*;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.jsp.CmsJspXmlContentBean;
import org.opencms.relations.CmsCategory;
/**
 * 
 * @author Paul-Inge Flakstad <flakstad at npolar.no>
 */
public class TimeRange {
    private long start = 0;
    private long end = 0;
    private Date date = null;
    
    public static final int RANGE_CURRENT_YEAR = 0;
    public static final int RANGE_CURRENT_MONTH = 1;
    public static final int RANGE_CURRENT_DATE = 2;
    public static final int RANGE_CURRENT_WEEK = 3;
    
    /**
     * Creates a new TimeRange instance, with the given start and stop values.
     * @param start the start value
     * @param end the end value
     */
    public TimeRange(int start, int end) {
        this.start = start;
        this.end = end;
    }
    
    /**
     * Gets this range's start value as a long.
     * @return this range's start value as a long
     */
    public long getStart() {
        return start;
    }
    
    /**
     * Gets this range's end value as a long.
     * @return this range's end value as a long
     */
    public long getEnd() {
        return end;
    }
    
    /**
     * Determines a range based on the given time variables' existence and validity.
     * @param date the date
     * @param week the week
     * @param month the month
     * @param year the year
     * @return one of the constant range values, or -1 if no range could be determined
     * @see #RANGE_CURRENT_DATE
     * @see #RANGE_CURRENT_WEEK
     * @see #RANGE_CURRENT_MONTH
     * @see #RANGE_CURRENT_YEAR
     */
    public static int getRange(String date, String week, String month, String year) {
        EventCalendar calendar = new EventCalendar();
        calendar.setTime(new Date()); 
        //calendar.setTime(defaultDate); // Set calendar time to beginning of 2011 - TEMPORARY
        calendar.setFirstDayOfWeek(Calendar.MONDAY); // Set start of week to Monday
        int m = calendar.get(Calendar.MONTH);
        int y = calendar.get(Calendar.YEAR);
        int d = calendar.get(Calendar.DATE);
        int w = calendar.get(Calendar.WEEK_OF_YEAR);
        
        if (date != null && month != null && year != null) {
            d = Integer.valueOf(d).intValue();
            if (d < 1 && d <= calendar.getMaximum(Calendar.DATE))
                throw new IllegalArgumentException("Illegal number for date: " + d + ". " +
                        "Value must be in the range 1-" + calendar.getMaximum(Calendar.DATE) + ".");
            m = Integer.valueOf(month).intValue();
            if (m < 0 || m > 11)
                throw new IllegalArgumentException("Illegal number for month: " + m + ". Value must be in the range 0 to 11.");
            y = Integer.valueOf(year).intValue();
            if (y < 0)
                throw new IllegalArgumentException("Illegal number for year: " + y + ". Value cannot be negative.");
            return RANGE_CURRENT_DATE;
        }
        
        else if (month != null && year != null) {
            m = Integer.valueOf(month).intValue();
            if (m < -1 | m > 11)
                throw new IllegalArgumentException("Illegal number for month: " + m + ". Value must be in the range -1 (all) to 11.");
            y = Integer.valueOf(year).intValue();
            if (y < 0)
                throw new IllegalArgumentException("Illegal number for year: " + y + ". Value cannot be negative.");
            if (m == -1) 
                return RANGE_CURRENT_YEAR;
            else
                return RANGE_CURRENT_MONTH;
        }
        
        else if (year != null && week != null) {
            y = Integer.valueOf(year).intValue();
            if (y < 0)
                throw new IllegalArgumentException("Illegal number for year: " + y + ". Value cannot be negative.");
            w = Integer.valueOf(week).intValue();
            if (w < 1 || w > calendar.getMaximum(Calendar.WEEK_OF_YEAR))
                throw new IllegalArgumentException("Illegal number for week: " + w + "." +
                        " Value must be in the range 1-" + calendar.getMaximum(Calendar.WEEK_OF_YEAR) + ".");
            calendar.set(Calendar.YEAR, y);
            calendar.set(Calendar.WEEK_OF_YEAR, w);
            m = calendar.get(Calendar.MONTH);
            calendar.timeWarp(EventCalendar.WARP_FIRST_DAY_OF_WEEK);
            d = calendar.get(Calendar.DATE);
            return RANGE_CURRENT_WEEK;
            //out.println("<h4>Initially: " + testFormat.format(calendar.getTime()) + "</h4>");
        }
        
        else if (year != null) {
            y = Integer.valueOf(year).intValue();
            if (y < 0)
                throw new IllegalArgumentException("Illegal number for year: " + y + ". Value cannot be negative.");
            return RANGE_CURRENT_YEAR;
        }
        
        return -1;
    }
}
