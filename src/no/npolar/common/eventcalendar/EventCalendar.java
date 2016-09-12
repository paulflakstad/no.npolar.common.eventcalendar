package no.npolar.common.eventcalendar;

import java.util.*;
//import java.sql.SQLException;
//import java.text.ParseException;
import java.text.SimpleDateFormat;
//import org.opencms.file.CmsDataAccessException;
import java.util.ArrayList;
import org.opencms.file.CmsResource;
//import org.opencms.util.CmsUUID;
import org.opencms.main.CmsException;
//import org.opencms.xml.A_CmsXmlDocument;
//import org.opencms.xml.content.*;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.jsp.CmsJspActionElement;
//import org.opencms.jsp.CmsJspXmlContentBean;
import org.opencms.relations.CmsCategory;

/**
 * An event calendar.
 * 
 * @author Paul-Inge Flakstad
 */
public class EventCalendar extends GregorianCalendar {
    /** Constant for jumping one month back in time */
    public static final int WARP_PREV_MONTH = -1;
    /** Constant for jumping one month forward in time */
    public static final int WARP_NEXT_MONTH = 1;
    /** Constant for jumping back in time to the first day of the previous week */
    public static final int WARP_FIRST_DAY_OF_PREV_WEEK = -7;
    /** Constant for jumping (possibly back in time) to the first day of the current week */
    public static final int WARP_FIRST_DAY_OF_WEEK = -6;
    
    /** Constant for parameter type: next date */
    public static final int PARAMETER_TYPE_NEXT_DATE = 0;
    /** Constant for parameter type: previous date */
    public static final int PARAMETER_TYPE_CURRENT_DATE = 1;
    /** Constant for parameter type: previous date */
    public static final int PARAMETER_TYPE_PREV_DATE = 2;
    
    /** Constant for parameter type: next month */
    public static final int PARAMETER_TYPE_NEXT_MONTH = 10;
    /** Constant for parameter type: previous month */
    public static final int PARAMETER_TYPE_CURRENT_MONTH = 11;
    /** Constant for parameter type: previous month */
    public static final int PARAMETER_TYPE_PREV_MONTH = 12;
    
    /** Constant for parameter type: next year */
    public static final int PARAMETER_TYPE_NEXT_YEAR = 20;
    /** Constant for parameter type: previous year */
    public static final int PARAMETER_TYPE_CURRENT_YEAR = 21;
    /** Constant for parameter type: previous year */
    public static final int PARAMETER_TYPE_PREV_YEAR = 22;
    
    /** Range value used for catching all events inside the current year */
    public static final int RANGE_CURRENT_YEAR = 0;
    /** Range value used for catching all events inside the current month */
    public static final int RANGE_CURRENT_MONTH = 1;
    /** Range value used for catching all events inside the current date */
    public static final int RANGE_CURRENT_DATE = 2;
    /** Range value used for catching all events inside the current week */
    public static final int RANGE_CURRENT_WEEK = 3;
    /** Range value used for catching all upcoming and in-progress events */
    public static final int RANGE_UPCOMING_AND_IN_PROGRESS = 4;
    /** Range value used for catching all expired events. This range spans from year 1 to now */
    public static final int RANGE_CATCH_ALL_EXPIRED = 98;
    /** Range value used for catching all events. This range spans from year 1 to year 2999 */
    public static final int RANGE_CATCH_ALL = 99;
    
    /** The MySQL standard datetime format */
    public static final String MYSQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /** The collector parameter string. Constructed dynamically when fetching events. */
    private String collectorParam = null;
    
    /** List of dated ('normal') events. This list is populated when calling one of the getEvents() methods that accept excluded/undated folders lists as arguments. */
    private List datedEvents = null;
    /** List of undated events. This list is populated when calling one of the getEvents() methods that accept excluded/undated folders lists as arguments. */
    private List undatedEvents = null;
    /** List of excluded events. This list is populated when calling one of the getEvents() methods that accept excluded/undated folders lists as arguments. */
    private List excludedEvents = null;
    /** List of expired events. This list is populated when calling one of the getEvents() methods. */
    private List expiredEvents = null;
    
    /** The long value representing the start of the current range. Set when calling one of the getEvents() methods. */
    private long rangeStart = -1;
    /** The long value representing the end of the current range. Set when calling one of the getEvents() methods. */
    private long rangeEnd = -1;
    
    /**
     * Creates a new EventCalendar.
     */
    public EventCalendar() {
        super();
    }
    
    /**
     * Creates a new EventCalendar with the given TimeZone.<p>
     * 
     * @param tz The TimeZone to use
     */
    public EventCalendar(TimeZone tz) {
        super(tz);
    }
    
    /**
     * Copy constructor.<p>
     * 
     * @param ec The EventCalendar to copy
     */
    public EventCalendar(EventCalendar ec) {
        super();
        this.setTimeInMillis(ec.getTimeInMillis());
        this.datedEvents = ec.datedEvents;
        this.undatedEvents = ec.undatedEvents;
        this.excludedEvents = ec.excludedEvents;
        this.collectorParam = ec.collectorParam;
    }
    
