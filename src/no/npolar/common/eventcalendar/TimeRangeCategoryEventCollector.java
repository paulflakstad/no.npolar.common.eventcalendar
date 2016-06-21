package no.npolar.common.eventcalendar;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
//import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
//import org.opencms.file.types.I_CmsResourceType;
//import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
//import org.opencms.main.CmsIllegalArgumentException;
//import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
//import org.opencms.util.CmsStringUtil;

//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
//import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import javax.servlet.http.HttpSession;
import org.opencms.file.collectors.*;
import org.opencms.main.CmsLog;
import org.apache.commons.logging.Log;
//import org.opencms.db.CmsSecurityManager;
//import org.opencms.db.CmsUserSettings;
//import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
//import org.opencms.main.CmsContextInfo;
//import org.opencms.workplace.CmsWorkplaceManager;
//import org.opencms.workplace.CmsWorkplaceSettings;

/**
 * Facilitates collecting events, based on time ranges and categories.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute.
 */
public class TimeRangeCategoryEventCollector extends CmsTimeRangeCategoryCollector {
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(TimeRangeCategoryEventCollector.class);
    //private CmsJspActionElement cms = null;
    
    /**
     * Holds the total number of events in the last result, before limiting.
     */
    private int resultsTotal = -1;
    /**
     * Holds the paths to all categories found on the events in the last result, before limiting.
     */
    private Map<String, Integer> resultCategories = new HashMap<String, Integer>();
    
    /**
     * Creates a new instance of this collector.
     */
    public TimeRangeCategoryEventCollector() {
        super();
    }
    
    /**
     * Creates a new instance of this collector.
     * 
     * @param cms Initialized action element.
     */
    public TimeRangeCategoryEventCollector(CmsJspActionElement cms) {
        super();
        //this.cms = cms;
    }
    
    
    /**
     * Gets any EvenEntry resources that meet the criteria defined in the 
     * collector's parameters.
     * <p>
     * This method stores the parameters and evaluates the collector name, then 
     * calls {@link #collectResourcesByTimeRangeAndCategories(org.opencms.file.CmsObject)}, 
     * which does all the heavy lifting.
     * 
     * @param cmso An initialized CmsObject holding context, locale etc.
     * @param collectorName The name of the collector to use. Passing null will cause a fallback to the default, {@link #COLLECTOR_NAME}.
     * @param param The parameters string. For more info, see {@link EventsCollector#getCollectorParameterString(java.lang.String, java.lang.String, java.lang.String, java.util.List, boolean, boolean, boolean, boolean, int)}.
     * @throws CmsDataAccessException, CmsException
     * @return Any resource that meet the criteria defined in the collector settings.
     * @see CmsTimeRangeCategoryCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     * @deprecated Use {@link #getEvents(org.opencms.jsp.CmsJspActionElement, java.lang.String)} whenever possible.
     */
    @Override
    public List<CmsResource> getResults(CmsObject cmso, String collectorName, String param) throws CmsDataAccessException, CmsException {
        // if action is not set use default
        if (collectorName == null) {
            collectorName = COLLECTOR_NAME;
        }
        
        // Parse parameters
        this.data = new CollectorDataPropertyBased(param);
        
        if (COLLECTOR_NAME.equals(collectorName)) {
            return collectResourcesByTimeRangeAndCategories(cmso);
        } else {
            throw new CmsDataAccessException(org.opencms.file.collectors.Messages.get().container(
                org.opencms.file.collectors.Messages.ERR_COLLECTOR_NAME_INVALID_1,
                collectorName));
        }
    }
    
