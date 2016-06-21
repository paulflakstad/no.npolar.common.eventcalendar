package no.npolar.common.eventcalendar;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
//import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

/**
 * Facilitates easy category filtering of collections.
 * @author Paul-Inge Flakstad, Norwegian Polar Institute.
 */
public class ResourceCategoriesFilter {
    /** Exclusive filtering mode: A resource must be assigned ALL filter categories to be considered a match. "Adding categories narrows results." */
    public static final int MODE_EXCLUSIVE = 0;
    /** Inclusive filtering mode: Any resource assigned at least one of the filter categories is considered a match. "Adding categories widens results." */
    public static final int MODE_INCLUSIVE = 1;
    
    //public ResourceCategoriesFilter() {}
    
    /**
     * Filters the given resources by evaluating their assigned categories.
     * <p>
     * The filter mode is used to determine how to evaluate if a resource matches 
     * the filter or not. There are 2 mode alternatives:
     * <ol>
     *  <li><strong>Exclusive filtering</strong> = match if assigned ALL filter categories.</li>
     *  <li><strong>Inclusive filtering</strong> = match if assigned at least one of the filter categories.</li>
     * </ol>
     * 
     * @param cmso An initialized CmsObject.
     * @param resources The list of resources to evaluate.
     * @param filterMatchCategories The root paths to the categories to match against (the filters).
     * @param categoriesPropertyName The name of the property onto which the categories are mapped (normally <code>collector.categories</code>).
     * @param filterMode The filter mode, one of {@link #MODE_EXCLUSIVE} or {@link #MODE_INCLUSIVE}.
     * @return The list of matching resources.
     * @see #MODE_EXCLUSIVE
     * @see #MODE_INCLUSIVE
     * @throws CmsException If something fails when reading the category property value from a resource.
     */
    public static List<CmsResource> filter(CmsObject cmso, List<CmsResource> resources, List<String> filterMatchCategories, String categoriesPropertyName, int filterMode) throws CmsException {
        
        if ((filterMatchCategories != null) && !filterMatchCategories.isEmpty()) { // If no filter categories were given, don't do anything
            CmsResource res = null;
            CmsProperty prop = null;
            
            Iterator<CmsResource> itr = resources.iterator();
            while (itr.hasNext()) { // Loop over all collected resources
                res = itr.next();
                prop = cmso.readPropertyObject(res, categoriesPropertyName, true); // Read the value of the category property (which should contain each assigned category's root path)
                if (prop.isNullProperty()) { // If the resource hasn't been assigned any category ...
                    itr.remove(); // ... then remove it
                } else { // The resource has been assigned at least one category, so we must match agains the filter(s) ...
                    List assignedCategories = CmsStringUtil.splitAsList(prop.getValue(), '|');

                    if (filterMode == MODE_INCLUSIVE) {
                        // Inclusive filter: 
                        // =================
                        // Any resource assigned at least one of the filter categories is considered a match.
                        // (The number of matching resources will remain stable or increase with each added category filter.)
                        boolean filterMatch = false;
                        Iterator<String> iFilterCategories = filterMatchCategories.iterator();
                        while (iFilterCategories.hasNext()) {
                            String filterCategory = iFilterCategories.next();
                            if (assignedCategories.contains(filterCategory)) {
                                filterMatch = true;
                                break;
                            }
                        }
                        if (!filterMatch) {
                            itr.remove();
                        }
                    }
                    else {
                        // Exclusive filter: (This is the typical case, and hence the default mode)
                        // =================
                        // A resource must be assigned ALL filter categories to be considered a match.
                        // (The number of matching resources will remain stable or decrease with each added category filter.)
                        if (!assignedCategories.containsAll(filterMatchCategories)) {
                            itr.remove();
                        }
                    }
                }
            }
        }
        return resources;
    }
    
    /**
     * Filters the given resources by evaluating their assigned categories.
     * <p>
     * The filter mode is used to determine how to evaluate if a resource matches 
     * the filter or not. There are 2 mode alternatives:
     * <ol>
     *  <li><strong>Exclusive filtering</strong> = match if assigned ALL filter categories.</li>
     *  <li><strong>Inclusive filtering</strong> = match if assigned at least one of the filter categories.</li>
     * </ol>
     * 
     * @param events The list of events to evaluate.
     * @param filterMatchCategories The root paths to the categories to match against (the filters).
     * @param filterMode The filter mode, one of {@link #MODE_EXCLUSIVE} or {@link #MODE_INCLUSIVE}.
     * @return The list of matching resources.
     * @see #MODE_EXCLUSIVE
     * @see #MODE_INCLUSIVE
     * @throws CmsException 
     */
    public static List<EventEntry> filter(List<EventEntry> events, List<String> filterMatchCategories, int filterMode) throws CmsException {
        
        if (filterMatchCategories != null && !filterMatchCategories.isEmpty()) { // If no filter categories were given, don't do anything
            
            Iterator<EventEntry> itr = events.iterator();
            while (itr.hasNext()) { // Loop over all collected resources
                EventEntry event = itr.next();
                
                if (!event.hasCategories()) {
                    itr.remove();
                } else { // The resource has been assigned at least one category, so we must match agains the filter(s) ...
                    
                    // OpenCms changed the separator for propertyList somewhere around version 9.0
                    String propertyListSeparatorRegex = ",";
                    if (event.getCategoriesString().contains("|")) {
                        propertyListSeparatorRegex = "\\|";
                    }
                    
                    List assignedCategories = Arrays.asList(event.getCategoriesString().split(propertyListSeparatorRegex));

                    if (filterMode == MODE_INCLUSIVE) {
                        // Inclusive filter: 
                        // =================
                        // Any resource assigned at least one of the filter categories is considered a match.
                        // (The number of matching resources will remain stable or increase with each added category filter.)
                        boolean filterMatch = false;
                        Iterator<String> iFilterCategories = filterMatchCategories.iterator();
                        while (iFilterCategories.hasNext()) {
                            String filterCategory = iFilterCategories.next();
                            if (assignedCategories.contains(filterCategory)) {
                                filterMatch = true;
                                break;
                            }
                        }
                        if (!filterMatch) {
                            itr.remove();
                        }
                    }
                    else {
                        // Exclusive filter: (This is the typical case, and hence the default mode)
                        // =================
                        // A resource must be assigned ALL filter categories to be considered a match.
                        // (The number of matching resources will remain stable or decrease with each added category filter.)
                        if (!assignedCategories.containsAll(filterMatchCategories)) {
                            itr.remove();
                        }
                    }
                }
            }
        }
        return events;
    }
}