    /*public List getTodaysEvents(CmsObject cmso, Locale locale) throws CmsException, SQLException {
        List events = new ArrayList();
        EventEntry event = null;
        
        SQLAgent sql = new SQLAgent();
        String start = this.get(Calendar.YEAR) + "-" + (this.get(Calendar.MONTH)+1) + "-" + this.get(Calendar.DATE) + " 23:59:59";
        String end = this.get(Calendar.YEAR) + "-" + (this.get(Calendar.MONTH)+1) + "-" + this.get(Calendar.DATE) + " 00:00:00";
        String sq = "SELECT * FROM event WHERE (`Begin` <= '" + start + "' AND `End` >= '" + end + "') OR " +
                    "(`Begin` BETWEEN '" + end + "' AND '" + start + "' AND `End` IS NULL)";
        java.sql.ResultSet rs = sql.doSelect(sq);
        CmsUUID sid;
        CmsResource r;
        A_CmsXmlDocument xmlDoc;
        String rDescription;
        String rTitle;
        String rStartTime;
        String rEndTime;
        String html;
            
        while (rs.next()) {
            sid = CmsUUID.valueOf(rs.getString("structure_id"));
            r = cmso.readResource(sid);
            if (cmso.readPropertyObject(r, "locale", true).getValue() != null) {
                if (cmso.readPropertyObject(r, "locale", true).getValue().equalsIgnoreCase(locale.toString())) {
                    try {
                        xmlDoc = CmsXmlContentFactory.unmarshal(cmso, cmso.readFile(r));
                        rDescription = xmlDoc.getValue("Description", locale).getStringValue(cmso);
                        rTitle = xmlDoc.getValue("Title", locale).getStringValue(cmso); 
                        rStartTime = rs.getString("begin").substring(0, 16);
                        rEndTime = rs.getString("end") != null ? (" - " + rs.getString("end").substring(0, 16)) : "";

                        // Construct the HTML
                        html = "<strong>" + rTitle + "</strong></br>" + rDescription + "<em>" + rStartTime + rEndTime + "</em>";

                        event = new EventEntry(rStartTime, rEndTime, rTitle, rDescription, r.getResourceId(), sid, locale);
                        event.setHtml(html);
                        events.add(new EventEntry(event));
                    } catch (NullPointerException npe) {
                        // Do nothing, assume this means that the resources are empty for the current locale
                        throw new NullPointerException("Could not create EventEntry " + (events.size()+1) + " for this date: " + npe.getMessage());
                    }
                }
            }
        }
        return events;
    }*/
    
    /*public List getTodaysEvents(CmsJspXmlContentBean cms, String eventsFolder, List categories, boolean sortDescending, int resultLimit) throws CmsException, IllegalArgumentException {
        return getEvents(RANGE_CURRENT_DATE, cms, eventsFolder, categories, sortDescending, resultLimit);
    }*/
    
    /**
     * Gets a collector parameter string that can be used to pass parameters
     * to the CmsTimeRangeCategoryCollector.<p>
     * 
     * @param start The time range start time
     * @param end The time range end time
     * @param eventsFolder The folder to collect resources from
     * @param categories The categories used to filter collected resources
     * @param sortDescending Sort ordering
     * @param resultLimit Maximum number of resources to collect
     * 
     * @return A parameter string that can be used to pass parameters to the CmsTimeRangeCategoryCollector
     */
    private String getCollectorParameterString(String start, 
                                                String end, 
                                                String eventsFolder, 
                                                List categories, 
                                                boolean excludeExpired, 
                                                boolean sortDescending, 
                                                boolean overlapLenient,
                                                boolean categoryInclusive,
                                                int resultLimit) {
        collectorParam = "resource=" + eventsFolder;
        collectorParam += "|resourceType=" + EventEntry.RESOURCE_TYPE_NAME_EVENT;
        collectorParam += "|timeStart=" + start;
        collectorParam += "|timeEnd=" + end;
        collectorParam += "|propertyTimeStart=" + EventEntry.PROPERTY_TIME_START;
        collectorParam += "|propertyTimeEnd=" + EventEntry.PROPERTY_TIME_END;
        collectorParam += "|excludeExpired=" + String.valueOf(excludeExpired);
        collectorParam += "|overlapLenient=" + String.valueOf(overlapLenient);
        collectorParam += "|categoryInclusive=" + String.valueOf(categoryInclusive);
        collectorParam += "|sortDescending=" + String.valueOf(sortDescending);
        collectorParam += "|resultLimit=" + (resultLimit == -1 ? String.valueOf(Integer.MAX_VALUE) : String.valueOf(resultLimit));
        if (categories != null) {
            Iterator itr = categories.iterator();
            Object obj = null;
            CmsCategory cat = null;
            String catName = null;
            if (itr.hasNext()) {
                collectorParam += "|propertyCategories=" + EventEntry.PROPERTY_CATEGORIES;
                collectorParam += "|categories=";
                while (itr.hasNext()) {
                    obj = itr.next();
                    if (obj instanceof CmsCategory) {
                        cat = (CmsCategory)obj;
                        //collectorParam += cat.getName();
                        collectorParam += cat.getRootPath();
                    }
                    else if (obj instanceof String) {
                        catName = (String)obj;
                        collectorParam += catName;
                    }
                    if (itr.hasNext()) {
                        collectorParam += ",";
                    }
                }
            }
        }
        return collectorParam;
    }
    
