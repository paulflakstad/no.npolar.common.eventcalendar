package no.npolar.common.eventcalendar;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.opencms.file.collectors.*;
import org.opencms.main.CmsLog;
import org.apache.commons.logging.Log;
import org.opencms.db.CmsSecurityManager;

/**
 *
 * @author flakstad
 */
public class TimeRangeCategoryEventCollector extends CmsTimeRangeCategoryCollector {
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSecurityManager.class);
    
    public TimeRangeCategoryEventCollector() {
        super();
    }
    
    /**
     * Gets any resource that meets the requirements defined in the collector's parameters.<p>
     * 
     * @see CmsTimeRangeCategoryCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List getResults(CmsObject cms, String collectorName, String param)
                                                    throws CmsDataAccessException, CmsException {
        // if action is not set use default
        if (collectorName == null) {
            collectorName = COLLECTOR_NAME;
        }
        
        // Parse parameters
        this.data = new CollectorDataPropertyBased(param);
        
        if (COLLECTOR_NAME.equals(collectorName)) {
            return collectEventsByTimeRangeAndCategories(cms);
        } else {
            throw new CmsDataAccessException(org.opencms.file.collectors.Messages.get().container(
                org.opencms.file.collectors.Messages.ERR_COLLECTOR_NAME_INVALID_1,
                collectorName));
        }
    }
    
    private List<CmsResource> collectEventsByTimeRangeAndCategories(CmsObject cms) throws CmsException {

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
        
        // Step 5: result limit
        return shrinkToFit(result, data.getCount());
    }
}
