package org.xmpp.extension.activity;

/**
 * Represents the category or general activity.
 *
 * @author Christian Schudt
 */
public enum Category {

    /**
     * Doing chores.
     */
    DOING_CHORES(Activity.AbstractCategory.DoingChores.class),
    /**
     * Drinking.
     */
    DRINKING(Activity.AbstractCategory.Drinking.class),
    /**
     * Eating.
     */
    EATING(Activity.AbstractCategory.Eating.class),
    /**
     * Exercising.
     */
    EXERCISING(Activity.AbstractCategory.Exercising.class),
    /**
     * Grooming.
     */
    GROOMING(Activity.AbstractCategory.Grooming.class),
    /**
     * Having appointment.
     */
    HAVING_APPOINTMENT(Activity.AbstractCategory.HavingAppointment.class),
    /**
     * Inactive.
     */
    INACTIVE(Activity.AbstractCategory.Inactive.class),
    /**
     * Relaxing.
     */
    RELAXING(Activity.AbstractCategory.Relaxing.class),
    /**
     * Talking.
     */
    TALKING(Activity.AbstractCategory.Talking.class),
    /**
     * Traveling.
     */
    TRAVELING(Activity.AbstractCategory.Traveling.class),
    /**
     * Undefined.
     */
    UNDEFINED(Activity.AbstractCategory.Undefined.class),
    /**
     * Working.
     */
    WORKING(Activity.AbstractCategory.Working.class);

    Class<? extends Activity.AbstractCategory> categoryClass;

    private Category(Class<? extends Activity.AbstractCategory> categoryClass) {
        this.categoryClass = categoryClass;
    }
}