    /**
     * Gets any EventEntry instances that meet the criteria defined in the 
     * collector's parameters.
     * <p>
     * This method stores the parameters, then calls
     * {@link #collectEventsByTimeRangeAndCategories(org.opencms.file.CmsObject)}, 
     * which does all the heavy lifting.
     * 
     * @param cms An initialized CMS action element, holding context, locale etc.
     * @param param The parameters string. For more info, see {@link EventsCollector#getCollectorParameterString(java.lang.String, java.lang.String, java.lang.String, java.util.List, boolean, boolean, boolean, boolean, int)}.
     * @throws CmsDataAccessException, CmsException
     * @return A list of EventEntry instances that meet the criteria defined in the collector settings.
     * @see CmsTimeRangeCategoryCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List<EventEntry> getEvents(CmsJspActionElement cms, String param) throws CmsDataAccessException, CmsException {
        // Parse parameters
        this.data = new CollectorDataPropertyBased(param);
        return collectEventsByTimeRangeAndCategories(cms);
    }
    
    /*private List<EventEntry> getRecurrences(CmsObject cmso, EventEntry event, long start, long end) {
        List<EventEntry> recurrencesOfEvent = new ArrayList<EventEntry>();
            
        if (event.hasRecurrenceRule()) {

            try {
                // Don't forget this ;)
                String rrule = "RRULE:" + event.getRecurrenceRule();

                //if (DEBUG) out.println("<p>'" + recurringEvent.getTitle() + "' isOneDayEvent(): " + recurringEvent.isOneDayEvent() + "</p>");
                // Then get its "next" begin time, that is, next after (or on) the given start time
                Date nextBeginTime = event.getBegin(new Date(start));
                // ... and then get the subsequent end time
                Date nextEndTime = event.getEnd(nextBeginTime);

                Date closestPastStartDate = event.getClosestPastStartDate( EventCalendarUtils.getOpenCmsNowDate(cmso) );
                //if (DEBUG) out.println("<p>getClosestPastStartDate returned " + new SimpleDateFormat("d MMM yyyy").format(cpd) + "</p>");
                //if (DEBUG) out.println("<p>nextBeginTime set to " + new SimpleDateFormat("d MMM yyyy").format(nextBeginTime) + "</p>");

                // Get the recurring event's initial start date (read from its regular "start time" field)
                Date recurringEventInitialStartTime = new Date(event.getStartTime());

                // If the recurring event's "original" start time is before the calculated "next" begin time, we're dealing with a recurrence
                boolean isRecurrence = recurringEventInitialStartTime.getTime() < nextBeginTime.getTime();
                if (isRecurrence) {
                    try {
                        // Create the recurrence event (it's identical to the original event, but with begin/end dates adjusted)
                        EventEntry recurrence = new EventEntry(nextBeginTime.getTime()
                                                    , nextEndTime.getTime()
                                                    , event.getTitle()
                                                    , event.getDescription()
                                                    , event.getTimeDisplayMode()
                                                    , event.getLocale()
                                                    , event.getResourceId()
                                                    , event.getStructureId()
                                                    , event.getRecurrenceRule()
                                                    , event.getCategoriesString());
                        
                        //if (DEBUG) out.println("<p>Recurrence event '" + recurrence.getTitle() + "' created.</p>");
                        // If the timespan of the recurrence event overlaps today ...
                        if (recurrence.overlapsRange(start, end)) {
                            //if (DEBUG) out.println("<p>Recurrence event '" + recurrence.getTitle() + "' DOES overlap current time: adding it.</p>");
                            recurrencesOfEvent.add(recurrence);
                            //break; // Don't iterate over any more 
                        } else {
                            //if (DEBUG) out.println("<p>Recurrence event '" + recurrence.getTitle() + "' does NOT overlap current time: ignoring it.</p>");
                        }
                    } catch (Exception e) {
                        //out.println("<!-- Error processing recurrence of event: " + e.getMessage() + " -->");
                    }
                }
            } catch (Exception ee) {
                //out.println("<!-- ERROR on event '" + recurringEventResource.getRootPath() + "': " + ee.getMessage() + " -->");
            }
        }
        return recurrencesOfEvent;
    }*/
    
