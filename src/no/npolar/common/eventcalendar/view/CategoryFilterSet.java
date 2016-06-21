package no.npolar.common.eventcalendar.view;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.opencms.relations.CmsCategory;

/**
 * A set of filters.
 * <p>
 * The set is identified by its root/parent category, and each filter in the 
 * set must be a sub-category of that category.
 * <p>
 * e.g.: 
 * Topic (the root)
 *  - Climate (category)
 *  - Biodiversity (category)
 *  - Pollutants (category)
 * 
 * @author flakstad
 */
public class CategoryFilterSet {
    /** The category filters in this set */
    private List<CategoryFilter> filterSet = null;
    /** The root category for this set */
    private CmsCategory rootCategory = null;
    
    /** Sort mode for sorting by relevancy (high number of matches = high relevancy) */
    public static final int SORT_MODE_RELEVANCY           = 2;
    /** Sort mode for sorting alphabetically by title */
    public static final int SORT_MODE_TITLE               = 1;
    /** Sort mode for sorting alphabetically by resource name (the filter category's URI) */
    public static final int SORT_MODE_RESOURCENAME        = 0;
    /** Comparator used for sorting by the counter (relevancy) */
    private final Comparator<CategoryFilter> COMPARATOR_COUNTER =
            new Comparator<CategoryFilter>() {
                public int compare(CategoryFilter o1, CategoryFilter o2) {
                    return o1.getCounter() > o2.getCounter() ? -1 : o1.getCounter() == o2.getCounter() ? 0 : 1;
                }
            };
    /** Comparator used for sorting by title */
    private final Comparator<CategoryFilter> COMPARATOR_TITLE =
            new Comparator<CategoryFilter>() {
                public int compare(CategoryFilter o1, CategoryFilter o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            };
    /** Comparator used for sorting by URI */
    private final Comparator<CategoryFilter> COMPARATOR_URI =
            new Comparator<CategoryFilter>() {
                public int compare(CategoryFilter o1, CategoryFilter o2) {
                    return o1.getUri().compareTo(o2.getUri());
                }
            };
    
    /** 
    * Creates a new category filter set, with the given root/parent category. 
    * 
    * @param rootCategory  The category to use as root/parent category
    */
    public CategoryFilterSet(CmsCategory rootCategory) {
        this.filterSet = new ArrayList<CategoryFilter>();
        this.rootCategory = rootCategory;
    }
    
    /** 
     * Gets the category filters in this set. 
     * 
     * @return The category filters in this set.
     */
    public List<CategoryFilter> getCategoryFilters() { return this.filterSet; }
    
    /** 
    * Adds a category filter to this set, and increments the counter for that 
    * filter.
    * 
    * @param filter  The filter to add to this set.
    */
    public void addCategoryFilter(CategoryFilter filter) {
        if (!this.filterSet.contains(filter)) {
            this.filterSet.add(filter);
        }
        filter = this.filterSet.get(this.filterSet.indexOf(filter));
        filter.incrementCounter();
    }
    
    /** 
     * Gets the title for this category filter set, which is identical to the 
     * parent/root category's title. 
     * 
     * @return the title for this category filter set.
     */
    public String getTitle() { 
        return this.rootCategory.getTitle(); 
    }
    
    /** 
    * Sorts the filters currently in this set according to the given sort mode. 
    * 
    * @param sortMode  The sort mode (possible values are given by the SORT_MODE_XXX members of this class).
    */
    public void sortCategoryFilters(int sortMode) {
        switch (sortMode) {
            case SORT_MODE_RELEVANCY:
                Collections.sort(filterSet, COMPARATOR_TITLE);
                Collections.sort(filterSet, COMPARATOR_COUNTER);
                break;
            case SORT_MODE_TITLE:
                Collections.sort(filterSet, COMPARATOR_TITLE);
                break;
            case SORT_MODE_RESOURCENAME:
                Collections.sort(filterSet, COMPARATOR_URI);
                break;
            default:
                break;
        }
    }
    
    /**
    * Removes from this set all category filters whose category corresponds to 
    * any of the given excluded categories or their sub-categories.
    * 
    * @param excludedCategories  The list of excluded categories to evaluate when removing filters from this set.
    */
    public void excludeAll(List<CmsCategory> excludedCategories) {
        Iterator<CategoryFilter> iFilters = this.filterSet.iterator();
        while (iFilters.hasNext()) {
            CategoryFilter filter = iFilters.next();
            
            Iterator<CmsCategory> noShowItr = excludedCategories.iterator();
            while (noShowItr.hasNext()) { // Loop all categories that are excluded in the config file
                if (isCategoryOrSubCategory(filter.getCategory(), noShowItr.next())) {
                    iFilters.remove(); // This filter is for an excluded category: remove it
                    break; // break the inner while loop
                }
            }
        }
    }
    
    /** 
     * Tests if a given test category is a sub-category of or identical to a 
     * reference category.
     * 
     * @param test The category to test.
     * @param reference The reference category, to test against.
     * @return true if the the given test category is a sub-category of the reference category or the reference category itself, false if not.
     */
    public static boolean isCategoryOrSubCategory(CmsCategory test, CmsCategory reference) {
        if (test.getRootPath().startsWith(reference.getRootPath())) {
            return true;
        }
        return false;
    }
    
    /** 
     * Overrides Object#equals(Object): Returns true if the root/parent 
     * categories' root paths are identical. 
     */
    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null) 
            return false;
        if (!(that instanceof CategoryFilter))
            return false;
        return this.rootCategory.getRootPath().equals(((CategoryFilterSet)that).rootCategory.getRootPath());
    }
}