    /**
     * Collects events using the given collector parameters.<p>
     * 
     * @param cms An initialized action element
     * @param collectorParam The collector parameters
     * 
     * @return A list of EventEntry objects collected using the given parameters
     * 
     * @throws org.opencms.main.CmsException If something goes wrong when attempting to collect the events
     * 
     * @see #getCollectorParameterString(java.lang.String, java.lang.String, java.lang.String, java.util.List, boolean, int)
     * @see CmsTimeRangeCategoryCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String) 
     */
    protected List collectEvents(CmsJspActionElement cms, String collectorParam) throws CmsException {
        Locale locale = cms.getRequestContext().getLocale();
        
        SimpleDateFormat dateFormatDateTime = new SimpleDateFormat(cms.label("label.event.dateformat.datetime"), locale);
        SimpleDateFormat dateFormatDateOnly = new SimpleDateFormat(cms.label("label.event.dateformat.dateonly"), locale);
        
        
        CmsObject cmso = cms.getCmsObject();
        CmsResource r       = null;
        ArrayList events = new ArrayList();
        /*
        EventEntry event = null;
        
        String rDescription = "";
        String rTitle       = "";
        String html         = "";
        long rStartLong     = 0;
        long rEndLong       = 0;
        */
        
        //CmsTimeRangeCategoryCollector collector = new CmsTimeRangeCategoryCollector();
        TimeRangeCategoryEventCollector collector = new TimeRangeCategoryEventCollector();
        List result = collector.getResults(cmso, null, collectorParam);
        Iterator<CmsResource> itResults = result.iterator();
        try {
            while (itResults.hasNext()) {
                r = itResults.next();
                /*
                // Clear variables
                rDescription= "";
                rTitle      = "";
                html        = "";                
                rStartLong  = 0;
                rEndLong    = 0;
                
                CmsProperty titleProp       = cmso.readPropertyObject(r, CmsPropertyDefinition.PROPERTY_TITLE, false);
                CmsProperty descriptionProp = cmso.readPropertyObject(r, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false);
                CmsProperty startTimeProp   = cmso.readPropertyObject(r, EventEntry.PROPERTY_TIME_START, false);
                CmsProperty endTimeProp     = cmso.readPropertyObject(r, EventEntry.PROPERTY_TIME_END, false);
                CmsProperty timeDisplayProp = cmso.readPropertyObject(r, EventEntry.PROPERTY_TIME_DISPLAY, false);
                                
                if (!titleProp.isNullProperty()) {
                    rTitle = titleProp.getValue();
                }
                if (!descriptionProp.isNullProperty()) {
                    rDescription = descriptionProp.getValue();
                }
                if (!startTimeProp.isNullProperty()) {
                    rStartLong = Long.valueOf(startTimeProp.getValue());
                }
                if (!endTimeProp.isNullProperty()) {
                    rEndLong = Long.valueOf(endTimeProp.getValue());
                }

                event = new EventEntry(rStartLong, rEndLong, rTitle, rDescription, r.getResourceId(), r.getStructureId(), locale);
                                
                // Set the date format based on the user's selection
                SimpleDateFormat dateFormat = dateFormatDateTime;
                if (!timeDisplayProp.isNullProperty()) {
                    if (timeDisplayProp.getValue().equals(EventEntry.TIME_DISPLAY_DATEONLY)) {
                        dateFormat = dateFormatDateOnly;
                        event.setTimeDisplayMode(EventEntry.TIME_DISPLAY_DATEONLY);
                    }
                }
                event.setDateFormat(dateFormat);
                
                // Construct the HTML
                html = "<h4>" + rTitle + "</h4>" +
                        "<span class=\"event-time\">" + 
                            event.formatStartTime() + 
                            (event.formatEndTime() != null ? (" &ndash; " + event.formatEndTime()) : "") + 
                        "</span>";
                */
                
                EventEntry event = new EventEntry(cmso, r);
                event.setDateFormat(event.isDisplayDateOnly() ? dateFormatDateOnly : dateFormatDateTime);
                
                // Construct the HTML
                String html = "<h4>" + event.getTitle() + "</h4>" +
                        "<span class=\"event-time\">" + event.formatStartTime() + (event.formatEndTime() != null ? (" &ndash; " + event.formatEndTime()) : "") + "</span>";
                
                event.setHtml(html);
                events.add(event);
            }
        } catch (Exception jspe) {
            throw new NullPointerException(jspe.getMessage());
        }
        
        return events;
    }
    
    /**
     * Gets a list of events within a given standard range, using no excluded or 
     * undated folders.<p>
     * 
     * A setting of "overlap lenient" and "category inclusive" is used.<p>
     * 
     * @param range the range, one of RANGE_CURRENT_YEAR, RANGE_CURRENT_MONTH, RANGE_CURRENT_DATE, RANGE_CURRENT_WEEK or RANGE_CATCH_ALL
     * @param cms Initialized action element
     * @param eventsFolder The folder to list events from. If set to null, cms.getRequestContext().getFolderUri() will be used.
     * @param categories List of categories to filter events by. Can be null.
     * @param sortDescending Sort order
     * @param resultLimit Result limit. Set to -1 for no limit.
     * 
     * @return A list of EventEntry objects that fit the given criteria
     * 
     * @throws org.opencms.main.CmsException
     * @throws java.lang.IllegalArgumentException
     */
    public List getEvents(int range, CmsJspActionElement cms, 
                            String eventsFolder, List categories, 
                            boolean sortDescending, int resultLimit) throws CmsException, IllegalArgumentException {
        
        return this.getEvents(range, cms, eventsFolder, null, null, categories, false, sortDescending, resultLimit);
    }
    
    /**
     * Gets a list of events within a specified interval, using no excluded or 
     * undated folders.<p>
     * 
     * A setting of "overlap lenient" and "category inclusive" is used.<p>
     * 
     * @param start the long representation of the time interval start
     * @param end the long representation of the time interval end
     * @param cms Initialized action element
     * @param eventsFolder The folder to list events from. If set to null, cms.getRequestContext().getFolderUri() will be used.
     * @param categories List of categories to filter events by. Can be null.
     * @param sortDescending Sort order
     * @param resultLimit Result limit. Set to -1 for no limit.
     * 
     * @return A list of EventEntry objects that fit the given criteria
     * 
     * @throws org.opencms.main.CmsException
     * @throws java.lang.IllegalArgumentException
     */
    public List getEvents(long start, long end, CmsJspActionElement cms, String eventsFolder, List categories, 
                            boolean sortDescending, int resultLimit) throws CmsException, IllegalArgumentException {
        
        return this.getEvents(start, end, cms, eventsFolder, null, null, categories, false, sortDescending, resultLimit);
    }
    
    /**
     * Gets a list of events within a given range. Additionally, this 
     * method populates the class member lists datedEvents, undatedEvents and 
     * excludedEvents, so that these may be easily fetched.<p>
     * 
     * A setting of "overlap lenient" and "category inclusive" is used.<p>
     * 
     * @param start The long representation of the time interval start
     * @param end The long representation of the time interval end
     * @param cms Initialized action element
     * @param eventsFolder The folder to list events from. If set to null, cms.getRequestContext().getFolderUri() will be used.
     * @param undatedFolders List of folders flagged as containing undated events. Can be null.
     * @param excludedFolders List of folders flagged as containing excluded events. Can be null.
     * @param categories List of categories to filter events by. Can be null.
     * @param sortDescending Sort order
     * @param resultLimit Result limit. Set to -1 for no limit.
     * 
     * @see #getEvents(long, long, org.opencms.jsp.CmsJspActionElement, java.lang.String, java.util.List, java.util.List, java.util.List, boolean, boolean, boolean, boolean, int) 
     * 
     * @return a list containing all dated and undated events, but not including excluded events.
     * 
     * @throws org.opencms.main.CmsException
     * @throws java.lang.IllegalArgumentException
     */
    public List getEvents(long start, 
                            long end, 
                            CmsJspActionElement cms, 
                            String eventsFolder, 
                            List undatedFolders, 
                            List excludedFolders,
                            List categories, 
                            boolean excludeExpired, 
                            boolean sortDescending, 
                            int resultLimit) throws CmsException, IllegalArgumentException {
        return getEvents(start,end,cms,eventsFolder,undatedFolders,excludedFolders,categories,excludeExpired,sortDescending,true,false,resultLimit);
    }
    