    /**
     * Does the actual collecting of event resources, on behalf of the invoking 
     * method {@link #getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)}.
     * <p>
     * <strong>Does not take recurrences into account.</strong> If possible, use 
     * {@link #getEvents(org.opencms.file.CmsObject, java.lang.String)} instead.
     * <p>
     * (The settings for this collector, which are used here, are set by passing 
     * a configuration string to the invoking method.)
     * 
     * @param cmso An initialized CmsObject holding context, locale, etc.
     * @return A list of event resources that fit the criteria currently configured for this collector.
     * @throws CmsException 
     * @see #getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String) 
     * @deprecated Use {@link #getEvents(org.opencms.file.CmsObject, java.lang.String)} instead, whenever possible.
     */
    private List<CmsResource> collectResourcesByTimeRangeAndCategories(CmsObject cms) throws CmsException {

        // Step 1: Read from DB, expiration is respected.
        String foldername = CmsResource.getFolderPath(data.getFileName());
        CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(OpenCms.getResourceManager().getResourceType("np_event").getTypeId()).addExcludeFlags(CmsResource.FLAG_TEMPFILE);
        
        List<CmsResource> result = cms.readResources(foldername, filter, true); // Fetch all folder resources, filtered by type and flags
        Iterator<CmsResource> itResults = result.iterator(); // Iterate over the previously fetched folder resources
        
        //LOG.error("Collected " + result.size() + " events initially.");
        
        while (itResults.hasNext()) {
            EventEntry event = new EventEntry(cms, itResults.next());
            
            if (data.isExcludeExpired() && event.isExpired()) { // If the event is expired and we don't want expired events ...
                itResults.remove(); // ... then remove it
                //LOG.error("Removed " + (event.isDisplayDateOnly() ? "dateonly" : "datetime") + "] event " + event.getTitle() + " because it was expired (now is "  + new Date() + ", event start is " + new Date(event.getStartTime()) + ").");
                continue;
            }
            
            // Handle the case "only start time set, no end time"
            if (!event.hasEndTime()) {
                if (!event.startsInRange(data.getTimeFrameStart(), data.getTimeFrameEnd())) { // If the start time is not inside the timerange ...
                    itResults.remove(); // ... then remove it
                    //LOG.error("Removed event " + event.getTitle() + " because it had no end time and started outside the range.");
                    continue;
                }
            }
            
            // Handle the case "both start time and end time set"
            else {
                // Lenient mode:
                if (data.isOverlapLenient()) {
                    if (!event.overlapsRange(data.getTimeFrameStart(), data.getTimeFrameEnd())) {  // ... if the event does not overlap the range in some way ...
                        itResults.remove(); // ... then remove it
                        //LOG.error("Removed event " + event.getTitle() + " because it did not overlap the range.");
                        continue;
                    }
                }
                // Non-lenient mode:
                else {
                    if (!event.startsInRange(data.getTimeFrameStart(), data.getTimeFrameEnd())) {
                        itResults.remove();
                        //LOG.error("Removed event " + event.getTitle() + " because it did not occur within the range in its entirety.");
                        continue;
                    }
                }
            }
        }

        //LOG.error("Now " + result.size() + " events remain.");
        //LOG.error("Filtering events by category ...");
        
        // Step 3: Category filtering
        ResourceCategoriesFilter.filter(cms, 
                              result, 
                              data.getCategories(), 
                              data.getPropertyCategories().getName(), 
                              data.isCategoryInclusive() ? ResourceCategoriesFilter.MODE_INCLUSIVE : ResourceCategoriesFilter.MODE_EXCLUSIVE);
        
        //LOG.error("Now " + result.size() + " events remain.");
        
        
        // Step 4: Sorting
        
        // Create a comparator for collector.date (date released can't be used)
        List datePropertiesForCoparison = new ArrayList();
        datePropertiesForCoparison.add(EventEntry.PROPERTY_TIME_START);//add("collector.date"); //removed hard coding, use the static final string instead
        CmsDateResourceComparator dateComparator = new CmsDateResourceComparator(cms, datePropertiesForCoparison, true);
        if (data.isSortDescending()) {
            dateComparator = new CmsDateResourceComparator(cms, datePropertiesForCoparison, false);
        }
        
        // Sort results using the defined comparator
        Collections.sort(result, dateComparator);
        
        // Step 5: Update categories in result, and result count
        setCategories(cms, result);
        this.resultsTotal = result.size();
        
        // Step 6: limit result
        return shrinkToFit(result, data.getCount());
    }
    
    /*private void setCategories(CmsObject cmso, List<EventEntry> events) {
        resultCategories.clear();
        
    }*/
    
    /**
     * Sets the result categories based on the given list of event instances.
     * <p>
     * The list of events must contain CmsResource or EventEntry instances.
     * 
     * @param cmso Initialized CmsObject, needed to access properties.
     * @param events The event objects, a CmsResource or EventEntry instances.
     */
    private void setCategories(CmsObject cmso, List<? extends Object> events) {
        resultCategories.clear();
        for (Object event : events) {
            updateCategories(cmso, event);
        }
    }
    
    /**
     * Updates the result categories map with the categories for the given event.
     * <p>
     * The event object must be either a CmsResource or an EventEntry instance.
     * 
     * @param cmso Initialized CmsObject, needed to access properties.
     * @param event The event object, a CmsResource or EventEntry instance.
     */
    private void updateCategories(CmsObject cmso, Object event) {
        String catString = "";
        
        if (event instanceof EventEntry) {
            catString = ((EventEntry)event).getCategoriesString();
        } else if (event instanceof CmsResource) {
            try { catString = cmso.readPropertyObject((CmsResource)event, EventEntry.PROPERTY_CATEGORIES, false).getValue(""); } catch (Exception e) {}
        }
        
        if (catString != null && !catString.trim().isEmpty()) {
            for (String catPath : catString.split(",|\\|")) {
                addOrIncrementCategory(catPath);
            }
        }
    }
    
