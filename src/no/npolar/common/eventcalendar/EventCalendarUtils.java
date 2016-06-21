package no.npolar.common.eventcalendar;

import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
//import no.npolar.common.eventcalendar.view.CategoryFilter;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;

/**
 * Helper methods for the events package.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute.
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
     * Gets a Calendar that represents the start of the day defined by the 
     * given date.
     * 
     * @param d The date to get the start for.
     * @return A Calendar that represents the start of the day defined by the given date.
     */
    public static Calendar getDateStartCal(Date d) {
        Calendar dateStartCal = new GregorianCalendar();
        dateStartCal.setTime(d);
        dateStartCal.set(Calendar.HOUR_OF_DAY, 0);
        dateStartCal.set(Calendar.MINUTE, 0);
        dateStartCal.set(Calendar.SECOND, 0);
        return dateStartCal;
    }
    /**
     * Gets a Calendar that represents the end of the day defined by the 
     * given date.
     * 
     * @param d The date to get the end for.
     * @return A Calendar that represents the end of the day defined by the given date.
     */
    public static Calendar getDateEndCal(Date d) {
        Calendar dateEndCal = new GregorianCalendar();
        dateEndCal.setTime(d);
        dateEndCal.set(Calendar.HOUR_OF_DAY, 23);
        dateEndCal.set(Calendar.MINUTE, 59);
        dateEndCal.set(Calendar.SECOND, 59);
        return dateEndCal;
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
     * Converts the given Date instance to a DateValue instance.
     */
    public static DateValue convertToDateValue(Date d) {
        Calendar helperCal = new GregorianCalendar(TimeZone.getTimeZone("GMT+1:00"), new Locale("no")); // Locale is just a random one
        helperCal.setTime(d);
        return new DateValueImpl(helperCal.get(Calendar.YEAR), helperCal.get(Calendar.MONTH)+1, helperCal.get(Calendar.DATE));
    }
    /**
     * Converts the given DateValue instance to a Date instance.
     * <p>
     * As DateValue know no clock time, it is set to 12:00:00 in the returned 
     * Date instance.
     */
    public static Date convertToDate(DateValue dv) {
        Calendar helperCal = new GregorianCalendar(TimeZone.getTimeZone("GMT+1:00"), new Locale("no")); // Locale is just a random one
        helperCal.set(dv.year(), dv.month()-1, dv.day(), 12, 0, 0);
        return helperCal.getTime();
    }
    
    /**
     * Gets the workplace timestamp as a Date instance.
     * <p>
     * If time warp is active, the returned datetime will be the "warped" time. 
     * Otherwise, the actual "now" is returned.
     *
     * @param cmso An initialized CmsObject.
     * @return Date The current workplace "now", as a Date instance - either the actual "now" or a time-warped "now".
     */
    public static Date getOpenCmsNowDate(CmsObject cmso) {
        long userCurrentTime = new Date().getTime();
        Object timeWarpObj = cmso.getRequestContext().getCurrentUser().getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_TIMEWARP);
        try {
            userCurrentTime = (Long)timeWarpObj;
        } catch (ClassCastException e) {
            try {
                userCurrentTime = Long.parseLong((String)timeWarpObj);
                if (userCurrentTime < 0) {
                    userCurrentTime = new Date().getTime();
                }
            } catch (Throwable t) {}
        } catch (Throwable t) {}
        
        return new Date(userCurrentTime);
        
        /*HttpSession s = cmso.getRequestContext().getRequest().getSession();
        Date now = new Date();
        try {
            // Try to set the current date to the warped time (if active), fallback to the "actual" now
            CmsWorkplaceSettings settings = (CmsWorkplaceSettings)s.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
            long timewarp = settings.getUserSettings().getTimeWarp();
            if (timewarp > 1) {
                return new Date(timewarp);
            }
        } catch (Exception e) {

        }*/
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