    /**
     * Gets a list of events within a given standard range. Additionally, this 
     * method populates the class member lists datedEvents, undatedEvents and 
     * excludedEvents, so that these may be easily fetched.<p>
     * 
     * @param start The long representation of the time interval start
     * @param end The long representation of the time interval end
     * @param cms Initialized action element
     * @param eventsFolder The folder to list events from. If set to null, cms.getRequestContext().getFolderUri() will be used.
     * @param undatedFolders List of folders flagged as containing undated events. Can be null.
     * @param excludedFolders List of folders flagged as containing excluded events. Can be null.
     * @param categories List of categories to filter events by. Can be null.
     * @param sortDescending Sort order
     * @param overlapLenient The "overlap leniency" setting (if events that only partially overlaps the time range should be collected)
     * @param categoryInclusive The "category inclusive" setting (if multiple categories should be treated as exclusive or inclusive)
     * @param resultLimit Result limit. Set to -1 for no limit.
     * 
     * @return a list containing all dated and undated events, but not including excluded events.
     * 
     * @throws org.opencms.main.CmsException
     * @throws java.lang.IllegalArgumentException
     */
    public List getEvents(long start, 
                            long end, 
                            CmsJspActionElement cms, 
                            String eventsFolder, 
                            List undatedFolders, 
                            List excludedFolders,
                            List categories, 
                            boolean excludeExpired, 
                            boolean sortDescending, 
                            boolean overlapLenient,
                            boolean categoryInclusive,
                            int resultLimit) throws CmsException, IllegalArgumentException {
        SimpleDateFormat mySqlDateFormat = new SimpleDateFormat(MYSQL_DATETIME_FORMAT);
        
        Date timeRangeStart = null;
        Date timeRangeEnd = null;
        
        // Set start/end based on the "range" arguments
        try {
            this.rangeStart = start;
            this.rangeEnd = end;
            timeRangeStart = new Date(start);
            timeRangeEnd = new Date(end);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to translate a range parameter to a date: " + e.getMessage());
        }

        // Construct the parameter string
        collectorParam = getCollectorParameterString(mySqlDateFormat.format(timeRangeStart), mySqlDateFormat.format(timeRangeEnd), 
                                                                (eventsFolder != null ? eventsFolder : cms.getRequestContext().getFolderUri()), 
                                                                categories, excludeExpired, sortDescending, overlapLenient, categoryInclusive, resultLimit);
        
        List allEvents = this.collectEvents(cms, collectorParam);
        this.createSeparateEventLists(allEvents, excludedFolders, undatedFolders, cms.getCmsObject());
        allEvents.removeAll(excludedEvents);
        return allEvents;
    }
    
    /**
     * Test method
     * @param start
     * @param end
     * @param cms
     * @param eventsFolder
     * @param undatedFolders
     * @param excludedFolders
     * @param categories
     * @param excludeExpired
     * @param sortDescending
     * @param overlapLenient
     * @param categoryInclusive
     * @param includeRecurrences
     * @param resultLimit
     * @return
     * @throws CmsException
     * @throws IllegalArgumentException 
     */
    /*public List getEventsInRange(long start, 
                            long end, 
                            CmsJspActionElement cms, 
                            String eventsFolder, 
                            List undatedFolders, 
                            List excludedFolders,
                            List categories, 
                            boolean excludeExpired, 
                            boolean sortDescending, 
                            boolean overlapLenient,
                            boolean categoryInclusive,
                            boolean includeRecurrences,
                            int resultLimit) throws CmsException, IllegalArgumentException {
        List events = ;
        //List originalEvents = getEvents(start, end, cms, eventsFolder, undatedFolders, excludedFolders, categories, excludeExpired, sortDescending, overlapLenient, categoryInclusive, resultLimit);
        return events;
        
    }*/
    
    /**
     * Gets a list of events within a given standard range.<p>
     * 
     * A setting of "overlap lenient" and "category inclusive" is used.<p>
     * 
     * @param range the range, one of RANGE_CURRENT_YEAR, RANGE_CURRENT_MONTH, RANGE_CURRENT_DATE, RANGE_CURRENT_WEEK or RANGE_CATCH_ALL
     * @param cms Initialized action element
     * @param eventsFolder The folder to list events from. If set to null, cms.getRequestContext().getFolderUri() will be used.
     * @param undatedFolders List of folders flagged as containing undated events. Can be null.
     * @param excludedFolders List of folders flagged as containing excluded events. Can be null.
     * @param categories List of categories to filter events by. Can be null.
     * @param sortDescending Sort order
     * @param resultLimit Result limit. Set to -1 for no limit.
     * 
     * @return A list of EventEntry objects that fit the given criteria
     * 
     * @throws org.opencms.main.CmsException
     * @throws java.lang.IllegalArgumentException
     */
    public List getEvents(int range, CmsJspActionElement cms,  
                            String eventsFolder, List undatedFolders, List excludedFolders, List categories, 
                            boolean excludeExpired, boolean sortDescending,
                            int resultLimit) 
                            throws CmsException, IllegalArgumentException {
        return getEvents(range, cms, eventsFolder, undatedFolders, excludedFolders, categories, excludeExpired, sortDescending, true, true, resultLimit);
    }
    