    /**
     * Adds the given category path to the list of paths, or, in case it already 
     * existed, increments the counter.
     * 
     * @param catPath The category path.
     */
    private void addOrIncrementCategory(String catPath) {
        if (catPath != null && !catPath.isEmpty()) {
            int num = 0;
            if (resultCategories.containsKey(catPath)) {
                num = resultCategories.get(catPath);
            } 
            resultCategories.put(catPath, ++num);
        }
    }
    
    /**
     * Does the actual collecting of events, on behalf of the invoking method 
     * {@link #getEvents(org.opencms.file.CmsObject, java.lang.String, java.lang.String)}.
     * <p>
     * (The settings for this collector, which are used here, are set by passing 
     * a configuration string to the invoking method.)
     * 
     * @param cms An initialized CMS action element, holding context, locale, etc.
     * @return A list of events that fits the criteria passed to the invoking method.
     * @throws CmsException 
     */
    private List<EventEntry> collectEventsByTimeRangeAndCategories(CmsJspActionElement cms) throws CmsException {
        CmsObject cmso = cms.getCmsObject();

        // Step 1: Read from DB, expiration is respected.
        String foldername = CmsResource.getFolderPath(data.getFileName());
        CmsResourceFilter filter = CmsResourceFilter.DEFAULT
                                    .addRequireType(OpenCms.getResourceManager().getResourceType(EventEntry.RESOURCE_TYPE_NAME_EVENT).getTypeId())
                                    .addExcludeFlags(CmsResource.FLAG_TEMPFILE);
        
        List<CmsResource> result = cmso.readResources(foldername, filter, true); // Fetch all resources in the folder sub-tree, filter on type and flag(s)
        Iterator<CmsResource> iResults = result.iterator(); // Iterator for these resources
        
        //LOG.error("Collected " + result.size() + " events initially.");
        
        // List to hold all matching events - we will fill this next
        List<EventEntry> events = new ArrayList<EventEntry>();
        
        while (iResults.hasNext()) {
            CmsResource res = iResults.next();
            
            // If excluded folder(s) are set, check if the event is in one
            if (!data.getExcludedFolders().isEmpty()) {
                boolean excludeThis = false;
                String eventUri = cmso.getSitePath(res);
                Iterator<String> iExcludedFolders = data.getExcludedFolders().iterator();
                while (iExcludedFolders.hasNext()) {
                    if (eventUri.startsWith(iExcludedFolders.next())) {
                        // Match => event is in excluded folder
                        excludeThis = true; 
                        break;
                    }
                }
                if (excludeThis) {
                    continue; // Do not proceed, just skip to next event
                }
            }
            
            // Create the EventEntry instance
            EventEntry event = new EventEntry(cms, res);
            // Get recurrences inside the specified timeframe, or - if 
            // recurrences are not to be included - just create an empty list
            List<EventEntry> eventAndRecurrences = data.isIncludeRecurrences() ? 
                    event.getRecurrences(data.getTimeFrameStart(), data.getTimeFrameEnd())
                    : new ArrayList<EventEntry>();
            
            // Add the original event at the head of the list (index zero)
            eventAndRecurrences.add(0, event);
            
            // If expired events should be excluded, do an expiration check
            if (data.isExcludeExpired()) {
                Iterator<EventEntry> iEvents = eventAndRecurrences.iterator();
                while (iEvents.hasNext()) {
                    EventEntry e = iEvents.next();
                    if (e.isExpired()) {
                        iEvents.remove(); // Expired event => remove it
                    }
                }
            }
            
            
            if (!event.hasEndTime()) {
                //
                // Handle case "only start time set, no end time"
                //
                
                Iterator<EventEntry> iEvents = eventAndRecurrences.iterator();
                while (iEvents.hasNext()) {
                    EventEntry e = iEvents.next();
                    if (e.startsInRange(data.getTimeFrameStart(), data.getTimeFrameEnd())) { // If the start time is inside the timerange ...
                        events.add(e);
                    }
                }
            } else { 
                //
                // Handle case "both start time and end time set"
                //
                
                if (data.isOverlapLenient()) {
                    // Lenient mode - keep any event that overlaps the time frame:
                    Iterator<EventEntry> iEvents = eventAndRecurrences.iterator();
                    while (iEvents.hasNext()) {
                        EventEntry e = iEvents.next();
                        if (e.overlapsRange(data.getTimeFrameStart(), data.getTimeFrameEnd())) { // If the event overlaps the time frame ...
                            events.add(e);
                        }
                    }
                } else {
                    // Non-lenient mode - keep only events that begin inside the time frame:
                    Iterator<EventEntry> iEvents = eventAndRecurrences.iterator();
                    while (iEvents.hasNext()) {
                        EventEntry e = iEvents.next();
                        if (e.startsInRange(data.getTimeFrameStart(), data.getTimeFrameEnd())) { // If the event overlaps the time frame ...
                            events.add(e);
                        }
                    }
                }
            }
        }

        // Remove duplicates ("kinda costly" ... should fix this...later)
        List<EventEntry> unique = new ArrayList<EventEntry>();
        Iterator<EventEntry> iEvents = events.iterator();
        while (iEvents.hasNext()) {
            EventEntry e = iEvents.next();
            if (unique.contains(e)) {
                iEvents.remove();
                continue;
            }
            unique.add(e);
        }
        
        
        //LOG.error("Now " + result.size() + " events remain.");
        //LOG.error("Filtering events by category ...");
        
        // Step 3: Category filtering
        ResourceCategoriesFilter.filter(events, 
                              data.getCategories(), 
                              data.isCategoryInclusive() ? ResourceCategoriesFilter.MODE_INCLUSIVE : ResourceCategoriesFilter.MODE_EXCLUSIVE);
        /*ResourceCategoriesFilter.filter(cmso, 
                              result, 
                              data.getCategories(), 
                              data.getPropertyCategories().getName(), 
                              data.isCategoryInclusive() ? ResourceCategoriesFilter.MODE_INCLUSIVE : ResourceCategoriesFilter.MODE_EXCLUSIVE);*/
        
        //LOG.error("Now " + result.size() + " events remain.");
        
        
        // Step 4: Sorting
        Collections.sort(events, data.isSortDescending() ? EventEntry.COMPARATOR_START_TIME : EventEntry.COMPARATOR_START_TIME_DESC);
        /*
        // Create a comparator for collector.date (date released can't be used)
        List<String> datePropertiesForComparison = Arrays.asList(new String[] { EventEntry.PROPERTY_TIME_START });
        CmsDateResourceComparator dateComparator = new CmsDateResourceComparator(cmso, datePropertiesForComparison, true);
        if (data.isSortDescending()) {
            dateComparator = new CmsDateResourceComparator(cmso, datePropertiesForComparison, false);
        }
        
        // Sort results using the defined comparator
        Collections.sort(result, dateComparator);
        */
        
        // Step 5: Update categories in result, and result count
        setCategories(cmso, events);
        this.resultsTotal = events.size();
        
        // Step 5: result limit
        return limit(events, data.getCount());
        //return shrinkToFit(result, data.getCount());
    }
    
