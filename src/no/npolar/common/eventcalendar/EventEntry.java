package no.npolar.common.eventcalendar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
//import org.opencms.file.CmsObject;
//import org.opencms.file.CmsResource;
//import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
//import org.opencms.xml.I_CmsXmlDocument;
//import org.opencms.xml.content.CmsXmlContentFactory;
//import org.opencms.xml.types.I_CmsXmlContentValue;

/**
 * A calendar event.
 * @author Paul-Inge Flakstad
 */
public class EventEntry implements Comparable {
    
    /**
    private class StartDateComparator implements Comparator {
        public int compare(Object thisObj, Object thatObj) {
            if (((EventEntry)thisObj).getStartTime() < ((EventEntry)thatObj).getStartTime())
                return -1;
            else if (((EventEntry)thisObj).getStartTime() > ((EventEntry)thatObj).getStartTime())
                return 1;
            return 0;
        }
    }
    */
    /** The event's start time. */
    private long start;
    /** The event's end time. */
    private long stop;
    /** The event's title. */
    private String title;
    /** The event's description. */
    private String description;
    /** The event's location. */
    private String location;
    /** The resource ID of the OpenCms resource represented by this instance. */
    private CmsUUID resourceId;
    /** The structure ID of the OpenCms structure represented by this instance. */
    private CmsUUID structureId;
    /** The locale used by this instance. */
    private Locale locale;
    /** The HTML code for this instance. */
    private String html = null;
    /** The date display mode, one of EventEntry#TIME_DISPLAY_DATEONLY or EventEntry#TIME_DISPLAY_DATETIME */
    private String timeDisplay = null;
    /** The date format used when formatting (date)timestamps. */
    private SimpleDateFormat sdf = null;
    //private StartDateComparator startDateComparator = null;
    /** The event resource type name. */
    public static final String RESOURCE_TYPE_NAME_EVENT = "np_event";
    /** The property used for the start time. */
    public static final String PROPERTY_TIME_START = "collector.date";
    /** The property used for the end time. */
    public static final String PROPERTY_TIME_END = "collector.time";
    /** The property used for the categories. (Multiple pipe-separated values can be assigned.) */
    public static final String PROPERTY_CATEGORIES = "collector.categories";
    /** The property used for the display option. */
    public static final String PROPERTY_TIME_DISPLAY = "display";
    /** The keyword for display option: display only the date. */
    public static final String TIME_DISPLAY_DATEONLY = "dateonly";
    /** The keyword for display option: display the date and the time. */
    public static final String TIME_DISPLAY_DATETIME = "datetime";
    /** Comparator used to compare the title of EventEntry instances, e.g. when sorting lists. */
    public static final Comparator<EventEntry> COMPARATOR_TITLE =
            new Comparator<EventEntry>() {
                public int compare(EventEntry e1, EventEntry e2) {
                    return e1.getTitle().compareTo(e2.getTitle());
                }
            };
    /** Comparator used to compare the start time of EventEntry instances, e.g. when sorting lists. */
    public static final Comparator<EventEntry> COMPARATOR_START_TIME = 
            new Comparator<EventEntry>() {
                public int compare(EventEntry e1, EventEntry e2) {
                    if (e1.getStartTime() < e2.getStartTime())
                        return -1;
                    else if (e1.getStartTime() > e2.getStartTime())
                        return 1;
                    return 0;
                }
            };
    /** This object's hash code, used in the override of java.lang.Object's hashCode() method. */
    private volatile int hashCode = 0;
    
    /**
     * Creates a new EventEntry with start and stop set to zero.<p>
     */
    public EventEntry() {
        this.start = 0;
        this.stop = 0;
        this.title = null;
        this.description = null;
        this.resourceId = null;
        this.structureId = null;
        this.locale = null;
        this.timeDisplay = TIME_DISPLAY_DATETIME;
    }
    