    /**
     * Gets a list of events within a given standard range.<p>
     * 
     * @param range the range, one of RANGE_CURRENT_YEAR, RANGE_CURRENT_MONTH, RANGE_CURRENT_DATE, RANGE_CURRENT_WEEK or RANGE_CATCH_ALL
     * @param cms Initialized action element
     * @param eventsFolder The folder to list events from. If set to null, cms.getRequestContext().getFolderUri() will be used.
     * @param undatedFolders List of folders flagged as containing undated events. Can be null.
     * @param excludedFolders List of folders flagged as containing excluded events. Can be null.
     * @param categories List of categories to filter events by. Can be null.
     * @param sortDescending Sort order
     * @param overlapLenient The "overlap leniency" setting (if events that only partially overlaps the time range should be collected)
     * @param categoryInclusive The "category inclusive" setting (if multiple categories should be treated as exclusive or inclusive)
     * @param resultLimit Result limit. Set to -1 for no limit.
     * 
     * @return A list of EventEntry objects that fit the given criteria
     * 
     * @throws org.opencms.main.CmsException
     * @throws java.lang.IllegalArgumentException
     */
    public List getEvents(int range, CmsJspActionElement cms,  
                            String eventsFolder, List undatedFolders, List excludedFolders, List categories, 
                            boolean excludeExpired, boolean sortDescending, boolean overlapLenient, boolean categoryInclusive,
                            int resultLimit) 
                            throws CmsException, IllegalArgumentException {
        String start = null;
        String end = null;
        
        // Set start/end based on the argument "range"
        if (range == RANGE_CURRENT_DATE) {
            start = getYearMonthDateString(this) + " 00:00:00";
            end = getYearMonthDateString(this) + " 23:59:59";
            /*
            start = this.get(Calendar.YEAR) + "-" + (this.get(Calendar.MONTH)+1) + "-" + this.get(Calendar.DATE) + " 00:00:00";
            end = this.get(Calendar.YEAR) + "-" + (this.get(Calendar.MONTH)+1) + "-" + this.get(Calendar.DATE) + " 23:59:59";
            */
        } 
        else if (range == RANGE_CURRENT_MONTH) {
            start = this.get(Calendar.YEAR) + "-" + this.getNormalizedMonth() + "-01" + " 00:00:00";
            end = this.get(Calendar.YEAR) + "-" + this.getNormalizedMonth() + "-" + this.getActualMaximum(Calendar.DATE) + " 23:59:59";
        }
        else if (range == RANGE_CURRENT_YEAR) {
            start = this.get(Calendar.YEAR) +   "-01-01" + " 00:00:00";
            end = this.get(Calendar.YEAR) +     "-12-31" + " 23:59:59";
        }
        else if (range == RANGE_CURRENT_WEEK) {
            Date beforeWarp = this.getTime();
            
            this.timeWarp(WARP_FIRST_DAY_OF_WEEK);
            //start = this.get(Calendar.YEAR) + "-" + (this.get(Calendar.MONTH)+1) + "-" + this.get(Calendar.DATE) + " 00:00:00";
            start = getYearMonthDateString(this) + " 00:00:00";
            this.add(Calendar.WEEK_OF_YEAR, 1);
            this.add(Calendar.DATE, -1);
            end = getYearMonthDateString(this) + " 23:59:59";
            //end = this.get(Calendar.YEAR) + "-" + (this.get(Calendar.MONTH)+1) + "-" + (this.get(Calendar.DATE)-1) + " 23:59:59";
            
            this.setTime(beforeWarp);
        }
        else if (range == RANGE_UPCOMING_AND_IN_PROGRESS) {
            //Date now = this.getTime();
            start = getTimestampString(this);
            /*
            start = this.get(Calendar.YEAR) + "-" + (this.get(Calendar.MONTH)+1) + "-" + this.get(Calendar.DATE) + " " 
                    + getClockTimeString(this.get(Calendar.HOUR_OF_DAY), this.get(Calendar.MINUTE), this.get(Calendar.SECOND));
            */
                    /*+ (this.get(Calendar.HOUR_OF_DAY) < 10 ? "0"+this.get(Calendar.HOUR_OF_DAY) : this.get(Calendar.HOUR_OF_DAY))
                    + ":" + (this.get(Calendar.MINUTE) < 10 ? "0"+this.get(Calendar.MINUTE) : this.get(Calendar.MINUTE)) 
                    + ":" + (this.get(Calendar.SECOND) < 10 ? "0"+this.get(Calendar.SECOND) : this.get(Calendar.SECOND));*/
            end = "2999-12-31 23:59:59";
        }
        else if (range == RANGE_CATCH_ALL_EXPIRED) {
            Calendar nowCal = new GregorianCalendar();
            nowCal.setTime(new Date());
            start = "0001-01-01 00:00:00";
            end = getTimestampString(nowCal);
            /*
            end = nowCal.get(Calendar.YEAR) + "-" + (nowCal.get(Calendar.MONTH)+1) + "-" + nowCal.get(Calendar.DATE) + " "
                    + getClockTimeString(nowCal.get(Calendar.HOUR_OF_DAY), nowCal.get(Calendar.MINUTE), nowCal.get(Calendar.SECOND));
            */
                    /*+ (nowCal.get(Calendar.HOUR_OF_DAY) < 10 ? "0"+nowCal.get(Calendar.HOUR_OF_DAY) : nowCal.get(Calendar.HOUR_OF_DAY))
                    + ":" + (nowCal.get(Calendar.MINUTE) < 10 ? "0"+nowCal.get(Calendar.MINUTE) : nowCal.get(Calendar.MINUTE)) 
                    + ":" + (nowCal.get(Calendar.SECOND) < 10 ? "0"+nowCal.get(Calendar.SECOND) : nowCal.get(Calendar.SECOND));*/
        }
        else if (range == RANGE_CATCH_ALL) {
            start = "0001-01-01 00:00:00";
            end = "2999-12-31 23:59:59";
        }
        
        try {
            SimpleDateFormat tempDateFormat = new SimpleDateFormat(MYSQL_DATETIME_FORMAT);
            this.rangeStart = tempDateFormat.parse(start).getTime();
            this.rangeEnd = tempDateFormat.parse(end).getTime();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse start/end of time range: " + e.getMessage());
        }

        collectorParam = getCollectorParameterString(start, end, (eventsFolder != null ? eventsFolder : cms.getRequestContext().getFolderUri()), 
                                                                categories, excludeExpired, sortDescending, overlapLenient, categoryInclusive, resultLimit);
        
        List allEvents = this.collectEvents(cms, collectorParam); // Get a list of ALL events
        this.createSeparateEventLists(allEvents, excludedFolders, undatedFolders, cms.getCmsObject());
        allEvents.removeAll(excludedEvents); // Remove the excluded events. 
        return allEvents; // Return dated and undated (if any) events
    }
    