    /**
     * Limits a list of events by size, retaining at max the first <i>N</i> 
     * entries (<i>N</i> = maxSize).
     * <p>
     * If the given list is not bigger than maxSize, it is returned directly.
     * <p>
     * The given list is not modified in any way. The returned list is either 
     * the same list (if it does not need limiting) or a new list that is 
     * created from a sub-list of the given list.
     * 
     * @param completeList The list to limit by size.
     * @param maxSize The maximum size of the returned list.
     * @return A list containing, at max, the <i>N</i> first entries of the given list (<i>N</i> = maxSize).
     */
    protected List<EventEntry> limit(List<EventEntry> completeList, int maxSize) {
        int size = completeList.size();
        if (size <= maxSize || maxSize < 0) {
            return completeList;
        } else {
            if (maxSize > completeList.size()) {
                maxSize = completeList.size();
            }
            return new ArrayList<EventEntry>(completeList.subList(0, maxSize));
        }
    }
    
    /**
     * Gets the total results of the previous collect operation, <em>before</em> 
     * any shrinking/limiting took place.
     * 
     * @return The total results of the previous collect operation, <em>before</em> any shrinking/limiting took place.
     */
    public int getTotalResults() {
        return this.resultsTotal;
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
        return this.resultCategories;
    }
}