    /**
     * Creates a new EventEntry with the given values.<p>
     * 
     * @param start the start time
     * @param stop the end time
     * @param title the title
     * @param description the description
     * @param resourceId the resource ID
     * @param structureId the structure ID
     * @param locale the locale
     */
    public EventEntry(long start, long stop, String title, String description, CmsUUID resourceId, CmsUUID structureId, Locale locale) {
        this.start = start;
        this.stop = stop;
        this.title = title;
        this.description = description;
        this.resourceId = resourceId;
        this.structureId = structureId;
        this.locale = locale;
        this.timeDisplay = TIME_DISPLAY_DATETIME;
    }
    
    public EventEntry(CmsObject cmso, CmsResource eventResource) throws CmsException {
        this.title = cmso.readPropertyObject(eventResource, "Title", false).getValue("");
        this.start = Long.valueOf(cmso.readPropertyObject(eventResource, PROPERTY_TIME_START, false).getValue("0"));
        this.stop = Long.valueOf(cmso.readPropertyObject(eventResource, PROPERTY_TIME_END, false).getValue("0"));
        this.description = cmso.readPropertyObject(eventResource, "Description", false).getValue("");
        this.timeDisplay = cmso.readPropertyObject(eventResource, PROPERTY_TIME_DISPLAY, false).getValue(TIME_DISPLAY_DATETIME);
        this.locale = cmso.getRequestContext().getLocale();
        this.resourceId = eventResource.getResourceId();
        this.structureId = eventResource.getStructureId();
    }
    
    /**
     * Copy constructor.<p>
     * 
     * @param other the EventEntry to copy
     */
    public EventEntry(EventEntry other) {
        this.start = other.start;
        this.stop = other.stop;
        this.title = other.title;
        this.description = other.description;
        this.resourceId = other.resourceId;
        this.structureId = other.structureId;
        this.locale = other.locale;
        this.html = other.html;
        this.sdf = other.sdf;
        this.timeDisplay = other.timeDisplay;
    }
    /**
     * Gets the event's start time as a String of the configured format.<p>
     * 
     * @see #setDateFormat(java.text.SimpleDateFormat) 
     * @see #setDateFormat(java.lang.String) 
     * 
     * @return The event's start time as a String of the configured format.
     */
    public String formatStartTime() { return sdf.format(new Date(this.getStartTime())); }
    /**
     * Gets the event's end time as a String of the configured format.<p>
     * 
     * @see #setDateFormat(java.text.SimpleDateFormat) 
     * @see #setDateFormat(java.lang.String) 
     * 
     * @return The event's end time, in the configured format.
     */
    public String formatEndTime() { return this.hasEndTime() ? sdf.format(new Date(this.getEndTime())) : null; }
    
    /**
     * Determines if the event has a defined end time.<p>
     * 
     * @return true if the event has a defined end time, false if not.
     */
    public boolean hasEndTime() { return this.stop > 0; }
    
    /** 
     * Gets the event's start time.<p>
     * 
     * @return The event's start time.
     */
    public long getStartTime() { 
        if (this.isDisplayDateOnly()){
            return getStartOfDay(this.start);
        }
        else 
            return this.start; 
    }
    
    /**
     * Gets the event's end time.<p>
     * 
     * @return The event's end time.
     */
    public long getEndTime() {
        if (this.isDisplayDateOnly()){
            return getEndOfDay(this.stop);
        }
        else 
            return this.stop; 
    }
    
    /**
     * Helper method for getting the timestamp for the end of the day represented 
     * by the given timestamp.
     * @param timeInMillis The timestamp to evaluate.
     * @return The end of the day represented by the given timestamp.
     */
    public static long getEndOfDay(long timeInMillis) {
        Calendar temp = new GregorianCalendar();
        temp.setTimeInMillis(timeInMillis);
        temp.set(Calendar.HOUR_OF_DAY, temp.getActualMaximum(Calendar.HOUR_OF_DAY));
        temp.set(Calendar.MINUTE, temp.getActualMaximum(Calendar.MINUTE));
        temp.set(Calendar.SECOND, temp.getActualMaximum(Calendar.SECOND));
        return temp.getTimeInMillis();
    }
    