    /**
     * Gets the "normal" numeric month; "01" (January) to "12" (December). 
     * <p>
     * Month numbers 1-9 are prefixed with a zero.
     * 
     * @return  the "normal" numeric month, prefixed (if needed) with a zero.
     * @see #toZeroPrefixed(int) 
     */
    public String getNormalizedMonth() {
        int month = this.get(Calendar.MONTH)+1; // Zero-based, so add 1
        return toZeroPrefixed(month);
    }
    
    /**
     * Gets the "normal" numeric date; "01" to "31". 
     * <p>
     * Dates in the range 1-9 are prefixed with a zero.
     * 
     * @return  the "normal" numeric date, prefixed (if needed) with a zero.
     * @see #toZeroPrefixed(int) 
     */
    public String getNormalizedDate() {
        int date = this.get(Calendar.DATE);
        return toZeroPrefixed(date);
    }
    
    /**
     * Gets a string representing the given value, prefixed with a zero, if its 
     * in the range 0-9.
     * <p>
     * If the given value is larger than 9 or less than zero, it is returned 
     * unmodified.
     * 
     * @param value The field, e.g. {@link Calendar#MONTH}.
     * @return The field value, prefixed (if needed) with a zero to make it a 2-digit number.
     */
    public static String toZeroPrefixed(int value) {
        if (value >= 0 && value <= 9) {
            return "0".concat(String.valueOf(value));
        }
        return String.valueOf(value);
    }
    
    /**
     * Gets the clock time as a string, using the given values.
     * 
     * @param hour The hour of day (as returned by Calendar#get(Calendar#HOUR_OF_DAY).
     * @param min The minute (as returned by Calendar#get(Calendar#MINUTE).
     * @param sec The second (as returned by Calendar#get(Calendar#SECOND).
     * 
     * @return The clock time.
     */
    protected String getClockTimeString(int hour, int min, int sec) {
        return ""
                + toZeroPrefixed(hour) + ":"
                + toZeroPrefixed(min) + ":"
                + toZeroPrefixed(sec);
    }
    
