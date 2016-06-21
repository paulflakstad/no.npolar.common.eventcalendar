package no.npolar.common.eventcalendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsCategory;

/**
 * Collects events based on a set of customizable settings.
 * <p>
 * Settings include:
 * <ul>
 *  <li>Category filtering (and match mode options)</li>
 *  <li>Sort order (descending or ascending)</li>
 *  <li>Exclude folders if necessary</li>
 * </ul>
 * <p>
 * ToDo: <br/>
 * 1: Introduce offset, so that we can use get(range, limit, offset), a
 *      prerequisite for implementing "load more" on the presentation layer.<br/>
 * 2: Rename some of the setter methods - setSortMode(boolean) etc. doesn't make
 *      much sense.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute <flakstad at npolar.no>
 */
public class EventsCollector {
    
    /**
     * Settings for this collector.
     */
    public class EventsCollectorSettings {
        /** The folder to collect from. */
        private String folder = null;
        /** Include recurrences? */
        private boolean includeEventRecurrences = true;
        /** Include events that only partially overlap? */
        private boolean includeEventsThatOverlap = true;
        /** Include undated events? */
        private boolean includeEventsWithNoDate = false;
        /** Include expired events? */
        private boolean includeExpired = false;
        /** Sort descending? */
        private boolean sortDescending = true;
        /** Use {@link ResourceCategoriesFilter#MODE_EXCLUSIVE} category filtering? */
        private boolean categoriesMustAllMatch = true;
        /** Holds URIs to excluded folders (if any). */
        private List<String> foldersExcluded = null;
        //** Holds URIs to folders containing undated events (if any). */
        //private List<String> foldersUndated = null;
        /** Holds categories to match on events. */
        private List<CmsCategory> categories = null;
        
        /** 
         * Creates a new settings instance with default values.
         */
        private EventsCollectorSettings() {
            foldersExcluded = new ArrayList<String>();
            //foldersUndated = new ArrayList<String>();
            categories = new ArrayList<CmsCategory>();
        }
    }
    
    //** Range value used for catching all events inside the current year */
    //public static final int RANGE_YEAR = 0;
    //** Range value used for catching all events inside the current month */
    //public static final int RANGE_MONTH = 1;
    //** Range value used for catching all events inside the current date */
    //public static final int RANGE_DATE = 2;
    //** Range value used for catching all events inside the current week */
    //public static final int RANGE_WEEK = 3;
    //** Range value used for catching all upcoming and in-progress events */
    //public static final int RANGE_UPCOMING_AND_IN_PROGRESS = 4;
    //** Range value used for catching all expired events. This range spans from year 1 to now */
    //public static final int RANGE_CATCH_ALL_EXPIRED = 98;
    //** Range value used for catching all events. This range spans from year 1 to year 2999 */
    //public static final int RANGE_CATCH_ALL = 99;
    
    /** Holds the collector settings. */
    private EventsCollectorSettings settings = null;
    
    private int lastResultsTotal = -1;
    private Map<String, Integer> lastResultCategories = null;
    
    /*public final int CATEGORY_MATCH_MODE_EXCLUSIVE = 0;
    public final int CATEGORY_MATCH_MODE_INCLUSIVE = 1;
    
    public final int HANDLING_MODE_EXCLUDE = 0;
    public final int HANDLING_MODE_INCLUDE = 1;
    
    public final int OVERLAP_MODE_NON_LENIENT = 0;
    public final int OVERLAP_MODE_LENIENT = 1;*/
    
    private CmsJspActionElement cms = null;
    
    /**
     * Creates a new collector with default settings.
     * <p>
     * Add any custom settings (categories to match, excluded folders, sort 
     * order etc.) before calling {@link #get(org.opencms.jsp.CmsJspActionElement, java.lang.String, int, long, long)}.
     */
    /*public EventsCollector() {
        this.settings = new EventsCollectorSettings();
    }*/
    
    /**
     * Creates a new collector with default settings.
     * <p>
     * Add any custom settings (categories to match, excluded folders, sort 
     * order etc.) before calling {@link #get(org.opencms.jsp.CmsJspActionElement, java.lang.String, int, long, long)}.
     * 
     * @param cms Action element, needed to read the event files.
     * @param folder The root folder of the events to collect.
     */
    public EventsCollector(CmsJspActionElement cms, String folder) {
        this.cms = cms;
        this.settings = new EventsCollectorSettings();
        this.settings.folder = folder;
    }
    
