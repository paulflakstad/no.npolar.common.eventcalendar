package no.npolar.common.eventcalendar;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 *
 * @author Paul-Inge Flakstad <flakstad at npolar.no>
 */
public class Messages extends A_CmsMessageBundle {
    /** Message constant for key in the resource bundle. */
    public static final String MSG_NUM_EVENTS_IN_YEAR_3 = "MSG_NUM_EVENTS_IN_YEAR_3";
    /** Message constant for key in the resource bundle. */
    public static final String MSG_NUM_EVENTS_IN_MONTH_4 = "MSG_NUM_EVENTS_IN_MONTH_4";
    /** Message constant for key in the resource bundle. */
    public static final String MSG_NUM_EVENTS_IN_WEEK_4 = "MSG_NUM_EVENTS_IN_WEEK_4";
    /** Message constant for key in the resource bundle. */
    public static final String MSG_NUM_EVENTS_IN_DATE_5 = "MSG_NUM_EVENTS_IN_DATE_5";
    /** Message constant for key in the resource bundle. */
    public static final String MSG_NUM_EVENTS_IN_INTERVAL_4 = "MSG_NUM_EVENTS_IN_YEAR_3";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "no.npolar.common.eventcalendar.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private Messages() {
        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.<p>
     * 
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {
        return INSTANCE;
    }

    /**
     * Returns the bundle name for this OpenCms package.<p>
     * 
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {
        return BUNDLE_NAME;
    }
}