    /**
     * Gets the clock time as a String, using the given Calendar.
     * 
     * @param cal The Calendar to use when constructing the clock time String.
     * 
     * @return The clock time (using the 24-hour clock), in a <code>HH:mm:ss</code> format. 
     */
    protected String getClockTimeString(Calendar cal) {
        return getClockTimeString(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
    }
    
    /**
     * Gets the year, month and date as a String, using the given Calendar.
     * 
     * @param cal The calendar to use when constructing the string.
     * @return  the year, month and date, in a <code>yyyy-MM-dd</code> format.
     */
    protected String getYearMonthDateString(Calendar cal) {
        return ""
                + cal.get(Calendar.YEAR) + "-"
                + toZeroPrefixed(cal.get(Calendar.MONTH)+1) + "-"
                + toZeroPrefixed(cal.get(Calendar.DATE));
    }
    
    /**
     * Gets a timestamp as a String, using the given Calendar.
     * 
     * @param cal The Calendar to use when constructing the timestamp.
     * 
     * @return The timestamp.
     */
    protected String getTimestampString(Calendar cal) {
        return ""
                + getYearMonthDateString(cal) 
                + " "
                + getClockTimeString(cal);
                /*
                + cal.get(Calendar.YEAR) + "-"
                + (cal.get(Calendar.MONTH)+1) + "-"
                + cal.get(Calendar.DATE) + " "                
                + getClockTimeString(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
                */
    }
    
    /**
     * Gets the number of days that has transpired since the first day of the week.
     * This value is always zero or negative , and always in the range 0 (current 
     * day is first day of week) to -6 (current is last day of week), inclusive.<p>
     * 
     * @return days since the first day of the week (range -6 to 0, inclusive).
     */
    public int getDaysSinceWeekStarted() {
        //return this.getFirstDayOfWeek() - this.get(DAY_OF_WEEK);
        int daysSinceWeekStart = this.getFirstDayOfWeek() - this.get(DAY_OF_WEEK);
        if (this.getFirstDayOfWeek() != SUNDAY && this.get(DAY_OF_WEEK) == SUNDAY) {
            daysSinceWeekStart += -7;
        }
        return daysSinceWeekStart;
        /*
        int daysSinceWeekStart = 0;
        if (this.get(DAY_OF_WEEK) != this.getFirstDayOfWeek()) { // If today is not the first day of the week...
            daysSinceWeekStart = this.getFirstDayOfWeek() - this.get(DAY_OF_WEEK);
        }
        return daysSinceWeekStart;
        */ 
    }
    
    /**
     * Gets the number of days to the first day of the previous week. This is 
     * always a negative number.<p>
     * 
     * Calculation is done like this: Step back 7 days (one week). If that day is
     * not the first day of the week, step back to the first day of the week.<p>
     * 
     * <em>The returned value can be used as x in EventCalendar.add(Calendar.DATE, x) 
     * to set the calendar to the first day of the last week.</em>
     * 
     * @return the number of days to the start of the previous week.
     */
    public int getDaysSincePreviousWeekStarted() {
        //this.add(WEEK_OF_YEAR, -1); // Step back one week
        //int daysSinceWeekStart = getDaysSinceWeekStart();
        //this.add(DATE, daysSinceWeekStart*-1); // Step back to the first day of that week (if neccesary)
        //this.get(DAY_OF_WEEK);
        /*
        if (this.get(Calendar.DAY_OF_WEEK) != this.getFirstDayOfWeek()) { // If today is not the first day of the week...
            daysToPreviousWeekStart = this.getFirstDayOfWeek() - this.get(Calendar.DAY_OF_WEEK);
            this.add(Calendar.DATE, daysToPreviousWeekStart); // ...go back to the first day of the week
        }
        */
        // Reset
        //this.add(WEEK_OF_YEAR, 1); // Step forward again
        //this.add(DATE, daysSinceWeekStart);
        //this.get(DAY_OF_WEEK);
        //this.add(DATE, daysToPreviousWeekStart * -1);
        
        return getDaysSinceWeekStarted() - 7;
    }
    
    /**
     * Ka-zzzzzooooom - Timewarp!<p>
     * 
     * The returned number (an int) will depend on the type of warp, e.g.:
     * <ul>
     * <li>"back one month" will return -1 because Calendar.add(MONTH, -1) was invoked</li>
     * <li>"first day of previous week" will return at least -7 because Calendar.add(DATE, -7-[days passed since week start]) was invoked.</li>
     * </ul>
     * 
     * @param jump the type of warp. Use one of the constant fields in EventCalendar
     * 
     * @return the factor that was added using Calendar.add(). 
     */
    public int timeWarp(int jump) {
        int factor = 0;
        switch (jump) {
            case WARP_NEXT_MONTH:
                factor = 1;
                this.add(Calendar.MONTH, factor);
                break;
            case WARP_PREV_MONTH:
                factor = -1;
                this.add(Calendar.MONTH, factor);
                break;
            case WARP_FIRST_DAY_OF_PREV_WEEK:
                factor = getDaysSinceWeekStarted() - 7;
                this.add(Calendar.DATE, factor);
                break;
            case WARP_FIRST_DAY_OF_WEEK:
                factor = getDaysSinceWeekStarted();
                this.add(Calendar.DATE, factor);
                break;
            default:
                break;
        }
        return factor;
    }
    
    /**
     * Determine whether or not this Calendar represents today.<p>
     * 
     * @return true if this Calendar represents today, false if not.
     */
    public boolean representsToday() {
        GregorianCalendar currentDayCalendar = new GregorianCalendar(this.getTimeZone());
        currentDayCalendar.setTime(new Date());
        return (currentDayCalendar.get(ERA) == this.get(ERA) &&
                currentDayCalendar.get(YEAR) == this.get(YEAR) &&
                currentDayCalendar.get(MONTH) == this.get(MONTH) &&
                currentDayCalendar.get(DATE) == this.get(DATE));
    }
    
    /**
     * Get the numbers defining the date after the current date of this calendar.<p>
     * 
     * @return the numbers defining the next date, with index 0,1,2 = date,month,year (vales represented as returned by Calendar.get(Calendar.XXX))
     */
    public int[] getNextDate() {
        // Keep the current values
        int[] tempDate = { this.get(Calendar.DATE), 
                        this.get(Calendar.MONTH),
                        this.get(Calendar.YEAR) };
        
        this.add(Calendar.DATE, 1);
        int[] returnDate = { this.get(Calendar.DATE),
                        this.get(Calendar.MONTH),
                        this.get(Calendar.YEAR) };
        // Reset
        this.set(Calendar.YEAR, tempDate[2]);
        this.set(Calendar.MONTH, tempDate[1]);
        this.set(Calendar.DATE, tempDate[0]);
        
        return returnDate;
    }
    
    
    
    /**
     * Get the numbers defining the current date of this calendar.<p>
     * 
     * @return the numbers defining the current date, with index 0,1,2 = date,month,year (values as returned by Calendar.get(Calendar.XXX))
     */
    public int[] getCurrentDate() {
        int[] returnDate = { this.get(Calendar.DATE),
                        this.get(Calendar.MONTH),
                        this.get(Calendar.YEAR) };
        
        return returnDate;
    }
    
    /**
     * Get the numbers defining the date before the current date of this calendar.<p>
     * 
     * @return the numbers defining the prevous date, with index 0,1,2 = date,month,year (representation as in Calendar)
     */
    public int[] getPreviousDate() {
        // Keep the current values
        int[] tempDate = { this.get(Calendar.DATE), 
                        this.get(Calendar.MONTH),
                        this.get(Calendar.YEAR) };
        
        this.add(Calendar.DATE, -1);
        int[] returnDate = { this.get(Calendar.DATE),
                        this.get(Calendar.MONTH),
                        this.get(Calendar.YEAR) };
        // Reset
        this.set(Calendar.YEAR, tempDate[2]);
        this.set(Calendar.MONTH, tempDate[1]);
        this.set(Calendar.DATE, tempDate[0]);
        
        return returnDate;
    }
    
    /**
     * Convenience method. Returns common URL-type parameter strings.<p>
     * 
     * @param parameterType the parameter type that should be generated, one of this class' own constants PARAMETER_TYPE_xxxx
     * 
     * @return A parameter string that can be added as URL parameters
     */
    public String getParameterString(int parameterType) {
        String param = "";
        int[] date;
        switch (parameterType) {
            case PARAMETER_TYPE_NEXT_DATE:
                date = getNextDate();
                param = "y=" + date[2] + "&amp;m=" + date[1] + "&amp;d=" + date[0];
                break;
            case PARAMETER_TYPE_CURRENT_DATE:
                date = getCurrentDate();
                param = "y=" + date[2] + "&amp;m=" + date[1] + "&amp;d=" + date[0];
                break;
            case PARAMETER_TYPE_PREV_DATE:
                date = getPreviousDate();
                param = "y=" + date[2] + "&amp;m=" + date[1] + "&amp;d=" + date[0];
                break;
            case PARAMETER_TYPE_NEXT_MONTH:
                timeWarp(WARP_NEXT_MONTH);
                param = "y=" + this.get(Calendar.YEAR) + "&amp;m=" + this.get(Calendar.MONTH);
                timeWarp(WARP_PREV_MONTH);
                break;
            case PARAMETER_TYPE_CURRENT_MONTH:
                param = "y=" + this.get(Calendar.YEAR) + "&amp;m=" + this.get(Calendar.MONTH);
                break;
            case PARAMETER_TYPE_PREV_MONTH:
                timeWarp(WARP_PREV_MONTH);
                param = "y=" + this.get(Calendar.YEAR) + "&amp;m=" + this.get(Calendar.MONTH);
                timeWarp(WARP_NEXT_MONTH);
                break;
            case PARAMETER_TYPE_NEXT_YEAR:
                param = "y=" + (this.get(Calendar.YEAR) + 1);
                break;
            case PARAMETER_TYPE_CURRENT_YEAR:
                param = "y=" + (this.get(Calendar.YEAR));
                break;
            case PARAMETER_TYPE_PREV_YEAR:
                param = "y=" + (this.get(Calendar.YEAR) - 1);
                break;
            default:
                break;
        }
        return param;
    }
    
    /**
     * Creates these separate event lists:
     * <ul>
     *  <li>datedEvents</li>
     *  <li>undatedEvents (will be empty)</li>
     *  <li>excludedEvents (will be empty)</li>
     *  <li>expiredEvents</li>
     * </ul>.
     * 
     * The lists can then be easily fetched without re-generating them.<p>
     * 
     * This is a convenience method, equal to calling createSeparateEventLists(List, null, null, CmsObject).
     * 
     * @param allEvents List of all events
     * @param cmso Initialized CmsObject
     * 
     * @throws org.opencms.main.CmsException if the event file cannot be read
     */
    protected void createSeparateEventLists(List allEvents, CmsObject cmso) throws CmsException {
        createSeparateEventLists(allEvents, null, null, cmso);
    }
    
    /**
     * Creates three separate event lists:
     * <ul>
     *  <li>datedEvents</li>
     *  <li>undatedEvents</li>
     *  <li>excludedEvents</li>
     *  <li>expiredEvents</li>
     * </ul>.
     * 
     * These lists can then be easily fetched without re-generating them.<p>
     * 
     * @param allEvents List of all events
     * @param excludedFolders List of excluded folders (can be null)
     * @param undatedFolders List of undated folders (can be null)
     * @param cmso Initialized CmsObject
     * 
     * @throws org.opencms.main.CmsException if the event file cannot be read
     */
    protected void createSeparateEventLists(List allEvents, List excludedFolders, List undatedFolders, CmsObject cmso) throws CmsException {
        this.datedEvents = new ArrayList<EventEntry>();
        this.undatedEvents = new ArrayList<EventEntry>();
        this.excludedEvents = new ArrayList<EventEntry>();
        this.expiredEvents = new ArrayList<EventEntry>();
        
        // Prevent NPEs: make any null lists empty ones instead
        if (excludedFolders == null)
            excludedFolders = new ArrayList<String>(0);
        if (undatedFolders == null)
            undatedFolders = new ArrayList<String>(0);
           
        
        Iterator iAll = allEvents.iterator();
        while (iAll.hasNext()) {
            EventEntry event = (EventEntry)iAll.next();
            
            // If the event has expired (relative to "now"), add it to the expired list
            if (event.isExpired())
                expiredEvents.add(event);
            
            String eventTreeFolder = CmsResource.getParentFolder(cmso.getSitePath(cmso.readResource(event.getStructureId())));
            // Deconstruct the sub tree of the event stepwise, and check if any of its parents folders is flagged as excluded or undated
            while (!eventTreeFolder.equals("/")) {
                if (excludedFolders.contains(eventTreeFolder)) {
                    // The event's path is in the sub-tree of a folder flagged as containing excluded events
                    excludedEvents.add(event); // Add the event to the list of excluded events
                    iAll.remove(); // Remove the event from the list of all events
                    break;
                } 
                else if (undatedFolders.contains(eventTreeFolder)) {
                    // The event's path is in the sub-tree of a folder flagged as containing undated events
                    undatedEvents.add(event);
                    iAll.remove();
                    break;
                }
                eventTreeFolder = CmsResource.getParentFolder(eventTreeFolder);
            }
        }
        
        datedEvents.addAll(allEvents);
    }
    
    /** 
     * Returns all currently collected events that are flagged as normal -- that is: not undated and not excluded.<p>
     * 
     * <em>NB: This list is unpopulated before any of the getEvents()-methods have been invoked.</em>
     * 
     * @return A list of EventEntry objects.
     */
    public List<EventEntry> getDatedEvents() { return this.datedEvents; }
    /** 
     * Returns all currently collected events that are flagged as undated.<p>
     * 
     * <em>NB: This list is unpopulated before any of the getEvents()-methods have been invoked.</em>
     * 
     * @return A list of EventEntry objects.
     */
    public List<EventEntry> getUndatedEvents() { return this.undatedEvents; }
    /** 
     * Returns all currently collected events that are flagged as excluded.<p>
     * 
     * <em>NB: This list is unpopulated before any of the getEvents()-methods have been invoked.</em>
     * 
     * @return A list of EventEntry objects.
     */
    public List<EventEntry> getExcludedEvents() { return this.excludedEvents; }
    /** 
     * Returns all currently collected events that are not excluded.<p>
     * 
     * <em>NB: This list is unpopulated before any of the getEvents()-methods have been invoked. </em>
     * 
     * @return A list of EventEntry objects.
     */
    public List<EventEntry> getNonExcludedEvents() { 
        List events = new ArrayList<EventEntry>(datedEvents);
        events.addAll(undatedEvents);
        return events;
    }
    /** 
     * Returns all events currently collected as expired events (events that have finished before "now").<p>
     * 
     * NB: This list is unpopulated before any of the getEvents()-methods have been invoked.
     * 
     * @return A list of EventEntry objects.
     */
    public List<EventEntry> getExpiredEvents() { return expiredEvents; }
    
    /**
     * Gets the long representation of the start of the current time range. 
     * The time range is set by calling one of the getEvents()-methods. If no 
     * time range has been set, -1 is returned. Otherwise, the returned value will
     * always be greater than zero.
     * @return the long representation of the start of the current time range.
     */
    public long getRangeStart() {
        return this.rangeStart;
    }
    
    /**
     * Gets the long representation of the end of the current time range. 
     * The time range is set by calling one of the getEvents()-methods. If no 
     * time range has been set, -1 is returned. Otherwise, the returned value will
     * always be greater than zero.
     * @return the long representation of the end of the current time range.
     */
    public long getRangeEnd() {
        return this.rangeEnd;
    }
}