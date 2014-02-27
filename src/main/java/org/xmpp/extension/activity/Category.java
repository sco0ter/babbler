/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.xmpp.extension.activity;

import javax.xml.bind.annotation.XmlElementRef;

/**
 * An abstract class for general activities.
 *
 * @author Christian Schudt
 */
public abstract class Category {

    @XmlElementRef
    private SpecificActivity specificActivity;

    private Category() {
    }

    private Category(SpecificActivity specificActivity) {
        this.specificActivity = specificActivity;
    }

    public SpecificActivity getSpecificActivity() {
        return specificActivity;
    }

    /**
     * The "doing_chores" activity.
     */
    public static final class DoingChores extends Category {
        public DoingChores() {
        }

        public DoingChores(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    /**
     * The "drinking" activity.
     */
    public static final class Drinking extends Category {
        public Drinking() {
        }

        public Drinking(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    /**
     * The "eating" activity.
     */
    public static final class Eating extends Category {
        public Eating() {
        }

        public Eating(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    /**
     * The "exercising" activity.
     */
    public static final class Exercising extends Category {
        public Exercising() {
        }

        public Exercising(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    /**
     * The "grooming" activity.
     */
    public static final class Grooming extends Category {
        public Grooming() {
        }

        public Grooming(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    /**
     * The "having_appointment" activity.
     */
    public static final class HavingAppointment extends Category {
        public HavingAppointment() {
        }

        public HavingAppointment(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    /**
     * The "inactive" activity.
     */
    public static final class Inactive extends Category {
        public Inactive() {
        }

        public Inactive(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    /**
     * The "relaxing" activity.
     */
    public static final class Relaxing extends Category {
        public Relaxing() {
        }

        public Relaxing(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    /**
     * The "talking" activity.
     */
    public static final class Talking extends Category {
        public Talking() {
        }

        public Talking(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    /**
     * The "traveling" activity.
     */
    public static final class Traveling extends Category {
        public Traveling() {
        }

        public Traveling(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    /**
     * The "doing_chores" activity.
     */
    public static final class Undefined extends Category {
        public Undefined() {
        }

        public Undefined(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    /**
     * The "working" activity.
     */
    public static final class Working extends Category {
        public Working() {
        }

        public Working(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }
}