    /**
     * Helper method for getting the timestamp for the start of the day represented 
     * by the given timestamp.
     * @param timeInMillis The timestamp to evaluate.
     * @return The start of the day represented by the given timestamp.
     */
    public static long getStartOfDay(long timeInMillis) {
        Calendar temp = new GregorianCalendar();
        temp.setTimeInMillis(timeInMillis);
        temp.set(Calendar.HOUR_OF_DAY, temp.getActualMinimum(Calendar.HOUR_OF_DAY));
        temp.set(Calendar.MINUTE, temp.getActualMinimum(Calendar.MINUTE));
        temp.set(Calendar.SECOND, temp.getActualMinimum(Calendar.SECOND));
        return temp.getTimeInMillis();
    }
    
    
    /**
     * Gets this event's time display mode keyword, one of 
     * EventEntry#TIME_DISPLAY_DATETIME and EventEntry#TIME_DISPLAY_DATEONLY.
     * @return This event's time display mode keyword.
     */
    public String getTimeDisplayMode() { return this.timeDisplay; }
    
    /** 
     * Determines if the event starts and ends on the same day.<p>
     * 
     * @return true if the event has no end time, or if the end date is also the start date.
     */
    public boolean isOneDayEvent() {        
        if (!this.hasEndTime())
            return true; // No end time, assume this is one-day event
       
        // Create two calendars, for start and stop
        Calendar startDay = new GregorianCalendar();
        startDay.setTimeInMillis(this.getStartTime());
       
        Calendar endDay = new GregorianCalendar();
        endDay.setTimeInMillis(this.getEndTime());
        
        // Compare the year, month and date of the two calendars, return the result
        return (startDay.get(Calendar.YEAR) == endDay.get(Calendar.YEAR) &&
                startDay.get(Calendar.MONTH) == endDay.get(Calendar.MONTH) &&
                startDay.get(Calendar.DATE) == endDay.get(Calendar.DATE));
    }
    
    /**
     * Determines if the event has taken place in the past (compared to the 
     * reference time contained in the given date) and is over.<p>
     * 
     * @param d The date to use as a reference time
     * 
     * @return true if the event has taken place in the past and is over, false if not.
     */
    public boolean isExpired(Date d) {
        if (this.hasEndTime()) {
            return this.getEndTime() < d.getTime();
        }
        else if (this.isDisplayDateOnly() && this.startsOnDate(d)) { // Case is "No end time" + "display date only" + event taking place on the given date
            return false; // Not expired - the event is a one-day event taking place on the (entire) given timestamp
            // Move on to the default return statement
        }
        // Compare the start time with the given date: If this event's start time 
        // precedes the given timestamp, the event is expired. If not, it is not expired.
        return this.getStartTime() < d.getTime();
    }
    
    /**
     * Determines if this event is set to display only the date (and not the time).
     * 
     * @return True if this event is set to display date only, false if not.
     */
    public boolean isDisplayDateOnly() { return TIME_DISPLAY_DATEONLY.equals(timeDisplay); }
    
    /**
     * Determines if the event has taken place in the past (compared to "now") and is over.<p>
     * 
     * @return true if the event has taken place in the past and is over, false if not.
     */
    public boolean isExpired() {
        return this.isExpired(new Date());
    }
    
    /**
     * Determines if the event starts on a given date.<p>
     * 
     * @param d The date.
     * 
     * @return True if the event starts on the given date, false if not.
     */
    public boolean startsOnDate(Date d) {
        Calendar dateCalendar = new GregorianCalendar();
        dateCalendar.setTime(d);
        Calendar eventCalendar = new GregorianCalendar();
        eventCalendar.setTimeInMillis(this.getStartTime());
        
        return dateCalendar.get(Calendar.YEAR) == eventCalendar.get(Calendar.YEAR) &&
                dateCalendar.get(Calendar.MONTH) == eventCalendar.get(Calendar.MONTH) &&
                dateCalendar.get(Calendar.DATE) == eventCalendar.get(Calendar.DATE);
    }
    
