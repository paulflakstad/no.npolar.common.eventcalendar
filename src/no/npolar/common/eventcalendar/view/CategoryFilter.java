package no.npolar.common.eventcalendar.view;

/**
 * A category filter.
 * <p>
 * When a filter is applied, the list should contain only events that are tagged 
 * with the filter's corresponding category.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
import org.opencms.relations.CmsCategory;

public class CategoryFilter {
    /** The title/name for this filter - identical to the corresponding category's title. */
    private String name = null;
    /** The URI for this filter - identical to the corresponding category's relative path. */
    private String uri  = null;
    /** The number of matches for this filter. */
    private int counter = 0;
    /** This filter's corresponding category. */
    private CmsCategory category = null;
    
    /**
     * Creates a new filter from the given category.
     * 
     * @param category  This filter's corresponding category.
     */
    public CategoryFilter(CmsCategory category) {
        this.name = category.getTitle();
        this.uri = category.getPath();
        this.category = category;
    }
    
    /**
     * Creates a new filter from the given category and counter.
     * 
     * @param counter The number of hits for this category filter.
     * @param category  This filter's corresponding category.
     */
    public CategoryFilter(CmsCategory category, int counter) {
        this(category);
        this.counter = counter;
    }
    
    /** Increments the counter for this filter. (Adds one match.) */
    public void incrementCounter() { this.counter++; }
    /** Gets the name (title) for this filter's corresponding category. */
    public String getName() { return this.name; }
    /** Gets the URI (path) for this filter's corresponding category. */
    public String getUri() { return this.uri; }
    /** Gets the counter for this filter -- that is, how many resources match the filter. */
    public int getCounter() { return this.counter; }
    /** Gets this filter's corresponding category. */
    public CmsCategory getCategory() { return this.category; }
    
    /**
     * Gets a complete link with a URL containing the correct parameters to either apply or remove the given filter.
     * 
     * @param parameterNameCategory The parameter name used for category filters (typically "cat").
     * @param linkTarget The resource that this link should point to, without parameters.
     * @param requestParameterString The parameter string for the current page.
     * 
     * @return A complete link with a URL containing the correct parameters to either apply or remove the given filter.
     */
    public String getApplyOrRemoveLink(String parameterNameCategory, String linkTarget, String requestParameterString) {
        String filterLink = "";
        if (requestParameterString.contains(this.getUri())) {
            filterLink = "<a style=\"font-weight:bold;\" class=\"remove filter filter--active\" href=\"" + linkTarget + "?" + requestParameterString;
            filterLink = filterLink.replace(parameterNameCategory + "=".concat(this.getUri()), "");
            filterLink = filterLink.replace("&amp;&amp;", "&amp;"); // If multiple categories were in the URL, we just created "&&"
            filterLink = filterLink.endsWith("?") ? filterLink.substring(0, filterLink.length() - 1) : filterLink; // Remove trailing ?
            filterLink = filterLink.endsWith("&amp;") ? filterLink.substring(0, filterLink.length() - 5) : filterLink; // Remove trailing &
            filterLink += "\" rel=\"nofollow\">" + this.getName() + "</a>";
        }
        else {
            filterLink = "<a class=\"filter\" href=\"" + linkTarget + "?" + 
                                        requestParameterString + "&amp;" + parameterNameCategory + "=" + this.getUri() + "\" rel=\"nofollow\">" + 
                                        this.getName() + "<span class=\"filter__num-matches\"> (" + this.getCounter() + ")</span></a>";
        }
        return filterLink;
    }
    
    /** Overrides Object#equals(Object): Returns true if the names and URIs are identical. */
    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null) 
            return false;
        if (!(that instanceof CategoryFilter))
            return false;
        return this.name.equals(((CategoryFilter)that).name) && this.uri.equals(((CategoryFilter)that).uri);
    } 
}