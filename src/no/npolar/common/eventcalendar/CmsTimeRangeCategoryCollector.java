package no.npolar.common.eventcalendar;

import java.math.BigDecimal;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Collector to fetch resources based on time and possibly also category, very
 * similar to OpenCms' own CmsTimeFrameCategoryCollector.
 * <p>
 * The main difference is that this collector can take into account a resource's 
 * end time as well as it's start time.
 * <p> 
 * The parameter string should be specified as follows: 
 * <pre>
 * key=value
 * </pre>
 * <p>
 * Multiple key-value pairs are pipe-separated: 
 * <pre>
 * key=value|key2=value2|key3=value3
 * </pre>
 * <p>
 * Reserved keys (not to be used in custom implementations): 
 * <ul>
 * <li>
 * <b>resource</b><br/>
 * The value defines the folder or single file to base the collection on.
 * </li>
 * <li>
 * <b>resourceType</b><br/>
 * The value defines the type name for the kind of resources to collect.
 * </li>
 * <li>
 * <b>resultLimit</b><br/>
 * The value defines the maximum amount of results to return.
 * </li>
 * <li>
 * <b>sortDescending</b><br/>
 * The value defines if the result is sorted in descending ("true") or ascending 
 * (anything else than "true") order. Note that sorting is done by start time.
 * </li>
 * <li>
 * <b>timeStart</b><br/>
 * The value defines the start time that will be used for the validity time 
 * frame of result candidates. The time must be given using the format 
 * <code>yyyy-MM-dd HH:mm:ss</code> (as described in <code>{@link SimpleDateFormat}</code> )
 * </li>
 * <li>
 * <b>timeEnd</b><br/>
 * The value defines the end time that will be used for the validity time 
 * frame of result candidates. The time must be given using the format 
 * <code>yyyy-MM-dd HH:mm:ss</code> (as described in <code>{@link SimpleDateFormat}</code> )
 * </li>
 * <li>
 * <b>excludeExpired</b><br/>
 * The value (true or false) determines if resources that has ended should be
 * collected or not. 
 * </li>
 * <li>
 * <b>overlapLenient</b><br/>
 * The value (true or false) determines how the collector should treat resources
 * that are not entirely within the collector's valitdity time frame (i.e. the
 * event's time frame the collector's validity time frame only partly overlap).
 * </li>
 * <li>
 * <b>categoryInclusive</b><br/>
 * The value (true or false) determines how the collector should behave when 
 * multiple categories are passed, to use as filters. There are 2 applications 
 * of multiple category filters: inclusive and exclusive. Inclusive means that 
 * a resource belonging to ANY of the categories will be collected. Exclusive 
 * means that a resource must belong to ALL of the categories to be collected.
 * </li>
 * <li>
 * <b>propertyTimeStart</b><br/>
 * The value defines the name of the property that is inspected for a time stamp 
 * in <code> {@link System#currentTimeMillis()}</code> syntax for the validity 
 * time frame check. 
 * </li>
 * <li>
 * <b>propertyTimeEnd</b><br/>
 * The value defines the name of the property that is inspected for a time stamp 
 * in <code> {@link System#currentTimeMillis()}</code> syntax for the validity 
 * time frame check. 
 * </li>
 * <li>
 * <b>propertyCategories</b><br/>
 * The value defines the name of the property that is inspected for a pipe 
 * separated list of category strings. 
 * </li>
 * <li>
 * <b>categories</b><br/>
 * The value defines a list of comma separated category Strings used to filter 
 * result candidates by. If this parameter is missing completely no category 
 * filtering will be done and also resources with empty category property will 
 * be accepted. 
 * </li>
 * </ul>
 * <p>
 * The parameter string format:<p>
 * @author Paul-Inge Flakstad <flakstad at npolar.no>
 */
public class CmsTimeRangeCategoryCollector extends A_CmsResourceCollector {
    
    /**
     * Supports a key - value syntax for collector params.<p>
     * 
     * All parameters are specified as follows: 
     * <pre>
     * key=value
     * </pre>
     * <p>
     * Many key - value pairs may exist: 
     * <pre>
     * key=value|key2=value2|key3=value3
     * </pre>
     * <p>
     * The following keys are reserved: 
     * <ul>
     * <li>
     * <b>resource</b><br/>
     * The value defines the folder / single file for collection of results. 
     * </li>
     * <li>
     * <b>resourceType</b><br/>
     * The value defines the name of the type of resource that is required for the result as 
     * defined in opencms-modules.xml, opencms-workplace.xml. 
     * </li>
     * <li>
     * <b>resultLimit</b><br/>
     * The value defines the maximum amount of results to return. 
     * </li>
     * <li>
     * <b>sortDescending</b><br/>
     * The value defines if the result is sorted in descending ("true") or ascending 
     * (anything else than "true") order. 
     * </li>
     * <li>
     * <b>timeStart</b><br/>
     * The value defines the start time that will be used for the validity time 
     * frame of result candidates. The time must be given using the format 
     * <code>yyyy-MM-dd HH:mm:ss</code> (as described in <code>{@link SimpleDateFormat}</code> )
     * </li>
     * <li>
     * <b>timeEnd</b><br/>
     * The value defines the end time that will be used for the validity time 
     * frame of result candidates. The time must be given using the format 
     * <code>yyyy-MM-dd HH:mm:ss</code> (as described in <code>{@link SimpleDateFormat}</code> )
     * </li>
     * <li>
     * <b>excludeExpired</b><br/>
     * The value (true or false) determines if resources that has ended should be
     * collected or not. 
     * </li>
     * <li>
     * <b>overlapLenient</b><br/>
     * The value (true or false) determines how the collector should treat resources
     * that are not entirely within the collector's valitdity time frame (i.e. the
     * event's time frame the collector's validity time frame only partly overlap).
     * </li>
     * <li>
     * <b>categoryInclusive</b><br/>
     * The value (true or false) determines how the collector should behave when 
     * collecting resources using multiple categories. There are two ways to 
     * treat multiple categories: inclusive and exclusive. Inclusive means that 
     * a resource belonging to ANY of the categories will be collected. Exclusive 
     * means that a resource must belong to ALL of the categories to be collected.
     * </li>
     * <li>
     * <b>propertyTimeStart</b><br/>
     * The value defines the name of the property that is inspected for a time stamp 
     * in <code> {@link System#currentTimeMillis()}</code> syntax for the validity time frame 
     * check. 
     * </li>
     * <li>
     * <b>propertyTimeEnd</b><br/>
     * The value defines the name of the property that is inspected for a time stamp 
     * in <code> {@link System#currentTimeMillis()}</code> syntax for the validity time frame 
     * check. 
     * </li>
     * <li>
     * <b>propertyCategories</b><br/>
     * The value defines the name of the property that is inspected for a pipe separated 
     * list of category strings. 
     * </li>
     * <li>
     * <b>categories</b><br/>
     * The value defines a list of comma separated category Strings used to filter 
     * result candidates by. If this parameter is missing completely no category 
     * filtering will be done and also resources with empty category property will 
     * be accepted. 
     * </li>
     * </ul>
     * <p>
     */
    protected class CollectorDataPropertyBased extends CmsCollectorData {

        /** The collector parameter key for the categories: value is a list of comma - separated Strings. */
        public static final String PARAM_KEY_CATEGORIES = "categories";
        
        /** The collector parameter key for URIs of folders to exclude. */
        public static final String PARAM_KEY_EXCLUDE_FOLDERS = "excludeFolders";

        /** The collector parameter key for the name of the categories property used to filter resources by. */
        public static final String PARAM_KEY_PROPERTY_CATEGORIES = "propertyCategories";

        /** The collector parameter key for the name of the property to use for the start time. */
        public static final String PARAM_KEY_PROPERTY_TIME_START = "propertyTimeStart";
        
        /** The collector parameter key for the name of the property to use for the end time. */
        public static final String PARAM_KEY_PROPERTY_TIME_END = "propertyTimeEnd";

        /** The collector parameter key for the resource (folder / file). */
        public static final String PARAM_KEY_RESOURCE = "resource";

        /** The collector parameter key for the resource type. */
        public static final String PARAM_KEY_RESOURCE_TYPE = "resourceType";

        /** The collector parameter key for a result limit. */
        public static final String PARAM_KEY_RESULT_LIMIT = "resultLimit";

        /** The collector parameter key for descending sort order. */
        public static final String PARAM_KEY_SORT_DESCENDING = "sortDescending";

        /** The collector parameter key for the end time of the validity time frame. */
        public static final String PARAM_KEY_TIMEFRAME_END = "timeEnd";

        /** The collector parameter key for the start time of the validity time frame. */
        public static final String PARAM_KEY_TIMEFRAME_START = "timeStart";
        
        /** The collector parameter key for the exclusion of 'expired' resources (resources with end times in the past). 
         * 'true' will exclude any resource that has an end time in the past (relative to the actual current time). 
         * 'false' will not exclude such resources. 
         * Default is 'false'.
         */
        public static final String PARAM_KEY_EXCLUDE_EXPIRED = "excludeExpired";
        
        /**
         * The collector parameter key for the 'include recurrences' setting.
         * <p>
         * Default is <code>true</code>.
         */
        public static final String PARAM_KEY_INCLUDE_RECURRENCES = "includeRecurrences";
        
        /** The collector parameter key for overlap leniency. 
         * 'false' will require the resource's entire time range to be within the collector's time range (more restrictive than 'true'). 
         * 'true' will require the resource's time range to somehow overlap the collector's time range (less restrictive than 'false'). 
         * Default is 'true'.
         */
        public static final String PARAM_KEY_OVERLAP_LENIENT = "overlapLenient";
        
        /**
         * The collector parameter key for how to treat multiple categories in the parameter string. 
         * 'false' (exclusive) will require a resource to belong to ALL of the categories (more restrictive than 'true'). 
         * 'true' (inclusive) will require a resource to belong to ANY of the categories (less restrictive than 'false'). 
         * Default is 'true'.
         */
        public static final String PARAM_KEY_CATEGORY_INCLUSIVE = "categoryInclusive";

        /** List containing the (root paths of) categories to allow (if any). */
        private List m_categories = Collections.EMPTY_LIST;
        
        /** List containing folders to exclude (if any). */
        private List m_excludedFolders = Collections.EMPTY_LIST;

        /** The display count. */
        private int m_count;

        /** The resource path (folder / file). */
        private String m_fileName;

        /** The property to look for a pipe separated list of category strings in.*/
        private CmsProperty m_propertyCategories = new CmsProperty();

        /** The property to look up for a time stamp on result candidates for validity time frame check.*/
        private CmsProperty m_propertyTimeStart = new CmsProperty();
        
        /** The property to look up for a time stamp on result candidates for validity time frame check.*/
        private CmsProperty m_propertyTimeEnd = new CmsProperty();

        /** If true results should be sorted in descending order.*/
        private boolean m_sortDescending;
        
        /** If true, result lists should not contain any candidates with end time in the past. */
        private boolean m_excludeExpired = false;
        
        /** If true, result lists should include recurrences. */
        private boolean m_includeRecurrences = true;
        
        /** Flag for how to determine if a resource is within the validity time frame. If true, a resource will only have to overlap the validity time frame, not be completely within it. */
        private boolean m_overlapLenient = true;
        
        /** Flag for how to treat multiple categories. If true, when filtering on multiple categories, a resource will need only to belong to any of the categories. */
        private boolean m_categoryInclusive = true;

        /** The end of the validity time frame. */
        private long m_timeFrameEnd = Long.MAX_VALUE;

        /** The start of the validity time frame. */
        private long m_timeFrameStart = Long.MIN_VALUE;

        /** The resource type to require. */
        private I_CmsResourceType m_type;

        /**
         * Constructor with the collector param of the tag.<p>
         * 
         * @param data the param attribute value of the contentload tag. 
         * 
         * @throws CmsLoaderException if the collector param specifies an illegal resource type.
         * 
         */
        public CollectorDataPropertyBased(String data)
        throws CmsLoaderException {

            try {
                parseParam(data);
            } catch (ParseException pe) {
                CmsRuntimeException ex = new CmsIllegalArgumentException(org.opencms.file.collectors.Messages.get().container(
                    org.opencms.file.collectors.Messages.ERR_COLLECTOR_PARAM_DATE_FORMAT_SYNTAX_0));
                ex.initCause(pe);
                throw ex;
            }

        }

        /**
         * Returns the List containing the (root paths of) categories to allow.<p>
         *
         * @return The List containing the (root paths of) categories to allow.
         */
        public List<String> getCategories() {
            return m_categories;
        }
        
        /**
         * Returns the list containing excluded folders (if any).<p>
         *
         * @return The list containing excluded folders, or an empty list if none.
         */
        public List getExcludedFolders() {
            return m_excludedFolders;
        }

        /**
         * Returns the count.
         * <p>
         * 
         * @return the count
         */
        @Override
        public int getCount() {

            return m_count;
        }

        /**
         * Returns the file name.<p>
         * 
         * @return the file name
         */
        @Override
        public String getFileName() {

            return m_fileName;
        }

        /**
         * Returns the property to look for a pipe separated list of category strings in.<p>
         *
         * Never write this property to VFS as it is "invented in RAM" and not 
         * read from VFS!<p>
         * 
         * @return the property to look for a pipe separated list of category strings in.
         */
        public CmsProperty getPropertyCategories() {

            return m_propertyCategories;
        }

        /**
         * Returns The property to look up for a time stamp 
         * on result candidates for validity time frame check.<p>
         * 
         * Never write this property to VFS as it is "invented in RAM" and not 
         * read from VFS!<p>
         *
         * @return The property to look up for a time stamp on result candidates for validity time frame check.
         */
        public CmsProperty getPropertyTimeStart() {

            return m_propertyTimeStart;
        }
        
        /**
         * Returns The property to look up for a time stamp 
         * on result candidates for validity time frame check.<p>
         * 
         * Never write this property to VFS as it is "invented in RAM" and not 
         * read from VFS!<p>
         *
         * @return The property to look up for a time stamp on result candidates for validity time frame check.
         */
        public CmsProperty getPropertyTimeEnd() {

            return m_propertyTimeEnd;
        }

        /**
         * Returns the timeFrameEnd.<p>
         *
         * @return the timeFrameEnd
         * 
         * @see #getPropertyTime()
         */
        public long getTimeFrameEnd() {

            return m_timeFrameEnd;
        }

        /**
         * Returns the timeFrameStart.<p>
         *
         * @return the timeFrameStart
         */
        public long getTimeFrameStart() {

            return m_timeFrameStart;
        }

        /**
         * Returns the type ID for resources to collect.
         * <p>
         * 
         * @return the type
         */
        @Override
        public int getType() {

            return m_type.getTypeId();
        }

        /**
         * If true, results should be sorted in descending order.<p>
         * 
         * Defaults to true.<p>
         *
         * @return true if results should be sorted in descending order, false 
         *      if results should be sorted in ascending order.
         */
        public boolean isSortDescending() {

            return m_sortDescending;
        }
        
        /**
         * If true, results should not have end times in the past.<p>
         * 
         * Defaults to false.<p>
         *
         * @return true if results should not have end times in the past, false 
         *      if end time should be disregarded.
         */
        public boolean isExcludeExpired() {
            return m_excludeExpired;
        }
        
        /**
         * If true, recurrences may appear in the results.
         * <p>
         * Defaults to true.
         * 
         * @return true if the recurrences are included in results, false if not.
         */
        public boolean isIncludeRecurrences() {
            return m_includeRecurrences;
        }
        
        /**
         * Defines whether the collector's timerange is restrictive or inclusive.<p>
         * 
         * True indicates inclusive, resources need only have their time range overlap the collector's time range in some way.
         * False indicates restrictive, resources need to have their entire time range within the collector's time range.<p>
         * 
         * Defaults to true.<p>
         * 
         * @return false if the overlap leniency is restrictive, true if it is inclusive.
         */
        public boolean isOverlapLenient() {
            return m_overlapLenient;
        }
        
        /**
         * Defines how the collector treats multiple categories.<p>
         * 
         * True indicates inclusive, resources need only belong to ANY (but at least one) of the multiple categories.
         * False indicates restrictive, resources must belong to ALL of the multiple categories .<p>
         * 
         * Defaults to true (keeping with standard OpenCms behaviour).<p>
         * 
         * @return true if inclusive, false if restrictive.
         */
        public boolean isCategoryInclusive() {
            return m_categoryInclusive;
        }

        /**
         * Internally parses the constructor-given param into the data model 
         * of this instance.<p> 
         * 
         * @param param the constructor-given param. 
         * 
         * @throws CmsLoaderException if the collector param specifies an illegal resource type.
         * 
         * @throws ParseException if date parsing in scope of the param attribute fails. 
         */
        private void parseParam(final String param) throws CmsLoaderException, ParseException {

            List keyValuePairs = CmsStringUtil.splitAsList(param, '|');
            String[] keyValuePair;
            Iterator itKeyValuePairs = keyValuePairs.iterator();
            String keyValuePairStr;
            String key;
            String value;
            while (itKeyValuePairs.hasNext()) {
                keyValuePairStr = (String)itKeyValuePairs.next();
                keyValuePair = CmsStringUtil.splitAsArray(keyValuePairStr, '=');
                if (keyValuePair.length != 2) {
                    throw new CmsIllegalArgumentException(org.opencms.file.collectors.Messages.get().container(
                        org.opencms.file.collectors.Messages.ERR_COLLECTOR_PARAM_KEY_VALUE_SYNTAX_1,
                        new Object[] {keyValuePairStr}));
                }
                key = String.valueOf(keyValuePair[0]).trim();
                value = String.valueOf(keyValuePair[1]).trim();

                if (PARAM_KEY_RESOURCE.equals(key)) {
                    m_fileName = value;
                } else if (PARAM_KEY_RESOURCE_TYPE.equals(key)) {
                    m_type = OpenCms.getResourceManager().getResourceType(value);
                } else if (PARAM_KEY_RESULT_LIMIT.equals(key)) {
                    m_count = Integer.parseInt(value);
                } else if (PARAM_KEY_SORT_DESCENDING.equals(key)) {
                    m_sortDescending = Boolean.valueOf(value).booleanValue();
                } else if (PARAM_KEY_TIMEFRAME_START.equals(key)) {
                    try {
                        m_timeFrameStart = DATEFORMAT_SQL.parse(value).getTime();
                    } catch (Exception e) {
                        try {
                            m_timeFrameStart = Long.parseLong(value);
                        } catch (Exception ee) {
                            try {
                                m_timeFrameStart = new BigDecimal(value).longValue();
                            } catch (Exception eee) {
                                m_timeFrameStart = 0;
                                if (LOG.isErrorEnabled()) {
                                    LOG.error("Unable to parse start time '" + value + "' for time range collector. (Collecting from " + m_fileName + ".)", eee);
                                }
                            }
                        }
                    }
                } else if (PARAM_KEY_TIMEFRAME_END.equals(key)) {
                    try {
                        m_timeFrameEnd = DATEFORMAT_SQL.parse(value).getTime();
                    } catch (Exception e) {
                        try {
                            m_timeFrameEnd = Long.parseLong(value);
                        } catch (Exception ee) {
                            try {
                                m_timeFrameEnd = new BigDecimal(value).longValue();
                            } catch (Exception eee) {
                                m_timeFrameEnd = 0;
                                if (LOG.isErrorEnabled()) {
                                    LOG.error("Unable to parse end time '" + value + "' for time range collector. (Collecting from " + m_fileName + ".)", eee);
                                }
                            }
                        }
                    }
                } else if (PARAM_KEY_PROPERTY_TIME_START.equals(key)) {
                    m_propertyTimeStart.setName(value);
                } else if (PARAM_KEY_PROPERTY_TIME_END.equals(key)) {
                    m_propertyTimeEnd.setName(value);
                } else if (PARAM_KEY_CATEGORIES.equals(key)) {
                    m_categories = CmsStringUtil.splitAsList(value, ',');
                } else if (PARAM_KEY_EXCLUDE_FOLDERS.equals(key)) {
                    m_excludedFolders = CmsStringUtil.splitAsList(value, ',');
                } else if (PARAM_KEY_PROPERTY_CATEGORIES.equals(key)) {
                    m_propertyCategories.setName(value);
                } else if (PARAM_KEY_EXCLUDE_EXPIRED.equals(key)) {
                    m_excludeExpired = Boolean.valueOf(value).booleanValue();
                } else if (PARAM_KEY_INCLUDE_RECURRENCES.equals(key)) {
                    m_includeRecurrences = Boolean.valueOf(value).booleanValue();
                } else if (PARAM_KEY_OVERLAP_LENIENT.equals(key)) {
                    m_overlapLenient = Boolean.valueOf(value).booleanValue();
                } else if (PARAM_KEY_CATEGORY_INCLUSIVE.equals(key)) {
                    m_categoryInclusive = Boolean.valueOf(value).booleanValue();
                } else {
                    // now, one could accept additional filter properties here...
                }

            }

        }

    } // End of inner class CollectorDataPropertyBased
    
     /** Static array of the collectors implemented by this class. */
    protected static final String COLLECTOR_NAME = "timeRangeAndCategories";

    /** Sorted set for fast collector name lookup. */
    private static final List COLLECTORS_LIST = Collections.unmodifiableList(Arrays.asList( new String[] {COLLECTOR_NAME} ));

    /** SQL Standard date format: "yyyy-MM-dd HH:mm:ss".*/
    public static final DateFormat DATEFORMAT_SQL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /** The logger. */
    private static final Log LOG = LogFactory.getLog(CmsTimeRangeCategoryCollector.class);
    
    /** Collector parameters */
    protected CollectorDataPropertyBased data = null;
    
    /**
     * Default constructor, uses only parent constructor.
     */
    public CmsTimeRangeCategoryCollector() {
        super();
    }
    
     /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List getCollectorNames() {

        return new ArrayList(COLLECTORS_LIST);
    }

    /**
     * @see org.opencms.file.collectors.A_CmsResourceCollector#getCreateInFolder(org.opencms.file.CmsObject, org.opencms.file.collectors.CmsCollectorData)
     */
    @Override
    protected String getCreateInFolder(CmsObject cms, CmsCollectorData data) throws CmsException {

        return super.getCreateInFolder(cms, data);
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateLink(CmsObject cms, String collectorName, String param)
    throws CmsException, CmsDataAccessException {

        // if action is not set, use default action
        if (collectorName == null) {
            collectorName = COLLECTOR_NAME;
        }
        if (COLLECTOR_NAME.equals(collectorName)) {
            return getCreateInFolder(cms, new CollectorDataPropertyBased(param));
        } else {
            throw new CmsDataAccessException(org.opencms.file.collectors.Messages.get().container(
                org.opencms.file.collectors.Messages.ERR_COLLECTOR_NAME_INVALID_1,
                collectorName));
        }
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateParam(CmsObject cms, String collectorName, String param) {

        return null;
    }

    /**
     * Gets any resource that meets the requirements defined in the collector's parameters.<p>
     * 
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
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
            //return getTimeRangeAndCategories(cms, param, false, false); // false => The resource timeframes must be completely inside the collectors timeframe
            //return getPartialOverlapResults(cms, collectorName, param, false);
            //return getPartialOverlapResults(cms, collectorName);
            return getTimeRangeAndCategories(cms);
        } else {
            throw new CmsDataAccessException(org.opencms.file.collectors.Messages.get().container(
                org.opencms.file.collectors.Messages.ERR_COLLECTOR_NAME_INVALID_1,
                collectorName));
        }
    }
    
    /**
     * Gets any resource that has a timerange that in some way overlaps with the collector's timerange.<p>
     * 
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    /*//private List getPartialOverlapResults(CmsObject cms, String collectorName, String param, boolean inclusive)
    private List getPartialOverlapResults(CmsObject cms, String collectorName)
    throws CmsDataAccessException, CmsException {
        // if action is not set use default
        if (collectorName == null) {
            collectorName = COLLECTOR_NAME;
        }
        
        
        
        if (COLLECTOR_NAME.equals(collectorName)) {
            return getTimeRangeAndCategories(cms, param, true, inclusive); // true => The resource timerange does NOT need to be completely inside the collectors timerange
        } else {
            throw new CmsDataAccessException(org.opencms.file.collectors.Messages.get().container(
                org.opencms.file.collectors.Messages.ERR_COLLECTOR_NAME_INVALID_1,
                collectorName));
        }
    }*/

    //private List getTimeRangeAndCategories(CmsObject cms, boolean includePartialOverlaps, boolean inclusive) throws CmsException {
    private List getTimeRangeAndCategories(CmsObject cms) throws CmsException {

        List result = null;
        
        // Step 1: Read from DB, expiration is respected.
        String foldername = CmsResource.getFolderPath(data.getFileName());
        CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(data.getType()).addExcludeFlags(
            CmsResource.FLAG_TEMPFILE);
        result = cms.readResources(foldername, filter, true); // Fetch all folder resources, filtered by type and flags

        // Step 2: Time range filtering
        //String timeStartProperty = this.data.getPropertyTimeStart().getName();  // The name of the starting time property
        //String timeEndProperty = this.data.getPropertyTimeEnd().getName();      // The name of the ending time property
        
        //long start = this.data.getTimeFrameStart();
        //long end = this.data.getTimeFrameEnd();
        
        long resStartTime = 0;
        long resEndTime = 0;
        
        Date now = new Date();
        
        Iterator itResults = result.iterator(); // Iterate over the previously fetched folder resources
        CmsProperty timeStartProp;
        CmsProperty timeEndProp;
        
        
        CmsResource res;
        CmsProperty prop;
        
        while (itResults.hasNext()) {
            res = (CmsResource)itResults.next();
            timeStartProp = cms.readPropertyObject(res, data.getPropertyTimeStart().getName(), true);
            timeEndProp = cms.readPropertyObject(res, data.getPropertyTimeEnd().getName(), true);
            
            if (timeStartProp.isNullProperty()) {
                // No start time, impossible to determine if resource is within time range
                itResults.remove();
                continue;
            }
            
            // Code reaches this point ==> a start time exists
            resStartTime = Long.valueOf(timeStartProp.getValue());
            
            // Handle the case "only start time set, no end time"
            if (timeEndProp.isNullProperty()) {
                // Remove the resource if the start time is not within the timerange
                if ((resStartTime < data.getTimeFrameStart()) || (resStartTime > data.getTimeFrameEnd())) { 
                    itResults.remove();
                    continue;
                }
                // Remove the resourc if the start time is in the past 
                if (data.isExcludeExpired()) { // Only start date, and start time is in the past, so it is "expired"
                    if (resStartTime > 0 && resStartTime < now.getTime()) {
                        itResults.remove();
                        continue;
                    }
                }
            }
            
            // Handle the case "both start time and end time set"
            else {
                resEndTime = Long.valueOf(timeEndProp.getValue());
                //boolean resEndInTimerange = (resEndTime >= data.getTimeFrameStart()) && (resEndTime <= data.getTimeFrameEnd());
                //boolean resStartInTimeRange = (resStartTime >= data.getTimeFrameStart()) && (resStartTime <= data.getTimeFrameEnd());
                //if (includePartialOverlaps) {
                if (data.isOverlapLenient()) { // Lenient mode
                    // Remove the resource if the start is not within the timeframe and the end is not within the timeframe
                    if ((resEndTime < data.getTimeFrameStart()) ||  // If the resource "ends" before the collector's timeframe start OR ...
                            (resStartTime > data.getTimeFrameEnd())) {  // ... if it "starts" after the collector's timeframe end
                        itResults.remove();
                        continue;
                    }
                }
                else { // Non-lenient mode
                    // Remove the resource if the resource's timeframe is not completely within the collector's timeframe
                    //if (!((resStartTime >= data.getTimeFrameStart()) && (resEndTime <= data.getTimeFrameEnd()))) {
                    // Above line was replaced - we'll examine only the resource's start time, and ignore its end time:
                    // Remove the resource if the resource's start time is not within the collector's timeframe
                    if (!((resStartTime >= data.getTimeFrameStart()) && (resStartTime <= data.getTimeFrameEnd()))) {
                        itResults.remove();
                        continue;
                    }
                }
                // Remove resources with end time in the past
                if (data.isExcludeExpired()) {
                    if (resEndTime > 0 && resEndTime < now.getTime()) {
                        itResults.remove();
                        continue;
                    }
                }
            }
            /*else {
                resEndTime = Long.valueOf(timeEndProp.getValue());
                // if NOT resource's timerange is within the collector's timeframe
                if (!((resStartTime >= data.getTimeFrameStart()) && (resEndTime <= data.getTimeFrameEnd()))) {
                    itResults.remove();
                }
            }*/
        }

        // Step 3: Category filtering
        // We'll return an "exclusive" resource list, one that contains only
        // resources that have been assigned ALL selected categories
        List categories = data.getCategories(); // Get the list of selected categories
        if ((categories != null) && !categories.isEmpty()) {
            itResults = result.iterator();
            String categoriesProperty = data.getPropertyCategories().getName();
            List categoriesFound;
            while (itResults.hasNext()) { // Loop over all collected resources
                res = (CmsResource)itResults.next();
                prop = cms.readPropertyObject(res, categoriesProperty, true);
                if (prop.isNullProperty()) { // If the resource hasn't been assigned any category, remove it
                    // disallow contents with empty category property: 
                    itResults.remove();
                    // accept contents with empty category property: 
                    // continue;
                } else { // One or more categories has been assigned to the resource
                    categoriesFound = CmsStringUtil.splitAsList(prop.getValue(), '|');

                    //if (inclusive) {
                    if (data.isCategoryInclusive()) {
                        // filter: resource has to be at least in one category
                        Iterator itCategories = categories.iterator();
                        String category;
                        boolean contained = false;
                        while (itCategories.hasNext()) {
                            category = (String)itCategories.next();
                            if (categoriesFound.contains(category)) {
                                contained = true;
                                break;
                            }
                        }
                        if (!contained) {
                            itResults.remove();
                        }
                    }
                    else {
                        // filter: resource has to be in ALL selected categories
                        if (!categoriesFound.containsAll(categories))
                            itResults.remove();
                    }
                }
            }
        }

        // Step 4: Sorting
        
        // Create a comparator for collector.date (date released can't be used)
        List datePropertiesForCoparison = new ArrayList();
        datePropertiesForCoparison.add(EventEntry.PROPERTY_TIME_START);//add("collector.date"); //removed hard coding, use the static final string instead
        CmsDateResourceComparator dateComparator = new CmsDateResourceComparator(cms, datePropertiesForCoparison, true);
        if (data.isSortDescending()) {
            dateComparator = new CmsDateResourceComparator(cms, datePropertiesForCoparison, false);
        }
        /*
        if (data.isSortDescending()) {
            Collections.sort(result, CmsResource.COMPARE_DATE_RELEASED);
        } else {
            Collections.sort(result, new ComparatorInverter(CmsResource.COMPARE_DATE_RELEASED));
        }
        */
        
        // Sort results using the defined comparator
        Collections.sort(result, dateComparator);
        
        // Step 5: result limit
        return shrinkToFit(result, data.getCount());
    }
    
    /**
     * Returns the start of the time range used by this collector.
     * 
     * @return the start of the time range used by this collector.
     */
    public long getTimeRangeStart() {
        return this.data.getTimeFrameStart();
    }
    /**
     * Returns the end of the time range used by this collector.
     * 
     * @return the end of the time range used by this collector.
     */
    public long getTimeRangeEnd() {
        return this.data.getTimeFrameEnd();
    }
    
    /**
     * Returns the maximum number of returned resources.
     * 
     * @return the maximum number of returned resources
     */
    public int getCount() {
        return this.data.getCount();
    }
}