    /**
     * Creates a new collector with default settings.
     * <p>
     * Add any custom settings (categories to match, excluded folders, sort 
     * order etc.) before calling {@link #get(org.opencms.jsp.CmsJspActionElement, java.lang.String, int, long, long)}.
     * 
     * @param cms Action element, needed to read the event files.
     */
    public EventsCollector(CmsJspActionElement cms) {
        this.settings = new EventsCollectorSettings();
        this.cms = cms;
    }
    
    /**
     * Collects events, using the current settings and the provided arguments.
     * 
     * @param folder The root folder of the events to collect.
     * @param start The time frame start.
     * @param end The time frame end.
     * @param limit The maximum size of the returned list.
     * @return A list of events, collected using the current settings and the provided arguments.
     * @throws CmsException 
     */
    public List<EventEntry> get(String folder, long start, long end, int limit) throws CmsException {
        this.settings.folder = folder;        
        return collectEvents(getCollectorParameterString(start, end, limit));
    }
    
    /**
     * Collects events, using the current settings and the provided arguments.
     * 
     * @param folder The root folder of the events to collect.
     * @param range The time range.
     * @param limit The maximum size of the returned list.
     * @return A list of events, collected using the current settings and the provided arguments.
     * @throws CmsException 
     */
    public List<EventEntry> get(String folder, CollectorTimeRange range, int limit) throws CmsException {
        this.settings.folder = folder;        
        return collectEvents(getCollectorParameterString(range.getStart(), range.getEnd(), limit));
    }
    
    /**
     * Collects events, using the current settings and the provided arguments.
     * 
     * @param start The time frame start.
     * @param end The time frame end.
     * @param limit The maximum size of the returned list.
     * @return A list of events, collected using the current settings and the provided arguments.
     * @throws CmsException 
     */
    public List<EventEntry> get(long start, long end, int limit) throws CmsException {
        return collectEvents(getCollectorParameterString(start, end, limit));
    }
    
    /**
     * Collects events, using the current settings and the provided arguments.
     * 
     * @param range The time range.
     * @param limit The maximum size of the returned list.
     * @return A list of events, collected using the current settings and the provided arguments.
     * @throws CmsException 
     */
    public List<EventEntry> get(CollectorTimeRange range, int limit) throws CmsException {
        return collectEvents(getCollectorParameterString(range.getStart(), range.getEnd(), limit));
    }
    
