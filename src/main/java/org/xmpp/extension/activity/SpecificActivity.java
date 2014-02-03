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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @author Christian Schudt
 */
@XmlSeeAlso({SpecificActivity.AtTheSpa.class, SpecificActivity.Sleeping.class})
public abstract class SpecificActivity {

    @XmlElementRef
    private SpecificActivity specificActivity;

    public SpecificActivity() {
    }

    public SpecificActivity(SpecificActivity specificActivity) {
        this.specificActivity = specificActivity;
    }

    public SpecificActivity getSpecificActivity() {
        return specificActivity;
    }

    @XmlRootElement(name = "at_the_spa")
    public static final class AtTheSpa extends SpecificActivity {
        public AtTheSpa() {
        }

        public AtTheSpa(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "brushing_teeth")
    public static final class BrushingTeeth extends SpecificActivity {
        public BrushingTeeth() {
        }

        public BrushingTeeth(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "buying_groceries")
    public static final class BuyingGroceries extends SpecificActivity {
        public BuyingGroceries() {
        }

        public BuyingGroceries(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "cleaning")
    public static final class Cleaning extends SpecificActivity {
        public Cleaning() {
        }

        public Cleaning(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "coding")
    public static final class Coding extends SpecificActivity {
        public Coding() {
        }

        public Coding(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "commuting")
    public static final class Commuting extends SpecificActivity {
        public Commuting() {
        }

        public Commuting(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "cooking")
    public static final class Cooking extends SpecificActivity {
        public Cooking() {
        }

        public Cooking(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "cycling")
    public static final class Cycling extends SpecificActivity {
        public Cycling() {
        }

        public Cycling(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "dancing")
    public static final class Dancing extends SpecificActivity {
        public Dancing() {
        }

        public Dancing(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "day_off")
    public static final class DayOff extends SpecificActivity {
        public DayOff() {
        }

        public DayOff(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "doing_maintenance")
    public static final class DoingMaintenance extends SpecificActivity {
        public DoingMaintenance() {
        }

        public DoingMaintenance(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "doing_the_dishes")
    public static final class DoingTheDishes extends SpecificActivity {
        public DoingTheDishes() {
        }

        public DoingTheDishes(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "doing_the_laundry")
    public static final class DoingTheLaundry extends SpecificActivity {
        public DoingTheLaundry() {
        }

        public DoingTheLaundry(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "driving")
    public static final class Driving extends SpecificActivity {
        public Driving() {
        }

        public Driving(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "fishing")
    public static final class Fishing extends SpecificActivity {
        public Fishing() {
        }

        public Fishing(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "gaming")
    public static final class Gaming extends SpecificActivity {
        public Gaming() {
        }

        public Gaming(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "gardening")
    public static final class Gardening extends SpecificActivity {
        public Gardening() {
        }

        public Gardening(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "getting_a_haircut")
    public static final class GettingAHaircut extends SpecificActivity {
        public GettingAHaircut() {
        }

        public GettingAHaircut(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "going_out")
    public static final class GoingOut extends SpecificActivity {
        public GoingOut() {
        }

        public GoingOut(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "hanging_out")
    public static final class HangingOut extends SpecificActivity {
        public HangingOut() {
        }

        public HangingOut(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "having_a_beer")
    public static final class HavingABeer extends SpecificActivity {
        public HavingABeer() {
        }

        public HavingABeer(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "having_a_snack")
    public static final class HavingASnack extends SpecificActivity {
        public HavingASnack() {
        }

        public HavingASnack(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "having_breakfast")
    public static final class HavingBreakfast extends SpecificActivity {
        public HavingBreakfast() {
        }

        public HavingBreakfast(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "having_coffee")
    public static final class HavingCoffee extends SpecificActivity {
        public HavingCoffee() {
        }

        public HavingCoffee(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "having_dinner")
    public static final class HavingDinner extends SpecificActivity {
        public HavingDinner() {
        }

        public HavingDinner(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "having_lunch")
    public static final class HavingLunch extends SpecificActivity {
        public HavingLunch() {
        }

        public HavingLunch(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "having_tea")
    public static final class HavingTea extends SpecificActivity {
        public HavingTea() {
        }

        public HavingTea(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "hiding")
    public static final class Hiding extends SpecificActivity {
        public Hiding() {
        }

        public Hiding(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "hiking")
    public static final class Hiking extends SpecificActivity {
        public Hiking() {
        }

        public Hiking(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "in_a_car")
    public static final class InACar extends SpecificActivity {
        public InACar() {
        }

        public InACar(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "in_a_meeting")
    public static final class InAMeeting extends SpecificActivity {
        public InAMeeting() {
        }