    /**
     * Determines if the event ends on a given date.<p>
     * 
     * @param d The date.
     * 
     * @return 0 if the event ends on the given date, 1 if the event ends after the given date, -1 if the event ends prior to the given date.
     */
    public boolean endsOnDate(Date d) {
        Calendar dateCalendar = new GregorianCalendar();
        dateCalendar.setTime(d);
        Calendar eventCalendar = new GregorianCalendar();
        eventCalendar.setTimeInMillis(this.hasEndTime() ? this.getEndTime() : this.getStartTime());
        
        return dateCalendar.get(Calendar.YEAR) == eventCalendar.get(Calendar.YEAR) &&
                dateCalendar.get(Calendar.MONTH) == eventCalendar.get(Calendar.MONTH) &&
                dateCalendar.get(Calendar.DATE) == eventCalendar.get(Calendar.DATE);
    }
    
    /**
     * Determines if the event starts in a given range. Start and end time values 
     * are inclusive (i.e. if event.getStartTime() == rangeStart, 'true' is returned).<p>
     * 
     * @param rangeStart The long representation of the start time for the range.
     * @param rangeStop The long representation of the end time for the range.
     * 
     * @return True if the event starts in the given range, false if not.
     */
    public boolean startsInRange(long rangeStart, long rangeStop) {
        return this.getStartTime() >= rangeStart && this.getStartTime() <= rangeStop;
    }
    
    public boolean overlapsRange(long rangeStart, long rangeStop) {
        if (!this.hasEndTime()) { // No end time set
            return (this.startsInRange(rangeStart, rangeStop)); // Just evaluate start time
        } else { // End time set
            if (this.getEndTime() < rangeStart  // If this event ends before the range start ...
                            || this.getStartTime() > rangeStop) // ... OR starts after the range end ...
                return false; // ... it does not overlap
        }
        return true; // It must overlap! :)
    }
    
    /**
     * Compares the event's end time to a given date (an _entrire_ date, not just a timestamp).<p>
     * 
     * @param d The date. It will represent that whole date from beginning to end.
     * 
     * @return 1 if the event ends after the given date, 0 if the event ends on the given date, -1 if the event ends prior to the given date.
     */
    public int compareEndTime(Date d) {
        if (this.endsOnDate(d))
            return 0;
        
        Calendar dateCalendar = new GregorianCalendar();
        dateCalendar.setTime(d);
        dateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        dateCalendar.set(Calendar.MINUTE, 0);
        dateCalendar.set(Calendar.SECOND, 0);
        long dStart = dateCalendar.getTimeInMillis();
        /*dateCalendar.set(Calendar.HOUR_OF_DAY, 23);
        dateCalendar.set(Calendar.MINUTE, 59);
        dateCalendar.set(Calendar.SECOND, 59);
        long dEnd = dateCalendar.getTimeInMillis();*/
        
        if (this.endsBefore(dStart))
            return -1;
        return 1;
    }
    
    /**
     * Compares the event's start time to a given date (an _entire_ date, not just a timestamp).<p>
     * 
     * @param d The date. It will represent that whole date from beginning to end.
     * 
     * @return 1 if the event starts after the given date, 0 if the event starts on the given date, -1 if the event starts prior to the given date.
     */
    public int compareStartTime(Date d) {
        if (this.startsOnDate(d))
            return 0;
        
        Calendar dateCalendar = new GregorianCalendar();
        dateCalendar.setTime(d);
        dateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        dateCalendar.set(Calendar.MINUTE, 0);
        dateCalendar.set(Calendar.SECOND, 0);
        long dStart = dateCalendar.getTimeInMillis();
        /*dateCalendar.set(Calendar.HOUR_OF_DAY, 23);
        dateCalendar.set(Calendar.MINUTE, 59);
        dateCalendar.set(Calendar.SECOND, 59);
        long dEnd = dateCalendar.getTimeInMillis();*/
        
        if (this.getStartTime() < dStart)
            return -1;
        return 1;
    }
    
