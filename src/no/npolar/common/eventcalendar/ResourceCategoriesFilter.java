package no.npolar.common.eventcalendar;

import java.util.Iterator;
import java.util.List;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;

/**
 *
 * @author flakstad
 */
public class ResourceCategoriesFilter {
    /** Exclusive filtering mode: A resource must be assigned ALL filter categories to be considered a match.  */
    public static final int MODE_EXCLUSIVE = 0;
    /** Inclusive filtering mode: Any resource assigned at least one of the filter categories is considered a match.  */
    public static final int MODE_INCLUSIVE = 1;
    
    public ResourceCategoriesFilter() {
        
    }
    /**
     * Filters the given resources by evaluating their assigned categories.
     * The filter mode is used to determine how to evaluate if a resource matches 
     * the filter or not. There are two alternatives:
     * <ol>
     *  <li>Exclusive filtering mode means a resource must be assigned ALL filter categories to be considered a match.</li>
     *  <li>Inclusive filtering mode means any resource assigned at least one of the filter categories is considered a match.</li>
     * </ol>
     * 
     * @param cms An initialized CmsObject.
     * @param resources The list of resources to evaluate.
     * @param filterCategories The root paths to the categories to match against (the filters).
     * @param categoriesPropertyName The name of the property onto which the categories are mapped (normally "collector.categories").
     * @param filterMode The filter mode, one of ResourceCategoriesFilter#MODE_EXCLUSIVE or ResourceCategoriesFilter#MODE_INCLUSIVE.
     * 
     * @return The list of matching resources.
     * 
     * @see ResourceCategoriesFilter#MODE_EXCLUSIVE
     * @see ResourceCategoriesFilter#MODE_INCLUSIVE
     * 
     * @throws CmsException If something fails when reading the category property value from a resource.
     */
    public static List<CmsResource> filter(CmsObject cms, List<CmsResource> resources, List<String> filterCategories, String categoriesPropertyName, int filterMode) throws CmsException {
        
        if ((filterCategories != null) && !filterCategories.isEmpty()) { // If no filter categories were given, don't do anything
            CmsResource res = null;
            CmsProperty prop = null;
            
            Iterator<CmsResource> itr = resources.iterator();
            while (itr.hasNext()) { // Loop over all collected resources
                res = itr.next();
                prop = cms.readPropertyObject(res, categoriesPropertyName, true); // Read the value of the category property (which should contain each assigned category's root path)
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
                        Iterator<String> iFilterCategories = filterCategories.iterator();
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
                        if (!assignedCategories.containsAll(filterCategories))
                            itr.remove();
                    }
                }
            }
        }
        return resources;
    }
}
