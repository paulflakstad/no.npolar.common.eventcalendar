package no.npolar.common.eventcalendar;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import no.npolar.common.eventcalendar.view.CategoryFilter;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;

/**
 *
 * @author flakstad
 */
public class EventCalendarUtils {
    public EventCalendarUtils() {
        
    }
    
    /**
     * Capitalizes the given String.
     */
    public static String capitalize(String s) {
        return String.valueOf(s.charAt(0)).toUpperCase().concat(s.substring(1));
    }
    
    /**
     * Converts the given parameter map to a String.
     */  
    public static String getParameterString(Map params) {
        if (params == null)
            return "";
        Set keys = params.keySet();
        Iterator ki = keys.iterator();
        String key = null;
        String val = null;
        String paramStr = "";
        while (ki.hasNext()) {
            key = (String)ki.next();
            if (params.get(key).getClass().getCanonicalName().equals("java.lang.String[]")) { // Multiple values (standard form)
                for (int i = 0; i < ((String[])params.get(key)).length; i++) {
                    val = ((String[])params.get(key))[i];
                    if (val.trim().length() > 0) {
                        if (paramStr.length() > 0)
                            paramStr += "&amp;";
                        paramStr += key + "=" + val;
                    }
                }
            }
            else if (params.get(key).getClass().getCanonicalName().equals("java.lang.String")) { // Single value
                if (paramStr.length() == 0)
                    paramStr += key + "=" + (String)params.get(key);
                else
                    paramStr += "&amp;" + key + "=" + (String)params.get(key);
            }
        }
        return paramStr;
    }
    
    /** 
     * Tries to match a category or any of its parent categories against a list of possible matching categories. 
     * Used typically to retrieve the "top level" (parent) category of a given category.
     *
     * @param possibleMatches The list of possible matching categories (typically "top level" categories).
     * @param category The category to match against the list of possible matches (typically any category assigned to an event).
     * @param cmso An initialized CmsObject.
     * @param categoryReferencePath The category reference path - i.e. a path that is used to determine which categories are available.
     * 
     * @return The first category in the list of possible matches that matches the given category, or null if there is no match.
     */
    public static CmsCategory matchCategoryOrParent(List<CmsCategory> possibleMatches, CmsCategory category, CmsObject cmso, String categoryReferencePath) throws CmsException {
        CmsCategoryService cs = CmsCategoryService.getInstance();
        String catPath = category.getPath();
        CmsCategory tempCat = null;
        while (catPath.contains("/") && !(catPath = catPath.substring(0, catPath.lastIndexOf("/"))).equals("")) {
            try {
                tempCat = cs.readCategory(cmso, catPath, categoryReferencePath);
                if (possibleMatches.contains(tempCat))
                    return tempCat;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    
    /**
     * Gets a link with a URL containing the correct parameters to either apply or remove the given filter.
     * 
     * @param filter The filter that this 
     * @param parameterNameCategory
     * @param linkTarget
     * @param requestParameterString
     * @return 
     */
    /*public static String getAddOrRemoveLink(CategoryFilter filter, String parameterNameCategory, String linkTarget, String requestParameterString) {
        String filterLink = "";
        if (requestParameterString.contains(filter.getUri())) {
            filterLink = "<a style=\"font-weight:bold;\" class=\"remove\" href=\"" + linkTarget + "?" + requestParameterString;
            filterLink = filterLink.replace(parameterNameCategory + "=".concat(filter.getUri()), "");
            filterLink = filterLink.replace("&amp;&amp;", "&amp;"); // If multiple categories were in the URL, we just created "&&"
            filterLink = filterLink.endsWith("?") ? filterLink.substring(0, filterLink.length() - 1) : filterLink; // Remove trailing ?
            filterLink = filterLink.endsWith("&amp;") ? filterLink.substring(0, filterLink.length() - 5) : filterLink; // Remove trailing &
            filterLink += "\" rel=\"nofollow\">" + filter.getName() + " <span class=\"remove-filter\""
                        + " style=\"background:red; border-radius:3px; color:white; padding:0 0.3em;\""
                        + ">X</span></a>";
        }
        else {
            filterLink = "<a href=\"" + linkTarget + "?" + 
                                        requestParameterString + "&amp;" + parameterNameCategory + "=" + filter.getUri() + "\" rel=\"nofollow\">" + 
                                        filter.getName() + " (" + filter.getCounter() + ")</a>";
        }
        return filterLink;
    }*/
}