    /** Determines if this event starts before a given time. The comparison is done using the long representation of the dates. */
    public boolean startsBefore(Date d) { return d.getTime() > this.getStartTime(); }
    /** Determines if this event ends after a given time. The long representation of the dates are compared. */
    public boolean endsAfter(Date d) { return d.getTime() < this.getEndTime(); }
    /** Determines if this event ends before a given time. */
    public boolean endsBefore(long when) { return when > this.getEndTime(); }
    /** Determines if this event starts after a given time. */
    public boolean startsAfter(long when) { return when < this.getStartTime(); }
    /** Gets the event's title. */
    public String getTitle() { return this.title; }
    /** Gets the event's description. */
    public String getDescription() { return this.description; }
    /** Gets the event's structure ID. */
    public CmsUUID getStructureId() { return this.structureId; }
    /** Gets the event's resource ID. */
    public CmsUUID getResourceId() { return this.resourceId; }
    /** Gets the event's locale. */
    public Locale getLocale() { return this.locale; }
    /** Gets the event's HTML code. */
    public String getHtml() { return this.html; }
    /** 
     * Gets the event's URI, relative to the site - e.g. "/my-events/the-event.html". 
     * @param cmso An initialized CmsObject, used to read this event's corresponding resource from the OpenCms VFS.
     * @throws CmsException If anything goes wrong.
     */
    public String getUri(CmsObject cmso) throws CmsException { return cmso.getSitePath(cmso.readResource(this.getStructureId())); }
    
    /**
     * Gets the event's time span, as ready-to-use HTML.
     * @return 
     */
    public String getTimespanHtml(CmsJspActionElement cms) throws CmsException {
        SimpleDateFormat datetime = new SimpleDateFormat(cms.label("label.event.dateformat.datetime"), locale);
        SimpleDateFormat dateonly = new SimpleDateFormat(cms.label("label.event.dateformat.dateonly"), locale);
        SimpleDateFormat timeonly = new SimpleDateFormat(cms.label("label.event.dateformat.timeonly"), locale);
        SimpleDateFormat dfIso = getDatetimeAttributeFormat(locale);
        
        // Select date format
        SimpleDateFormat df = isDisplayDateOnly() ? dateonly : datetime;
        
        String begins = null;
        String ends = null;
        String beginsIso = null;
        String endsIso = null;
        try {
            begins = df.format(new Date(getStartTime())).replaceAll("\\s", "&nbsp;");
            beginsIso = dfIso.format(new Date(getStartTime()));
            // If there is an end-time
            if (this.hasEndTime()) {
                if (this.isOneDayEvent())
                    // End time is on the same day as begin time, format only the hour/minute
                    ends = timeonly.format(new Date(getEndTime())).replaceAll("\\s", "&nbsp;");
                else
                    ends = df.format(new Date(getEndTime())).replaceAll("\\s", "&nbsp;");
                
                endsIso = dfIso.format(new Date(getEndTime()));
            }
        } catch (NumberFormatException nfe) {
            // Keep ends=null
        }
        
        String s = "";
        
        s += "<time itemprop=\"startDate\" datetime=\"" + beginsIso + "\">" + begins + "</time>";
        if (ends != null)
            s += " &ndash; <time itemprop=\"endDate\" datetime=\"" + endsIso + "\">" + ends + "</time>";
        
        return s;
    }
    
    /**
     * Gets the appropriate date format for an event's "datetime" attribute (google "rich snippets" for more info). 
     * The format is determined by evaluating the event's "time display" mode (date only or date and time).
     * 
     * @param locale The locale to use in the returned date format.
     * 
     * @return SimpleDateFormat The appropriate format for the datetime attribute.
     */
    public SimpleDateFormat getDatetimeAttributeFormat(Locale locale) throws CmsException {
        SimpleDateFormat dfFullIso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", locale);
        SimpleDateFormat dfShortIso = new SimpleDateFormat("yyyy-MM-dd", locale);

        // Examine if the event is "display date only", and select the appropriate date format to use for the "datetime" attribute
        SimpleDateFormat dfIso = this.isDisplayDateOnly() ? dfShortIso : dfFullIso;
        dfIso.setTimeZone(TimeZone.getTimeZone("GMT+1"));

        return dfIso;
    }
    
    /**
     * Sets the date format pattern.<p>
     * 
     * @param pattern the date pattern
     * 
     * @see java.text.SimpleDateFormat
     */
    public void setDateFormat(String pattern) {
        this.sdf = new SimpleDateFormat(pattern);
    }
    