    /**
     * Gets a collector parameter string that can be used to pass parameters
     * to the CmsTimeRangeCategoryCollector.
     * 
     * @param start The time range start time
     * @param end The time range end time
     * @param eventsFolder The folder to collect resources from
     * @param categories The categories used to filter collected resources.
     * @param sortDescending Sort ordering
     * @param resultLimit Maximum number of resources to collect
     * 
     * @return A parameter string that can be used to pass parameters to the CmsTimeRangeCategoryCollector
     */
    private String getCollectorParameterString(long start, 
                                                long end, 
                                                //String eventsFolder, 
                                                //List<CmsCategory> categories,
                                                int resultLimit) {
        String collectorParam = TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_RESOURCE + "=" + settings.folder;
        collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_RESOURCE_TYPE + "=" + EventEntry.RESOURCE_TYPE_NAME_EVENT;
        collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_TIMEFRAME_START + "=" + String.valueOf(start);
        collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_TIMEFRAME_END + "=" + String.valueOf(end);
        collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_PROPERTY_TIME_START + "=" + EventEntry.PROPERTY_TIME_START;
        collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_PROPERTY_TIME_END + "=" + EventEntry.PROPERTY_TIME_END;
        collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_EXCLUDE_EXPIRED + "=" + String.valueOf(!settings.includeExpired);
        collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_OVERLAP_LENIENT + "=" + String.valueOf(settings.includeEventsThatOverlap);
        collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_CATEGORY_INCLUSIVE + "=" + String.valueOf(!settings.categoriesMustAllMatch);
        collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_INCLUDE_RECURRENCES + "=" + String.valueOf(settings.includeEventRecurrences);
        collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_SORT_DESCENDING + "=" + String.valueOf(settings.sortDescending);
        collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_RESULT_LIMIT + "=" + (resultLimit == -1 ? String.valueOf(Integer.MAX_VALUE) : String.valueOf(resultLimit));
        if (this.settings.categories != null) {
            Iterator<CmsCategory> iCategories = this.settings.categories.iterator();
            if (iCategories.hasNext()) {
                collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_PROPERTY_CATEGORIES + "=" + EventEntry.PROPERTY_CATEGORIES;
                collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_CATEGORIES + "=";
                while (iCategories.hasNext()) {
                    collectorParam += iCategories.next().getRootPath() + (iCategories.hasNext() ? "," : "");
                }
            }
        }
        if (!settings.foldersExcluded.isEmpty()) {
            Iterator<String> iFoldersExcluded = settings.foldersExcluded.iterator();
            collectorParam += "|" + TimeRangeCategoryEventCollector.CollectorDataPropertyBased.PARAM_KEY_EXCLUDE_FOLDERS + "=";
            while (iFoldersExcluded.hasNext()) {
                collectorParam += iFoldersExcluded.next() + (iFoldersExcluded.hasNext() ? "," : "");
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
     * @see TimeRangeCategoryEventCollector#getEvents(org.opencms.file.CmsObject, java.lang.String) 
     */
    protected List<EventEntry> collectEvents(CmsJspActionElement cms, String collectorParam) throws CmsException {
        //CmsObject cmso = cms.getCmsObject();
        
        
        TimeRangeCategoryEventCollector collector = new TimeRangeCategoryEventCollector();
        List<EventEntry> results = collector.getEvents(cms, collectorParam);
        this.lastResultsTotal = collector.getTotalResults();
        this.lastResultCategories = collector.getResultCategories();
        return results;
        /*
        ArrayList<EventEntry> events = new ArrayList<EventEntry>();
        List result = collector.getResults(cmso, null, collectorParam);
        Iterator<CmsResource> iResults = result.iterator();
        try {
            
            while (iResults.hasNext()) {
                EventEntry event = new EventEntry(cms, iResults.next());
                event.setHtml("[No default html here, compose it yadamnself]");
                events.add(event);
            }
        } catch (Exception jspe) {
            throw new NullPointerException(jspe.getMessage());
        }
        
        return events;*/
    }

    /**
     * Collects events using the given collector parameters.<p>
     * 
     * @param collectorParam The collector parameters
     * 
     * @return A list of EventEntry objects collected using the given parameters
     * 
     * @throws org.opencms.main.CmsException If something goes wrong when attempting to collect the events
     * 
     * @see #getCollectorParameterString(java.lang.String, java.lang.String, java.lang.String, java.util.List, boolean, int)
     * @see TimeRangeCategoryEventCollector#getEvents(org.opencms.file.CmsObject, java.lang.String)
     */
    protected List<EventEntry> collectEvents(String collectorParam) throws CmsException {
        TimeRangeCategoryEventCollector collector = new TimeRangeCategoryEventCollector();
        List<EventEntry> results = collector.getEvents(cms, collectorParam);
        this.lastResultsTotal = collector.getTotalResults();
        this.lastResultCategories = collector.getResultCategories();
        return results;
    }
    
    /**
     * Gets the total results of the previous collect operation, <em>before</em> 
     * any shrinking/limiting took place.
     * <p>
     * A negative return value means no collect operation has taken place.
     * 
     * @return The total results of the previous collect operation, <em>before</em> any shrinking/limiting took place.
     */
    public int getTotalResults() {
        return this.lastResultsTotal;
    }
    
    /**
     * Gets the paths to all event categories in the previous collect operation, 
     * <em>before</em> any shrinking/limiting took place.
     * <p>
     * Each map entry consists of the category path, as read from the event's 
     * property {@link EventEntry#PROPERTY_CATEGORIES}, and a counter showing 
     * the number of events that has this category assigned.
     * 
     * @return the paths to all event categories in the previous collect operation, <em>before</em> any shrinking/limiting took place.
     */
    public Map<String, Integer> getResultCategories() {
        return this.lastResultCategories;
    }
    
    /**
     * Clears all settings.
     * 
     * @return This instance, with all settings cleared / reset to default.
     */
    public EventsCollector clear() {
        this.settings = new EventsCollectorSettings();
        this.lastResultsTotal = -1;
        this.lastResultCategories.clear();
        return this;
    }
    
    /**
     * Adds a category to match on events.
     * <p>
     * Categories are matched either exclusively (match ALL) or inclusively 
     * (match ANY).
     * 
     * @param category The category.
     * @return This instance, updated.
     * @see ResourceCategoriesFilter#filter(java.util.List, java.util.List, int)
     */
    public EventsCollector addCategoryToMatch(CmsCategory category) {
        if (category != null)
            this.settings.categories.add(category);
        
        return this;
    }
    
    /**
     * Adds a collection of categories to match on events.
     * <p>
     * Categories are matched either exclusively (match ALL) or inclusively 
     * (match ANY).
     * 
     * @param categories The collection of categories.
     * @return This instance, updated.
     * @see ResourceCategoriesFilter#filter(java.util.List, java.util.List, int)
     */
    public EventsCollector addCategoriesToMatch(List<CmsCategory> categories) {
        if (categories != null && !categories.isEmpty())
            this.settings.categories.addAll(categories);
        
        return this;
    }
    
    /**
     * Adds a folder to exclude.
     * 
     * @param folder The URI of the folder to exclude, e.g. "/my/folder/"
     * @return This instance, updated.
     */
    public EventsCollector excludeFolder(String folder) {
        if (folder != null)
            this.settings.foldersExcluded.add(folder);
        
        return this;
    }
    
    /**
     * Adds a collection of folders to exclude.
     * 
     * @param folders A list of URIs of folders to exclude.
     * @return This instance, updated.
     */
    public EventsCollector excludeFolders(List<String> folders) {
        if (folders != null && !folders.isEmpty())
            this.settings.foldersExcluded.addAll(folders);
        
        return this;
    }
    
    /*
     * Adds a folder holding undated events.
     * 
     * @param folder The URI of the folder holding undated events.
     * @return This instance, updated.
     *
    public EventsCollector addUndatedFolder(String folder) {
        this.settings.foldersUndated.add(folder);
        return this;
    }*/
    
    /*
     * Adds a collection of folders holding undated events.
     * 
     * @param folders A list of URIs to folders holding undated events.
     * @return This instance, updated.
     *
    public EventsCollector addUndatedFolders(List<String> folders) {
        this.settings.foldersUndated.addAll(folders);
        return this;
    }*/
    
    /**
     * Sets the sort order.
     * <p>
     * By default, events are sorted descending.
     * 
     * @param descending Pass <code>true</code> to sort descending, or <code>false</code> to sort ascending.
     * @return This instance, updated.
     */
    public EventsCollector setSortOrder(boolean descending) {
        this.settings.sortDescending = descending;
        return this;
    }
    
    /**
     * Sets whether or not to include recurrences.
     * <p>
     * By default, recurrences are included.
     * 
     * @param include Pass <code>true</code> to include, or <code>false</code> to exclude.
     * @return This instance, updated.
     */
    public EventsCollector setRecurrencesHandling(boolean include) {
        this.settings.includeEventRecurrences = include;
        return this;
    }
    
    /**
     * Sets whether or not to include events that only partially overlap.
     * 
     * @param lenient Pass <code>true</code> to include events that only partially overlap, or <code>false</code> to include only events that begin and end within a time frame.
     * @return This instance, updated.
     */
    public EventsCollector setOverlapLeniency(boolean lenient) {
        this.settings.includeEventsThatOverlap = lenient;
        return this;
    }
    
    /**
     * Sets whether or not to include events that have expired.
     * 
     * @param include Pass <code>true</code> to include, or <code>false</code> to exclude.
     * @return This instance, updated.
     */
    public EventsCollector setExpiredHandling(boolean include) {
        this.settings.includeExpired = include;
        return this;
    }
    
    /**
     * Sets whether or not to include events that are undated.
     * 
     * @param include Pass <code>true</code> to include, or <code>false</code> to exclude.
     * @return This instance, updated.
     */
    public EventsCollector setUndatedHandling(boolean include) {
        this.settings.includeEventsWithNoDate = include;
        return this;
    }
    
    /**
     * Sets the category match mode to either exclusive (must match ALL) or
     * inclusive (must match ANY).
     * 
     * @param mustAllMatch Pass <code>true</code> to include only events assigned ALL categories (exclusive mode), or <code>false</code> to include any event assigned any one category (inclusive mode).
     * @return This instance, updated.
     */
    public EventsCollector setCategoryMatchMode(boolean mustAllMatch) {
        this.settings.categoriesMustAllMatch = mustAllMatch;
        return this;
    }
    
    /**
     * Determines whether or not recurrences will be included.
     * 
     * @return True if recurrences will be included, false if not.
     */
    public boolean isRecurrenceInclusive() { return this.settings.includeEventRecurrences; }
    
    /**
     * Determines whether or not events that only partially overlap will be included.
     * 
     * @return True if events that only partially overlap will be included, false if not.
     */
    public boolean isOverlapLenient() { return this.settings.includeEventsThatOverlap; }
    
    /**
     * Determines whether or not events that have expired will be included.
     * 
     * @return True if expired events will be included, false if not.
     */
    public boolean isExpiredInclusive() { return this.settings.includeExpired; }
    
    /** 
     * Determines whether or not events that are undated will be included. 
     * 
     * @return True if undated events will be included, false if not.
     */
    public boolean isUndatedInclusive() { return this.settings.includeEventsWithNoDate; }
    
    /** 
     * Determines if the collector uses exclusive (match ALL) or inclusive (match ANY) category matching. 
     * 
     * @return True if the collector uses exclusive (match ALL) category matching, or false otherwise.
     */
    public boolean isCategoriesMustAllMatch() { return this.settings.categoriesMustAllMatch; }
}