        public InAMeeting(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "in_real_life")
    public static final class InRealLife extends SpecificActivity {
        public InRealLife() {
        }

        public InRealLife(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "jogging")
    public static final class Jogging extends SpecificActivity {
        public Jogging() {
        }

        public Jogging(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "on_a_bus")
    public static final class OnABus extends SpecificActivity {
        public OnABus() {
        }

        public OnABus(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "on_a_plane")
    public static final class OnAPlane extends SpecificActivity {
        public OnAPlane() {
        }

        public OnAPlane(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "on_a_train")
    public static final class OnATrain extends SpecificActivity {
        public OnATrain() {
        }

        public OnATrain(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "on_a_trip")
    public static final class OnATrip extends SpecificActivity {
        public OnATrip() {
        }

        public OnATrip(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "on_the_phone")
    public static final class OnThePhone extends SpecificActivity {
        public OnThePhone() {
        }

        public OnThePhone(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "on_vacation")
    public static final class OnVacation extends SpecificActivity {
        public OnVacation() {
        }

        public OnVacation(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "on_video_phone")
    public static final class OnVideoPhone extends SpecificActivity {
        public OnVideoPhone() {
        }

        public OnVideoPhone(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "other")
    public static final class Other extends SpecificActivity {
        public Other() {
        }

        public Other(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "partying")
    public static final class Partying extends SpecificActivity {
        public Partying() {
        }

        public Partying(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "playing_sports")
    public static final class PlayingSports extends SpecificActivity {
        public PlayingSports() {
        }

        public PlayingSports(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "praying")
    public static final class Praying extends SpecificActivity {
        public Praying() {
        }

        public Praying(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "reading")
    public static final class Reading extends SpecificActivity {
        public Reading() {
        }

        public Reading(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "rehearsing")
    public static final class Rehearsing extends SpecificActivity {
        public Rehearsing() {
        }

        public Rehearsing(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "running")
    public static final class Running extends SpecificActivity {
        public Running() {
        }

        public Running(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "running_an_errand")
    public static final class RunningAnErrand extends SpecificActivity {
        public RunningAnErrand() {
        }

        public RunningAnErrand(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "scheduled_holiday")
    public static final class ScheduledHoliday extends SpecificActivity {
        public ScheduledHoliday() {
        }

        public ScheduledHoliday(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "shaving")
    public static final class Shaving extends SpecificActivity {
        public Shaving() {
        }

        public Shaving(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "shopping")
    public static final class Shopping extends SpecificActivity {
        public Shopping() {
        }

        public Shopping(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "skiing")
    public static final class Skiing extends SpecificActivity {
        public Skiing() {
        }

        public Skiing(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "sleeping")
    public static final class Sleeping extends SpecificActivity {
        public Sleeping() {
        }

        public Sleeping(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "smoking")
    public static final class Smoking extends SpecificActivity {
        public Smoking() {
        }

        public Smoking(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "socializing")
    public static final class Socializing extends SpecificActivity {
        public Socializing() {
        }

        public Socializing(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "studying")
    public static final class Studying extends SpecificActivity {
        public Studying() {
        }

        public Studying(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "sunbathing")
    public static final class Sunbathing extends SpecificActivity {
        public Sunbathing() {
        }

        public Sunbathing(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "swimming")
    public static final class Swimming extends SpecificActivity {
        public Swimming() {
        }

        public Swimming(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "taking_a_bath")
    public static final class TakingABath extends SpecificActivity {
        public TakingABath() {
        }

        public TakingABath(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "taking_a_shower")
    public static final class TakingAShower extends SpecificActivity {
        public TakingAShower() {
        }

        public TakingAShower(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "thinking")
    public static final class Thinking extends SpecificActivity {
        public Thinking() {
        }

        public Thinking(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "walking")
    public static final class Walking extends SpecificActivity {
        public Walking() {
        }

        public Walking(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "walking_the_dog")
    public static final class WalkingTheDog extends SpecificActivity {
        public WalkingTheDog() {
        }

        public WalkingTheDog(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "watching_a_movie")
    public static final class WatchingAMovie extends SpecificActivity {
        public WatchingAMovie() {
        }

        public WatchingAMovie(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "watching_tv")
    public static final class WatchingTv extends SpecificActivity {
        public WatchingTv() {
        }

        public WatchingTv(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "working_out")
    public static final class WorkingOut extends SpecificActivity {
        public WorkingOut() {
        }

        public WorkingOut(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

    @XmlRootElement(name = "writing")
    public static final class Writing extends SpecificActivity {
        public Writing() {
        }

        public Writing(SpecificActivity specificActivity) {
            super(specificActivity);
        }
    }

}