    /**
     * Sets the date format.<p>
     * 
     * @param sdf the date format
     * 
     * @see java.text.SimpleDateFormat
     */
    public void setDateFormat(SimpleDateFormat sdf) {
        this.sdf = sdf;
    }
    
    /**
     * Sets the HTML code content.<p>
     * 
     * @param html the HTML code content
     */
    public void setHtml(String html) {
        this.html = html;
    }
    
    /**
     * Sets the time display mode for this event. The given keyword must be one of
     * <ul>
     *  <li>EventEntry#TIME_DISPLAY_DATEONLY</li>
     *  <li>EventEntry#TIME_DISPLAY_DATETIME</li>
     * </ul>
     * Defaults to EventEntry#TIME_DISPLAY_DATETIME. Any value passed other than
     * EventEntry#TIME_DISPLAY_DATEONLY will default to this.
     * 
     * @param displayMode The display mode keyword for this event.
     * 
     * @return This event object instance.
     */
    public EventEntry setTimeDisplayMode(String displayMode) {
        if (TIME_DISPLAY_DATEONLY.equals(displayMode)) {
            this.timeDisplay = TIME_DISPLAY_DATEONLY;
        }
        else
            this.timeDisplay = TIME_DISPLAY_DATETIME;
        return this;
    }
    
    /**
     * Compares two EventEntry objects by evaluating their start time, end time 
     * and title.<p>
     * 
     * @param that another EventEntry object.
     * 
     * @return -1 if the other EventEntry starts after this, 1 if the opposite, 0 if the two have identical start times.
     */
    public int compareTo(Object that) {
        if (this.start < ((EventEntry)that).start)
            return -1;
        else if (this.start > ((EventEntry)that).start)
            return 1;
        else { // Same start date
            if (this.stop == ((EventEntry)that).stop && this.title.equals(((EventEntry)that).title)) // Same end time & title, assume identical
                return 0;
            else 
                return (this.title.compareTo(((EventEntry)that).title)); // Not same end time and/or title, return title comparison
        }
    }
    
    /**
     * Override of java.lang.Object's equals(Object), implemented to facilitate 
     * equality between otherwise equal objects. Equality is determined by looking at 
     * start time, end time and title. Two events with identical values for 
     * these attributes are considered equal.<p>
     * 
     * @param that The EventEntry object to compare with.
     * 
     * @return true if the objects are considered equal, false if not.
     */
    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null) 
            return false;
        if (!(that instanceof EventEntry))
            return false;
        if (this.start < ((EventEntry)that).start)
            return false;
        else if (this.start > ((EventEntry)that).start)
            return false;
        else { // Same start date
            if (this.stop == ((EventEntry)that).stop && this.title.equals(((EventEntry)that).title)) // Same end time & title, assume identical
                return true;
            else 
                return (this.title.equals(((EventEntry)that).title)); // Not same end time and/or title, return title comparison
        }
    }
    
    /**
     * Override of java.lang.Object's hashCode(), implemented to facilitate 
     * equal codes for equal objects. Equality is determined by looking at 
     * start time, end time and title. Two events with identical values for 
     * these attributes are considered equal.<p>
     * 
     * @return The object hash code
     */
    @Override
    public int hashCode() {
        final int multiplier = 23;
        int code = 133;
        code = multiplier * code + Integer.valueOf(String.valueOf(start));
        code = multiplier * code + Integer.valueOf(String.valueOf(stop));
        code = multiplier * code + title.hashCode();
        return hashCode;        
    }
    
    /*
    public void getContent(CmsObject cmso) throws CmsException {
        // Parse the XML
        //Locale loc = cmso.getRequestContext().getLocale();
        CmsResource r = cmso.readResource(this.structureId);
        I_CmsXmlDocument xmlDoc = CmsXmlContentFactory.unmarshal(cmso, cmso.readFile(r));
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            I_CmsXmlContentValue paragraphVal = xmlDoc.getValue(("Paragraph[" + i + "]"), locale);
            break;
        }
        //String description = xmlDoc.getValue("Description", locale).getStringValue(cmso);
    }*/
}
