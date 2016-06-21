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
import com.google.ical.iter.RecurrenceIteratorFactory;
import com.google.ical.iter.RecurrenceIterator;
//import com.google.ical.values.DateValueImpl;
import com.google.ical.values.DateValue;
import java.util.ArrayList;
import java.util.List;
//import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
//import org.opencms.workplace.CmsWorkplaceManager;
//import org.opencms.workplace.CmsWorkplaceSettings;
//import org.opencms.xml.I_CmsXmlDocument;
//import org.opencms.xml.content.CmsXmlContentFactory;
//import org.opencms.xml.types.I_CmsXmlContentValue;

/**
 * An event.
 * @author Paul-Inge Flakstad, Norwegian Polar Institute.
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
    
    /** The event resource type name. */
    public static final String RESOURCE_TYPE_NAME_EVENT = "np_event";
    /** The property used for the start time. */
    public static final String PROPERTY_TIME_START = "collector.date";
    /** The property used for the end time. */
    public static final String PROPERTY_TIME_END = "collector.time";
    /** The property used for the recurrence rule. */
    public static final String PROPERTY_RECURRENCE_RULE = "rrule";
    /** The property used for the categories. (Multiple pipe-separated values can be assigned.) */
    public static final String PROPERTY_CATEGORIES = "collector.categories";
    /** The property used for the display option. */
    public static final String PROPERTY_TIME_DISPLAY = "display";
    /** The keyword for display option: display only the date. */
    public static final String TIME_DISPLAY_DATEONLY = "dateonly";
    /** The keyword for display option: display the date and the time. */
    public static final String TIME_DISPLAY_DATETIME = "datetime";
    /** The date format pattern for a full ISO date. */
    public static final String DATE_FORMAT_PATTERN_ISO_FULL = "yyyy-MM-dd'T'HH:mm:ssZ";
    /** The date format pattern for a date-only ISO date. */
    public static final String DATE_FORMAT_PATTERN_ISO_SHORT = "yyyy-MM-dd";
    /** The default date format pattern. */
    public static final String DEFAULT_DATE_FORMAT_PATTERN = DATE_FORMAT_PATTERN_ISO_SHORT;
    /** The default time zone. */
    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("GMT+1:00");
    
    /** Compares EventEntry instances by title. */
    public static final Comparator<EventEntry> COMPARATOR_TITLE =
            new Comparator<EventEntry>() {
                public int compare(EventEntry e1, EventEntry e2) {
                    return e1.getTitle().compareTo(e2.getTitle());
                }
            };
    /** Compares EventEntry instances by start time (sorts by "oldest first"). */
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
    /** Compares EventEntry instances by start time (sorts by "newest first"). */
    public static final Comparator<EventEntry> COMPARATOR_START_TIME_DESC = 
            new Comparator<EventEntry>() {
                public int compare(EventEntry e1, EventEntry e2) {
                    if (e2.getStartTime() < e1.getStartTime())
                        return -1;
                    else if (e2.getStartTime() > e1.getStartTime())
                        return 1;
                    return 0;
                }
            };
    
    /** The event's start time. */
    private long start;
    /** The event's end time. */
    private long stop;
    /** The event's recurrence rule, if any. */
    private String recurrenceRule = "";
    /** The event's title. */
    private String title;
    /** The event's description. */
    private String description;
    //** The event's location. */
    //private String location;
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
    /** The assigned categories, as read from the property {@link #PROPERTY_CATEGORIES}. */
    private String categoriesString = "";
    /** Flag indicating whether or not this event is a recurrence. */
    private boolean isRecurrenceEvent = false;
    /** This object's hash code, used in the override of java.lang.Object's hashCode() method. */
    private volatile int hashCode = 0;
    /** The time zone, defaults to {@link #DEFAULT_TIME_ZONE} until explicitly set. */
    private TimeZone tz = DEFAULT_TIME_ZONE;
    
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(EventEntry.class);
    
    /**
     * Creates a new EventEntry with start and stop set to zero.
     * <p>
     * Avoid using this method whenever possible.
     * 
     * @deprecated Use {@link #EventEntry(org.opencms.jsp.CmsJspActionElement, org.opencms.file.CmsResource)} or {@link #EventEntry(org.opencms.file.CmsObject, org.opencms.file.CmsResource) } instead.
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
     * Creates a new EventEntry with the given values.
     * <p>
     * Avoid using this method whenever possible.
     * 
     * @deprecated Use {@link #EventEntry(org.opencms.jsp.CmsJspActionElement, org.opencms.file.CmsResource)} or {@link #EventEntry(org.opencms.file.CmsObject, org.opencms.file.CmsResource) } instead.
     * 
     * @param start the start time.
     * @param stop the end time.
     * @param title the title.
     * @param description the description.
     * @param resourceId the resource ID.
     * @param structureId the structure ID.
     * @param locale the locale.
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
    
    /**
     * Creates a new event, based on the given details.
     * 
     * @param start the start time.
     * @param stop the end time.
     * @param title the title.
     * @param description the description.
     * @param timeDisplay the time display mode, see {@link #TIME_DISPLAY_DATEONLY} and {@link #TIME_DISPLAY_DATETIME}.
     * @param locale the locale to use.
     * @param resourceId the resource ID.
     * @param structureId the structure ID.
     * @param recurrenceRule the recurrence rule.
     * @param categoriesString the assigned categories, as read from the property {@link #PROPERTY_CATEGORIES}.
     */
    public EventEntry(long start, long stop, String title, String description, String timeDisplay, Locale locale, CmsUUID resourceId, CmsUUID structureId, String recurrenceRule, String categoriesString) {
        this.start = start;
        this.stop = stop;
        this.title = title;
        this.description = description;
        this.timeDisplay = timeDisplay;
        this.locale = locale;
        this.resourceId = resourceId;
        this.structureId = structureId;
        this.recurrenceRule = recurrenceRule;
        this.categoriesString = categoriesString;
    }
    
    /**
     * Creates a new event, with a defined "is recurrence" flag, based on the 
     * given details.
     * <p>
     * This constructor should normally be used only internally by the 
     * {@link #getRecurrences(long, long)} method.
     * 
     * @param start the start time.
     * @param stop the end time.
     * @param title the title.
     * @param description the description.
     * @param timeDisplay the time display mode, see {@link #TIME_DISPLAY_DATEONLY} and {@link #TIME_DISPLAY_DATETIME}.
     * @param locale the locale to use.
     * @param resourceId the resource ID.
     * @param structureId the structure ID.
     * @param recurrenceRule the recurrence rule.
     * @param categoriesString the assigned categories, as read from the property {@link #PROPERTY_CATEGORIES}.
     * @param isRecurrence if true, the created event is flagged as being a recurrence.
     */
    protected EventEntry(long start, long stop, String title, String description, String timeDisplay, Locale locale, CmsUUID resourceId, CmsUUID structureId, String recurrenceRule, String categoriesString, boolean isRecurrence) {
        this(start,stop,title,description,timeDisplay,locale,resourceId,structureId,recurrenceRule,categoriesString);
        this.isRecurrenceEvent = isRecurrence;
    }
    
    /**
     * <strong>Recommended constructor</strong>: Creates a new event, using the 
     * given action element, and based on the given event resource.
     * 
     * @param cms initialized action element.
     * @param eventResource the event resource.
     */
    public EventEntry(CmsJspActionElement cms, CmsResource eventResource) {
        this(cms.getCmsObject(), eventResource);
        
        // Set native date format
        try {
            String nativeDateFormatPattern = cms.label("label.event.dateformat.".concat(this.timeDisplay));
            this.sdf = new SimpleDateFormat(nativeDateFormatPattern, locale);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to set native localized date format for event. Fallback to default.", e);
            }
        }
    }
    
    /**
     * Creates a new event, using the given CmsObject, and based on the given 
     * event resource.
     * <p>
     * Use {@link #EventEntry(org.opencms.jsp.CmsJspActionElement, org.opencms.file.CmsResource)} 
     * instead, if at all possible. (This constructor is unable to read the  
     * localized date format pattern, and will fallback to using 
     * {@link #DEFAULT_DATE_FORMAT_PATTERN}.
     * 
     * @param cmso initialized cms object.
     * @param eventResource the event resource.
     */
    public EventEntry(CmsObject cmso, CmsResource eventResource) {
        try {
            this.title = cmso.readPropertyObject(eventResource, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue("");
            this.start = Long.valueOf(cmso.readPropertyObject(eventResource, PROPERTY_TIME_START, false).getValue("0"));
            this.stop = Long.valueOf(cmso.readPropertyObject(eventResource, PROPERTY_TIME_END, false).getValue("0"));
            this.description = cmso.readPropertyObject(eventResource, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue("");
            this.timeDisplay = cmso.readPropertyObject(eventResource, PROPERTY_TIME_DISPLAY, false).getValue(TIME_DISPLAY_DATETIME);
            this.locale = cmso.getRequestContext().getLocale();
            this.resourceId = eventResource.getResourceId();
            this.structureId = eventResource.getStructureId();
            this.recurrenceRule = cmso.readPropertyObject(eventResource, PROPERTY_RECURRENCE_RULE, false).getValue("");
            this.categoriesString = cmso.readPropertyObject(eventResource, PROPERTY_CATEGORIES, false).getValue("");
            this.sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT_PATTERN);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                if (e instanceof CmsException) {
                    LOG.error("Error creating event: missing or invalid required property.", e);
                }
                else {
                    LOG.error("Unexpected error creating event.", e);
                }
            }
        }
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
        this.recurrenceRule = other.recurrenceRule;
        this.categoriesString = other.categoriesString;
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
        if (this.isDisplayDateOnly()) {
            return EventCalendarUtils.getStartOfDay(this.start);
        } else {
            return this.start; 
        }
    }
    
    /**
     * Gets the event's end time.<p>
     * 
     * @return The event's end time.
     */
    public long getEndTime() {
        if (this.isDisplayDateOnly()) {
            return EventCalendarUtils.getEndOfDay(this.stop);
        } else {
            return this.stop;
        }
    }    
    
    /**
     * Gets this event's time display mode keyword, one of 
     * EventEntry#TIME_DISPLAY_DATETIME and EventEntry#TIME_DISPLAY_DATEONLY.
     * @return This event's time display mode keyword.
     */
    public String getTimeDisplayMode() { return this.timeDisplay; }
    
    /** 
     * Determines if the event starts and ends on the same day.
     * <p>
     * Note that for events without an end time set, <code>true</code> is always 
     * returned.
     * 
     * @return true if the event starts and ends on the same day.
     */
    public boolean isOneDayEvent() {        
        if (!this.hasEndTime())
            return true; // No end time, assume this is one-day event
       
        // Create two calendars, for start and stop
        Calendar calStart = new GregorianCalendar();
        calStart.setTimeInMillis(this.getStartTime());
       
        Calendar calEnd = new GregorianCalendar();
        calEnd.setTimeInMillis(this.getEndTime());
        
        // Compare the year, month and date of the two calendars, return the result
        return (calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR) &&
                calStart.get(Calendar.MONTH) == calEnd.get(Calendar.MONTH) &&
                calStart.get(Calendar.DATE) == calEnd.get(Calendar.DATE));
    }
    
    /** 
     * Determines if the event starts and ends in the same month.
     * <p>
     * Note that for events without an end time set, <code>true</code> is always 
     * returned.
     * 
     * @return true if the event starts and ends in the same month, false if not.
     */
    public boolean isOneMonthEvent() {
        if (!this.hasEndTime())
            return true;
        
        Calendar calStart = new GregorianCalendar();
        calStart.setTimeInMillis(this.getStartTime());
        
        Calendar calEnd = new GregorianCalendar();
        calEnd.setTimeInMillis(this.getEndTime());
        
        return calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR) && 
                calStart.get(Calendar.MONTH) == calEnd.get(Calendar.MONTH);
    }
    
    /** 
     * Determines if the event starts and ends in the same year.
     * <p>
     * Note that for events without an end time set, <code>true</code> is always 
     * returned.
     * 
     * @return true if the event starts and ends in the same year, false if not.
     */
    public boolean isOneYearEvent() {
        if (!this.hasEndTime())
            return true;
        
        Calendar calStart = new GregorianCalendar();
        calStart.setTimeInMillis(this.getStartTime());
        
        Calendar calEnd = new GregorianCalendar();
        calEnd.setTimeInMillis(this.getEndTime());
        
        return calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR);
    }
    
    /**
     * Determines if the event begins (and ends, if it has an end time set) in 
     * the "current" year, as defined by the system's "now" timestamp.
     * 
     * @return true if the event takes place in the "current" year.
     */
    public boolean isCurrentYearEvent() {
        return isCurrentYearEvent(new Date());
    }
    
    /**
     * Determines if the event begins (and ends, if it has an end time set) in 
     * the "current" year, as defined by the given date's timestamp.
     * 
     * @param d The date to use for comparison.
     * @return true if the event takes place in the "current" year.
     */
    public boolean isCurrentYearEvent(Date d) {
        Calendar calStart = new GregorianCalendar();
        calStart.setTimeInMillis(this.getStartTime());
        
        Calendar calNow = new GregorianCalendar();
        calNow.setTimeInMillis(d.getTime());
        
        if (calStart.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)) {
            if (this.hasEndTime()) {
                Calendar calEnd = new GregorianCalendar();
                calEnd.setTimeInMillis(this.getEndTime());
                return calEnd.get(Calendar.YEAR) == calNow.get(Calendar.YEAR);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Determines if this event has a set recurrence rule or not.
     * 
     * @return True if a recurrence rule exists, false if not.
     */
    public boolean hasRecurrenceRule() {
        if (recurrenceRule == null || recurrenceRule.isEmpty())
            return false;
        return true;
    }
    
    /**
     * Determines if the event is assigned any categories.
     * 
     * @return true if the event is assigned at least 1 category.
     */
    public boolean hasCategories() { return this.categoriesString != null && !this.categoriesString.trim().isEmpty(); }
    
    
    
    /**
     * Determines if the event has taken place in the past (compared to the 
     * reference time contained in the given date) and is over.<p>
     * 
     * @param d The date to use as a reference time.
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
     * Determines if the event is in progress, using the system's current time 
     * as "now".
     * 
     * @return true if the event is in progress, false if not.
     */
    public boolean isInProgress() {
        return isInProgress(new Date());
    }
    
    /**
     * Determines if the event is in progress, using the given date's time as
     * "now".
     * 
     * @param d The date to use as a reference time.
     * @return true if the event is in progress, false if not.
     */
    public boolean isInProgress(Date d) {
        if (this.hasEndTime()) {
            return this.getStartTime() <= d.getTime() && this.getEndTime() >= d.getTime();
        }
        return false;
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
     * Determines whether or not the event is a recurrence.
     * 
     * @return True if the event is a recurrence, false if not.
     */
    public boolean isRecurrence() {
        return this.isRecurrenceEvent;
    }
    
    /**
     * Determines if the event starts on a given date.
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
     * Determines if the event starts in a given range.
     * <p>
     * Start and end time values are inclusive 
     * (i.e. if event.getStartTime() == rangeStart, 'true' is returned).
     * 
     * @param rangeStart The long representation of the start time for the range.
     * @param rangeStop The long representation of the end time for the range.
     * 
     * @return True if the event starts in the given range, false if not.
     */
    public boolean startsInRange(long rangeStart, long rangeStop) {
        return this.getStartTime() >= rangeStart && this.getStartTime() <= rangeStop;
    }
    
    /**
     * Determines if the event overlaps the given range.
     * 
     * @param rangeStart The range start time.
     * @param rangeStop The range end time.
     * @return True if the event overlaps the given range, false if not.
     */
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
    
    /**
     * Determines if this event starts before a given time.
     * <p>
     * The comparison is done using the long representation of the dates. 
     * 
     * @param d The date to use for comparison.
     * @return true if the event starts before the given date's timestamp, false if not.
     */
    public boolean startsBefore(Date d) { return d.getTime() > this.getStartTime(); }
    
    /** 
     * Determines if this event ends after a given time.
     * <p>
     * The long representation of the dates are compared. 
     * 
     * @param d The date to use for comparison.
     * @return true if the event ends after the given date's timestamp, false if not.
     */
    public boolean endsAfter(Date d) { return d.getTime() < this.getEndTime(); }
    
    /**
     * Determines if this event ends before a given time.
     * 
     * @return true if the event ends before the given time, false if not.
     */
    public boolean endsBefore(long when) { return when > this.getEndTime(); }
    
    /**
     * Determines if this event starts after a given time.
     * 
     * @return true if the event starts after the given time, false if not.
     */
    public boolean startsAfter(long when) { return when < this.getStartTime(); }
    
    /**
     * Gets the event's title.
     * 
     * @return the event's title.
     */
    public String getTitle() { return this.title; }
    
    /**
     * Gets the event's description. 
     * 
     * @return the event's description. 
     */
    public String getDescription() { return this.description; }
    
    /**
     * Gets the event's structure ID. 
     * 
     * @return the event's structure ID. 
     */
    public CmsUUID getStructureId() { return this.structureId; }
    
    /**
     * Gets the event's resource ID. 
     * 
     * @return the event's resource ID. 
     */
    public CmsUUID getResourceId() { return this.resourceId; }
    
    /**
     * Gets the event's recurrence rule, or an empty string if none. 
     * 
     * @return the event's recurrence rule, or an empty string if none. 
     */
    public String getRecurrenceRule() { return this.recurrenceRule; }
    
    /**
     * Gets the event's assigned categories.
     * 
     * @param cmso Provides CMS access, needed to read categories.
     * @return This event's assigned categories, or an empty list if none.
     */
    public List<CmsCategory> getAssignedCategories(CmsObject cmso) {
        // Get the category service
        CmsCategoryService catService = CmsCategoryService.getInstance();
        // Read assigned categories for this resource
        List<CmsCategory> assignedCategories = new ArrayList<CmsCategory>(0); 
        try {
            assignedCategories = catService.readResourceCategories(cmso, this.getUri(cmso));
        } catch (Exception e) {}
        
        return assignedCategories;
    }
    
    /**
     * Checks if this event is assigned the category identified by the given 
     * path.
     * <p>
     * The evaluation is done by comparing category <em>relative</em> paths, 
     * which is language-/locale-agnostic evaluation. (In contrast to comparing
     * <em>root</em> paths, which is generally language-/locale-specific.)
     * <p>
     * If root path (= exact match) comparison is needed, please use 
     * {@link #isAssignedCategory(org.opencms.file.CmsObject, org.opencms.relations.CmsCategory)} 
     * instead.
     * 
     * @param cmso Provides access to the CMS, needed for reading categories.
     * @param categoryPath The relative path to the category in question.
     * @return True if this event is assigned the category identified by the given path, false if not.
     * @see #isAssignedCategory(org.opencms.file.CmsObject, org.opencms.relations.CmsCategory) 
     */
    public boolean isAssignedCategory(CmsObject cmso, String categoryPath) {
        for (CmsCategory cat : getAssignedCategories(cmso)) {
            if (cat.getPath().equals(categoryPath)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if this event is assigned the given category.
     * <p>
     * The check is done by comparing category root paths.
     * 
     * @param cmso Provides access to the CMS, needed for reading categories.
     * @param category The category in question.
     * @return True if this event is assigned the given category, false if not.
     * @see #isAssignedCategory(org.opencms.file.CmsObject, java.lang.String) 
     */
    public boolean isAssignedCategory(CmsObject cmso, CmsCategory category) {
        for (CmsCategory cat : getAssignedCategories(cmso)) {
            if (cat.getRootPath().equals(category.getRootPath())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the event's categories string, as read from the property 
     * {@link #PROPERTY_CATEGORIES}, or an empty string if none.
     * 
     * @return the event's categories string, or an empty string if none.
     */
    public String getCategoriesString() { return this.categoriesString; }
    
    /**
     * Gets the event's locale.
     * 
     * @return the event's locale.
     */
    public Locale getLocale() { return this.locale; }
    
    /**
     * Gets the event's HTML code.
     * 
     * @return the event's HTML code.
     */
    public String getHtml() { return this.html; }
    
    /** 
     * Gets the event's URI, relative to the site - e.g. "/my-events/the-event.html". 
     * 
     * @param cmso An initialized CmsObject, used to read this event's corresponding resource from the OpenCms VFS.
     * @return the event's URI, relative to the site.
     * @throws CmsException If anything goes wrong.
     */
    public String getUri(CmsObject cmso) throws CmsException {
        return cmso.getSitePath(cmso.readResource(this.getStructureId()));
    }
    
    /**
     * Gets the event's time span, optimized, as ready-to-use HTML.
     * <p>
     * The returned HTML will be optimized in length, and also according to the 
     * given "now" timestamp. (If the event is "this year", the year is not 
     * displayed.)
     * <p>
     * The returned HTML is rich snippets compatible. (Uses <code>time</code> 
     * tags with <code>startDate</code> and <code>endDate</code> attributes.)
     * 
     * @param cms An initialized CMS action element, needed to read labels.
     * @param now The "now" timestamp, used to determine if this event occurs in the "current" year.
     * @return The event's time span, as ready-to-use HTML.
     * @throws CmsException
     */
    public String getTimespanHtml(CmsJspActionElement cms, Date now) throws CmsException {
	/*String loc = locale.toString();
        SimpleDateFormat datetime = new SimpleDateFormat(cms.label("label.event.dateformat.datetime"), locale);
        SimpleDateFormat dateonly = new SimpleDateFormat(cms.label("label.event.dateformat.dateonly"), locale);
        SimpleDateFormat timeonly = new SimpleDateFormat(cms.label("label.event.dateformat.timeonly"), locale);
        SimpleDateFormat month = new SimpleDateFormat(cms.label("label.event.dateformat.month"), locale);*/
        SimpleDateFormat dmyt = new SimpleDateFormat(cms.label("label.event.dateformat.dmyt"), locale);
        SimpleDateFormat dmy = new SimpleDateFormat(cms.label("label.event.dateformat.dmy"), locale);
        SimpleDateFormat dm = new SimpleDateFormat(cms.label("label.event.dateformat.dm"), locale);
        SimpleDateFormat dmt = new SimpleDateFormat(cms.label("label.event.dateformat.dmt"), locale);
        SimpleDateFormat d = new SimpleDateFormat(cms.label("label.event.dateformat.d"), locale);
        SimpleDateFormat t = new SimpleDateFormat(cms.label("label.event.dateformat.t"), locale);
        SimpleDateFormat iso = this.getDatetimeAttributeFormat(locale);
        
        boolean currentYearEvent = this.isCurrentYearEvent(now);
        
        // Select initial date format
        SimpleDateFormat df = this.isDisplayDateOnly() ? (currentYearEvent ? dm : dmy) : (currentYearEvent ? dmt : dmyt);
        
        String begins = null;
        String ends = null;
        String beginsIso = null;
        String endsIso = null;
        try {
            SimpleDateFormat beginFormat = df;
            beginsIso = iso.format(new Date(this.getStartTime()));
            
            // If there is an end-time
            if (this.hasEndTime()) {
                SimpleDateFormat endFormat = df;
                
                if (this.isOneDayEvent()) {
                    if (!this.isDisplayDateOnly()) {
                        // End time is on the same day as begin time, format only the hour/minute
                        endFormat = t;
                    }
                } else {
                    // Not one-day event, but maybe same month?
                    if (this.isOneMonthEvent() && this.isDisplayDateOnly()) {
                        endFormat = currentYearEvent ? dm : dmy;
                        beginFormat = d;
                    }
                }
                
                ends = endFormat.format(new Date(this.getEndTime())).replaceAll("\\s", "&nbsp;");
                endsIso = iso.format(new Date(this.getEndTime()));
            }
            begins = beginFormat.format(new Date(this.getStartTime())).replaceAll("\\s", "&nbsp;");
        } catch (NumberFormatException nfe) {
            // Keep ends=null
        }
        
        String s = "";
        
        s += "<time itemprop=\"startDate\" datetime=\"" + beginsIso + "\">" + begins + "</time>";
        
        if (ends != null) {
            // Sometimes we want to use a space, sometimes not...
            String spaceOrNot = begins.contains("nbsp") && ends.contains("nbsp") ? " " : "";
            s += spaceOrNot + "&ndash;" + spaceOrNot 
                    + "<time itemprop=\"endDate\" datetime=\"" + endsIso + "\">" + ends + "</time>";
        }
        
        return s;
    }
    
    /**
     * Gets the event's time span, optimized, as ready-to-use HTML.
     * <p>
     * The returned HTML will be optimized in length, and also according to the 
     * system's "now" timestamp. (If the event is "this year", the year is not 
     * displayed.)
     * <p>
     * The returned HTML is rich snippets compatible. (Uses <code>time</code> 
     * tags with <code>startDate</code> and <code>endDate</code> attributes.)
     * 
     * @param cms An initialized CMS action element, needed to read labels.
     * @return The event's time span, as ready-to-use HTML.
     * @throws CmsException
     * @see #getTimespanHtml(org.opencms.jsp.CmsJspActionElement, java.util.Date) 
     */
    public String getTimespanHtml(CmsJspActionElement cms) throws CmsException {
        return getTimespanHtml(cms, new Date());
    }
    
    /**
     * Gets the event's time span, with full data, as ready-to-use HTML.
     * <p>
     * The returned time span is the FULL data, e.g. 01 May 2016 - 02 May 2016.
     * For a more optimized form, e.g. 1-2 May or 1-2 May 2023, see 
     * {@link #getTimespanHtml(org.opencms.jsp.CmsJspActionElement, java.util.Date)}.
     * 
     * @param cms initialized action element.
     * @return the event's time span, as ready-to-use HTML.
     * @throws CmsException
     * @see #getTimespanHtml(org.opencms.jsp.CmsJspActionElement, java.util.Date) 
     */
    public String getFullTimespanHtml(CmsJspActionElement cms) throws CmsException {
        //SimpleDateFormat datetime = new SimpleDateFormat(cms.label("label.event.dateformat.datetime"), locale);
        //SimpleDateFormat dateonly = new SimpleDateFormat(cms.label("label.event.dateformat.dateonly"), locale);
        SimpleDateFormat dfIso = getDatetimeAttributeFormat(locale);
        
        // Select date format
        SimpleDateFormat df = sdf != null ? sdf : new SimpleDateFormat(cms.label("label.event.dateformat.").concat(this.timeDisplay), locale);
        //SimpleDateFormat df = isDisplayDateOnly() ? dateonly : datetime;
        
        String begins = null;
        String ends = null;
        String beginsIso = null;
        String endsIso = null;
        try {
            begins = df.format(new Date(getStartTime())).replaceAll("\\s", "&nbsp;");
            beginsIso = dfIso.format(new Date(getStartTime()));
            // If there is an end-time
            if (this.hasEndTime()) {
                if (this.isOneDayEvent()) {
                    SimpleDateFormat timeonly = new SimpleDateFormat(cms.label("label.event.dateformat.timeonly"), locale);
                    // End time is on the same day as begin time, format only the hour/minute
                    ends = timeonly.format(new Date(getEndTime())).replaceAll("\\s", "&nbsp;");
                } else {
                    ends = df.format(new Date(getEndTime())).replaceAll("\\s", "&nbsp;");
                }
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
     * Gets the appropriate date format for an event's "datetime" attribute 
     * (google "rich snippets" for more info).
     * <p>
     * The format is determined by evaluating the event's "time display" mode (date only or date and time).
     * 
     * @param locale The locale to use in the returned date format.
     * @return SimpleDateFormat The appropriate format for the datetime attribute.
     * @throws CmsException
     */
    public SimpleDateFormat getDatetimeAttributeFormat(Locale locale) throws CmsException {
        //SimpleDateFormat dfFullIso = new SimpleDateFormat(DATE_FORMAT_PATTERN_ISO_FULL, locale);
        //SimpleDateFormat dfShortIso = new SimpleDateFormat(DATE_FORMAT_PATTERN_ISO_SHORT, locale);

        // Examine if the event is "display date only", and select the appropriate date format to use for the "datetime" attribute
        SimpleDateFormat dfIso = new SimpleDateFormat(this.isDisplayDateOnly() ? DATE_FORMAT_PATTERN_ISO_SHORT : DATE_FORMAT_PATTERN_ISO_FULL, locale);
        //SimpleDateFormat dfIso = this.isDisplayDateOnly() ? dfShortIso : dfFullIso;
        dfIso.setTimeZone(tz);

        return dfIso;
    }
    
    /**
     * Sets the date format, based on the given pattern.<p>
     * 
     * @param pattern the date format pattern.
     * @return This instance, updated.
     * 
     * @see java.text.SimpleDateFormat
     */
    public EventEntry setDateFormat(String pattern) {
        sdf = new SimpleDateFormat(pattern);
        return this;
    }
    
    /**
     * Sets the date format.<p>
     * 
     * @param sdf the date format.
     * @return This instance, updated.
     * 
     * @see java.text.SimpleDateFormat
     */
    public EventEntry setDateFormat(SimpleDateFormat sdf) {
        this.sdf = sdf;
        return this;
    }
    
    /**
     * Sets the HTML code content.<p>
     * 
     * @param html the HTML code content.
     * @return This instance, updated.
     */
    public EventEntry setHtml(String html) {
        this.html = html;
        return this;
    }
    
    /**
     * Sets the time display mode for this event.
     * <p>
     * The given keyword must be one of
     * <ul>
     *  <li>{@link #TIME_DISPLAY_DATEONLY}</li>
     *  <li>{@link #TIME_DISPLAY_DATETIME}</li>
     * </ul>
     * Defaults to EventEntry#TIME_DISPLAY_DATETIME. (Any value passed other than
     * EventEntry#TIME_DISPLAY_DATEONLY will cause this value to be assigned.)
     * 
     * @param displayMode The display mode keyword for this event.
     * @return This event object instance.
     */
    public EventEntry setTimeDisplayMode(String displayMode) {
        if (TIME_DISPLAY_DATEONLY.equals(displayMode)) {
            this.timeDisplay = TIME_DISPLAY_DATEONLY;
        } else {
            this.timeDisplay = TIME_DISPLAY_DATETIME;
        }
        return this;
    }
    
    /**
     * Gets an iterator for the recurrences of this event, or <code>null</code> 
     * if this event has no recurrence rule.
     * 
     * @return An an iterator for the recurrences of this event, or <code>null</code> if none.
     * @throws java.text.ParseException 
     */
    public RecurrenceIterator getRecurrenceIterator() throws java.text.ParseException {
        if (!this.hasRecurrenceRule())
            return null;
        
        // Get the recurrence rule
        String rRule = "RRULE:" + getRecurrenceRule();
        
        // Get the initial begin timestamp
        Date initialStartTime = new Date(getStartTime());
        // Get the iterator for the recurrence dates, using the recurrence rule (RRULE) found on the event
        RecurrenceIterator iRecur = RecurrenceIteratorFactory.createRecurrenceIterator(rRule
                                                                                        , EventCalendarUtils.convertToDateValue(initialStartTime)
                                                                                        , tz
                                                                                        );
        return iRecur;
    }
    
    /**
     * Gets the date that is this event's closest start date, before or on the 
     * given point in time.
     * <p>
     * If no recurrence rule is set, the regular start time is returned.
     * <p>
     * Otherwise, this method will iterate over recurrences and return the first
     * encountered start date that is not after the given point in time. If no 
     * such start date exists, the regular start time is returned.
     * 
     * @param pointInTime The point in time to use as a reference point.
     * @return This event's closest start date before or on the given point in time, or, if no such start date exists, the regular start date.
     */
    public Date getClosestPastStartDate(Date pointInTime) {
    //public Date getClosestPastStartDate(DateValue marker) {
        try {
            if (!this.hasRecurrenceRule())
                return new Date(this.getStartTime()); // Not a recurring event, return initial start time
            
            //Date markerDate = convertToDate(marker); // The marker date
            Date markerDate = pointInTime; // The marker date
            Date endMarkerDate = new Date(EventCalendarUtils.getDateEndCal(markerDate).getTimeInMillis()); // Timestamp: The end of the marker date
            Date closest = null;
            
            // Safety 
            int iterations = 0;
            int maxIterations = 1000;
            
            // Loop recurrences
            RecurrenceIterator iRecurrences = getRecurrenceIterator();
            while (iRecurrences.hasNext() && iterations++ < maxIterations) {
                Date recurrenceStartDate = EventCalendarUtils.convertToDate( (DateValue)iRecurrences.next() );
                if (recurrenceStartDate.after(endMarkerDate)) // Is the recurrence start date after the marker date?
                    break; // Yes: break
                /*if (event.hasEndTime() && !event.isOneDayEvent()) { // Is the recurrence a multiple-day event?
                    if (new Date(recStartDate.getTime() + (event.getEndTime() - event.getStartTime())).after(endMarkerDate)) // Is the recurrence end time after the 
                        break;
                }*/
                closest = recurrenceStartDate; // No - then so far it has the closest start date
            }
            
            if (closest != null) {
                return closest; // Found a date in the past, return it
            } else {
                return new Date(this.getStartTime()); // Found nothing, return the event's initial start time
            }
        } catch(Exception e) {
            return new Date(this.getStartTime());
        }
    }
    
    /**
     * Gets the event's begin time, relative to the given point in time.
     * <p>
     * If the event is non-recurring, the regular begin time is returned. 
     * <p>
     * Otherwise (if the event is recurring), the "closest" begin time, relative 
     * to the given pointInTime, is returned. (I.e. could be today or next week, 
     * but not 3 days ago (again, relative to pointInTime).)
     * 
     * @param pointInTime The point in time to use as "now". Pass null to use the actual "now".
     * @return The event's regular begin time (if non-recurring), or the "closest" begin time, relative to the given point in time (if recurring).
     */
    public Date getBegin(Date pointInTime) {
        // Get the initial begin timestamp
        Date initialStartTime = new Date(this.getStartTime());

        // This event is set to recur
        try {
            RecurrenceIterator iRecur = getRecurrenceIterator();
            if (iRecur == null) {
                // Not recurring - return the initial start time
                return initialStartTime;
            }
            
            if (pointInTime == null) {
                // Get the current time
                Date currentTime = new Date();;
                pointInTime = currentTime;
            }
            
            if (!this.isOneDayEvent() && this.hasEndTime()) {
                // Advance to the closest start date in the past (relative to the given point in time)
                iRecur.advanceTo( EventCalendarUtils.convertToDateValue(getClosestPastStartDate(pointInTime)) );
                //iRecur.advanceTo( convertToDateValue(getClosestPastStartDate(convertToDateValue(pointInTime))));
            } else { // One-day event (or no end time specified)
                // Advance to the point in time
                iRecur.advanceTo( EventCalendarUtils.convertToDateValue(pointInTime) );
            }

            if (iRecur.hasNext()) {
                // Get the event's "next" recurring date (could be "today")
                DateValue dv = (DateValue)iRecur.next();
                Calendar cal = new GregorianCalendar(tz, locale);
                // First, set the time to the *initial* start time, so that any "event begin" clock time is preserved
                cal.setTime(initialStartTime);
                // Then update year, month and day
                cal.set(dv.year(), dv.month()-1, dv.day());
                // And return the Date instance representing the "next" beginning date
                return cal.getTime();
            }
        } catch (Exception e) {
            //out.println("<!-- Error processing recurring event: " + e.getMessage() + " -->");
        }
        return null;
    }
    
    /**
     * Gets the "event end" timestamp (as a Date), based on the given eventStart
     * timestamp.
     * <p>
     * The intended use case for this method is to get the end time for a
     * specific recurrence (defined by eventStart) of the given event.
     * <p>
     * If the given event is non-recurring, or if the given eventStart timestamp 
     * is identical to the given event's initial start time, then the initial 
     * end time is returned.
     * <p>
     * If the given event is a recurring one, and the given eventStart timestamp
     * differs from the given event's initial start time, then a new end time
     * is calculated.
     * <p>
     * If no end time is set at all, the returned value will always be 
     * "new Date(0)".
     * 
     * @param eventStart The event's start time
     * @return The "event end" timestamp, as a Date.
     */
    public Date getEnd(Date eventStart) {
        long longBeginOri = this.getStartTime();
        long longBegin = eventStart.getTime();

        if (longBeginOri < longBegin && this.hasRecurrenceRule()) {
            // The given event's initial start time is before the given 
            // eventStart AND a recurrence rule exists for the given event: 
            //      Assume that the given eventStart is the start time for a later 
            //      recurrence of the given event: Adjust the end time equally
            long diff = longBegin - longBeginOri;
            return new Date(this.hasEndTime() ? (this.getEndTime() + diff) : 0);
        }
        // No recurrence rule OR the no difference in start times: Just return
        // the event's initial end time
        return new Date(this.getEndTime());
    }
    
    /**
     * Gets the next N recurrences of this event, after the start time, or an 
     * empty list if there are no recurrences.
     * 
     * @param start the start time.
     * @return the N next recurrences of this event, after the given start time, or an empty list if none.
     */
    public List<EventEntry> getRecurrences(long start, int limit) {
        return getRecurrences(start, Long.MAX_VALUE, limit);
    }
    
    /**
     * Gets recurrences of this event, within the time frame specified by the 
     * given start and end values, or an empty list if there are no recurrences.
     * 
     * @param start the time frame start.
     * @param end the time frame end.
     * @return all recurrences of this event within the given time frame, or an empty list if none.
     */
    public List<EventEntry> getRecurrences(long start, long end) {
        return getRecurrences(start, end, Integer.MAX_VALUE);
    }
    
    /**
     * Gets recurrences of this event, within the time frame specified by the 
     * given start and end values, or an empty list if there are no recurrences.
     * <p>
     * It is safe to provide a huge value for <code>end</code>, if the limit is 
     * reasonable: A limit check is done for each addition to the returned list.
     * 
     * @param start the time frame start.
     * @param end the time frame end.
     * @param limit the maximum number of recurrences to get.
     * @return a limited number of recurrences of this event within the given time frame, or an empty list if none.
     */
    public List<EventEntry> getRecurrences(long start, long end, int limit) {
        List<EventEntry> recurrences = new ArrayList<EventEntry>();
        
        if (this.hasRecurrenceRule()) {

            try {
                RecurrenceIterator iRecur = getRecurrenceIterator();
                
                if (!this.isOneDayEvent() && this.hasEndTime()) {
                    // Advance to the closest start date in the past (relative to the current workplace time)
                    iRecur.advanceTo(EventCalendarUtils.convertToDateValue(getClosestPastStartDate(new Date(start))));
                } else { // One-day event (or no end time specified)
                    // Advance to the point in time
                    iRecur.advanceTo( EventCalendarUtils.convertToDateValue(new Date(start)) );
                }

                while (iRecur.hasNext() && recurrences.size() < limit) {
                    // Get the event's "next" recurring date (could be "today")
                    DateValue dv = (DateValue)iRecur.next();
                    Calendar cal = new GregorianCalendar(tz, locale);
                    // First, set the time to the *initial* start time, so that any "event begin" clock time is preserved
                    cal.setTime(new Date(this.getStartTime()));
                    // Then update year, month and day
                    cal.set(dv.year(), dv.month()-1, dv.day());
                    
                    // Set the Date instance representing the "next" beginning date
                    Date nextBeginTime = cal.getTime();
                    
                    if (nextBeginTime.getTime() > end) {
                        break; // Break out of the while-loop - we're out of range
                    }
                    
                    // Set the subsequent end time, according to the "next" begin date
                    Date nextEndTime = this.getEnd(nextBeginTime);
                    
                    // If the recurring event's "original" start time is before the calculated "next" begin time, we're dealing with a recurrence
                    boolean isRecurrence = this.getStartTime() < nextBeginTime.getTime();
                    if (isRecurrence) {
                        try {
                            // Create the recurrence event (it's identical to the original event, but with begin/end dates adjusted)
                            EventEntry recurrence = new EventEntry(nextBeginTime.getTime()
                                                    , nextEndTime.getTime()
                                                    , this.getTitle()
                                                    , this.getDescription()
                                                    , this.getTimeDisplayMode()
                                                    , this.getLocale()
                                                    , this.getResourceId()
                                                    , this.getStructureId()
                                                    , this.getRecurrenceRule()
                                                    , this.getCategoriesString()
                                                    , true); // "is recurrence event"
                            //if (DEBUG) out.println("<p>Recurrence event '" + recurrence.getTitle() + "' created.</p>");
                            // If the timespan of the recurrence event overlaps today ...
                            if (recurrence.overlapsRange(start, end)) {
                                //if (DEBUG) out.println("<p>Recurrence event '" + recurrence.getTitle() + "' DOES overlap current time: adding it.</p>");
                                recurrences.add(recurrence);
                                if (recurrences.size() >= limit) {
                                    break;
                                }
                            } else {
                                //if (DEBUG) out.println("<p>Recurrence event '" + recurrence.getTitle() + "' does NOT overlap current time: ignoring it.</p>");
                            }
                        } catch (Exception e) {
                            //out.println("<!-- Error processing recurrence of event: " + e.getMessage() + " -->");
                        }
                    }
                }
            } catch (Exception ee) {
                //out.println("<!-- ERROR on event '" + recurringEventResource.getRootPath() + "': " + ee.getMessage() + " -->");
            }
        }
        return recurrences;
    }
    
    /**
     * Sets the time zone.
     * 
     * @param tz the time zone.
     * @return this instance, updated.
     */
    public EventEntry setTimeZone(TimeZone tz) {
        this.tz = tz;
        return this;
    }
    /**
     * Gets the time zone.
     * <p>
     * If not priorly set explicitly via {@link #setTimeZone(java.util.TimeZone)},
     * the returned time zone will be {@link #DEFAULT_TIME_ZONE}.
     * 
     * @return the time zone.
     */
    public TimeZone getTimeZone() {
        return this.tz;
    }
    
    /**
     * Compares two EventEntry objects by evaluating their start time, end time 
     * and title.<p>
     * 
     * @param that another EventEntry object.
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
     * equal codes for equal objects.
     * <p>
     * Equality is determined by looking at start time, end time and title. Two 
     * events with identical values for these attributes are considered equal.
     * 
     * @return The object hash code
     */
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            final int multiplier = 23;
            int code = 133;
            code = multiplier * code + Integer.valueOf(String.valueOf(start));
            code = multiplier * code + Integer.valueOf(String.valueOf(stop));
            code = multiplier * code + title.hashCode();
            hashCode = code;
        }
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
