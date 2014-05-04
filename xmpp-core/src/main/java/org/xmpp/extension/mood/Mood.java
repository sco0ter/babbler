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

package org.xmpp.extension.mood;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The implementation of <a href="http://xmpp.org/extensions/xep-0107.html">XEP-0107: User Mood</a>.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0107.html">XEP-0107: User Mood</a></cite></p>
 * <p>This specification defines a payload format for communicating information about user moods, such as whether a person is currently happy, sad, angy, or annoyed. The payload format is typically transported using the personal eventing protocol, a profile of XMPP publish-subscribe specified in XEP-0163.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "mood")
@XmlSeeAlso({
        Mood.Afraid.class,
        Mood.Amazed.class,
        Mood.Amorous.class,
        Mood.Angry.class,
        Mood.Annoyed.class,
        Mood.Anxious.class,
        Mood.Aroused.class,
        Mood.Ashamed.class,
        Mood.Bored.class,
        Mood.Brave.class,
        Mood.Calm.class,
        Mood.Cautious.class,
        Mood.Cold.class,
        Mood.Confident.class,
        Mood.Confused.class,
        Mood.Contemplative.class,
        Mood.Contented.class,
        Mood.Cranky.class,
        Mood.Crazy.class,
        Mood.Creative.class,
        Mood.Curious.class,
        Mood.Dejected.class,
        Mood.Depressed.class,
        Mood.Disappointed.class,
        Mood.Disgusted.class,
        Mood.Dismayed.class,
        Mood.Distracted.class,
        Mood.Embarrassed.class,
        Mood.Envious.class,
        Mood.Excited.class,
        Mood.Flirtatious.class,
        Mood.Frustrated.class,
        Mood.Grateful.class,
        Mood.Grieving.class,
        Mood.Grumpy.class,
        Mood.Guilty.class,
        Mood.Happy.class,
        Mood.Hopeful.class,
        Mood.Hot.class,
        Mood.Humbled.class,
        Mood.Humiliated.class,
        Mood.Hungry.class,
        Mood.Hurt.class,
        Mood.Impressed.class,
        Mood.InAwe.class,
        Mood.InLove.class,
        Mood.Indignant.class,
        Mood.Interested.class,
        Mood.Intoxicated.class,
        Mood.Invincible.class,
        Mood.Jealous.class,
        Mood.Lonely.class,
        Mood.Lost.class,
        Mood.Lucky.class,
        Mood.Mean.class,
        Mood.Moody.class,
        Mood.Nervous.class,
        Mood.Neutral.class,
        Mood.Offended.class,
        Mood.Outraged.class,
        Mood.Playful.class,
        Mood.Proud.class,
        Mood.Relaxed.class,
        Mood.Relieved.class,
        Mood.Remorseful.class,
        Mood.Restless.class,
        Mood.Sad.class,
        Mood.Sarcastic.class,
        Mood.Satisfied.class,
        Mood.Serious.class,
        Mood.Shocked.class,
        Mood.Shy.class,
        Mood.Sick.class,
        Mood.Sleepy.class,
        Mood.Spontaneous.class,
        Mood.Stressed.class,
        Mood.Strong.class,
        Mood.Surprised.class,
        Mood.Thankful.class,
        Mood.Thirsty.class,
        Mood.Tired.class,
        Mood.Undefined.class,
        Mood.Weak.class,
        Mood.Worried.class,
})
public final class Mood {
    @XmlElement
    private String text;

    @XmlJavaTypeAdapter(MoodAdapter.class)
    @XmlElements({@XmlElement(name = "afraid", type = Afraid.class),
            @XmlElement(name = "amazed", type = Amazed.class),
            @XmlElement(name = "angry", type = Angry.class),
            @XmlElement(name = "amorous", type = Amorous.class),
            @XmlElement(name = "annoyed", type = Annoyed.class),
            @XmlElement(name = "anxious", type = Anxious.class),
            @XmlElement(name = "aroused", type = Aroused.class),
            @XmlElement(name = "ashamed", type = Ashamed.class),
            @XmlElement(name = "bored", type = Bored.class),
            @XmlElement(name = "brave", type = Brave.class),
            @XmlElement(name = "calm", type = Calm.class),
            @XmlElement(name = "cautious", type = Cautious.class),
            @XmlElement(name = "cold", type = Cold.class),
            @XmlElement(name = "confident", type = Confident.class),
            @XmlElement(name = "confused", type = Confused.class),
            @XmlElement(name = "contemplative", type = Contemplative.class),
            @XmlElement(name = "contented", type = Contented.class),
            @XmlElement(name = "cranky", type = Cranky.class),
            @XmlElement(name = "crazy", type = Crazy.class),
            @XmlElement(name = "creative", type = Creative.class),
            @XmlElement(name = "curious", type = Curious.class),
            @XmlElement(name = "dejected", type = Dejected.class),
            @XmlElement(name = "depressed", type = Depressed.class),
            @XmlElement(name = "disappointed", type = Disappointed.class),
            @XmlElement(name = "disgusted", type = Disgusted.class),
            @XmlElement(name = "dismayed", type = Dismayed.class),
            @XmlElement(name = "distracted", type = Distracted.class),
            @XmlElement(name = "embarrassed", type = Embarrassed.class),
            @XmlElement(name = "envious", type = Envious.class),
            @XmlElement(name = "excited", type = Excited.class),
            @XmlElement(name = "flirtatious", type = Flirtatious.class),
            @XmlElement(name = "frustrated", type = Frustrated.class),
            @XmlElement(name = "grumpy", type = Grumpy.class),
            @XmlElement(name = "guilty", type = Guilty.class),
            @XmlElement(name = "happy", type = Happy.class),
            @XmlElement(name = "hopeful", type = Hopeful.class),
            @XmlElement(name = "hot", type = Hot.class),
            @XmlElement(name = "humbled", type = Humbled.class),
            @XmlElement(name = "humiliated", type = Humiliated.class),
            @XmlElement(name = "hungry", type = Hungry.class),
            @XmlElement(name = "hurt", type = Hurt.class),
            @XmlElement(name = "impressed", type = Impressed.class),
            @XmlElement(name = "in_awe", type = InAwe.class),
            @XmlElement(name = "in_love", type = InLove.class),
            @XmlElement(name = "indignant", type = Indignant.class),
            @XmlElement(name = "interested", type = Interested.class),
            @XmlElement(name = "intoxicated", type = Intoxicated.class),
            @XmlElement(name = "invincible", type = Invincible.class),
            @XmlElement(name = "jealous", type = Jealous.class),
            @XmlElement(name = "lonely", type = Lonely.class),
            @XmlElement(name = "lucky", type = Lucky.class),
            @XmlElement(name = "mean", type = Mean.class),
            @XmlElement(name = "moody", type = Moody.class),
            @XmlElement(name = "nervous", type = Nervous.class),
            @XmlElement(name = "neutral", type = Neutral.class),
            @XmlElement(name = "offended", type = Offended.class),
            @XmlElement(name = "outraged", type = Outraged.class),
            @XmlElement(name = "playful", type = Playful.class),
            @XmlElement(name = "proud", type = Proud.class),
            @XmlElement(name = "relaxed", type = Relaxed.class),
            @XmlElement(name = "relieved", type = Relieved.class),
            @XmlElement(name = "remorseful", type = Remorseful.class),
            @XmlElement(name = "restless", type = Restless.class),
            @XmlElement(name = "sad", type = Sad.class),
            @XmlElement(name = "sarcastic", type = Sarcastic.class),
            @XmlElement(name = "serious", type = Serious.class),
            @XmlElement(name = "shocked", type = Shocked.class),
            @XmlElement(name = "shy", type = Shy.class),
            @XmlElement(name = "sick", type = Sick.class),
            @XmlElement(name = "sleepy", type = Sleepy.class),
            @XmlElement(name = "spontaneous", type = Spontaneous.class),
            @XmlElement(name = "stressed", type = Stressed.class),
            @XmlElement(name = "strong", type = Strong.class),
            @XmlElement(name = "surprised", type = Surprised.class),
            @XmlElement(name = "thankful", type = Thankful.class),
            @XmlElement(name = "thirsty", type = Thirsty.class),
            @XmlElement(name = "tired", type = Tired.class),
            @XmlElement(name = "undefined", type = Undefined.class),
            @XmlElement(name = "weak", type = Weak.class),
            @XmlElement(name = "worried", type = Worried.class)})
    private Value value;

    private Mood() {
    }

    /**
     * Creates a mood with a specific value.
     *
     * @param value The mood value.
     */
    public Mood(Value value) {
        this.value = value;
    }

    /**
     * Creates a mood with a specific value and a text.
     *
     * @param value The mood value.
     * @param text  A natural-language description of, or reason for, the mood.
     */
    public Mood(Value value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * Gets a natural-language description of, or reason for, the mood.
     *
     * @return The description.
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the mood value.
     *
     * @return The mood value.
     */
    public Value getValue() {
        return value;
    }

    /**
     * Defines a mood value.
     */
    public enum Value {
        /**
         * Impressed with fear or apprehension; in fear; apprehensive.
         */
        AFRAID(Afraid.class),
        /**
         * Astonished; confounded with fear, surprise or wonder.
         */
        AMAZED(Amazed.class),

        /**
         * Inclined to love; having a propensity to love, or to sexual enjoyment; loving, fond, affectionate, passionate, lustful, sexual, etc.
         */
        AMOROUS(Amorous.class),
        /**
         * Displaying or feeling anger, i.e., a strong feeling of displeasure, hostility or antagonism towards someone or something, usually combined with an urge to harm.
         */
        ANGRY(Angry.class),
        /**
         * To be disturbed or irritated, especially by continued or repeated acts.
         */
        ANNOYED(Annoyed.class),
        /**
         * Full of anxiety or disquietude; greatly concerned or solicitous, esp. respecting something future or unknown; being in painful suspense.
         */
        ANXIOUS(Anxious.class),
        /**
         * To be stimulated in one's feelings, especially to be sexually stimulated.
         */
        AROUSED(Aroused.class),
        /**
         * Feeling shame or guilt.
         */
        ASHAMED(Ashamed.class),
        /**
         * Suffering from boredom; uninterested, without attention.
         */
        BORED(Bored.class),
        /**
         * Strong in the face of fear; courageous.
         */
        BRAVE(Brave.class),
        /**
         * Peaceful, quiet.
         */
        CALM(Calm.class),
        /**
         * Taking care or caution; tentative.
         */
        CAUTIOUS(Cautious.class),
        /**
         * Feeling the sensation of coldness, especially to the point of discomfort.
         */
        COLD(Cold.class),
        /**
         * Feeling very sure of or positive about something, especially about one's own capabilities.
         */
        CONFIDENT(Confident.class),
        /**
         * Chaotic, jumbled or muddled.
         */
        CONFUSED(Confused.class),
        /**
         * Feeling introspective or thoughtful.
         */
        CONTEMPLATIVE(Contemplative.class),
        /**
         * Pleased at the satisfaction of a want or desire; satisfied.
         */
        CONTENTED(Contented.class),
        /**
         * Grouchy, irritable; easily upset.
         */
        CRANKY(Cranky.class),
        /**
         * Feeling out of control; feeling overly excited or enthusiastic.
         */
        CRAZY(Crazy.class),
        /**
         * Feeling original, expressive, or imaginative.
         */
        CREATIVE(Creative.class),
        /**
         * Inquisitive; tending to ask questions, investigate, or explore.
         */
        CURIOUS(Curious.class),
        /**
         * Feeling sad and dispirited.
         */
        DEJECTED(Dejected.class),
        /**
         * Severely despondent and unhappy.
         */
        DEPRESSED(Depressed.class),
        /**
         * Defeated of expectation or hope; let down.
         */
        DISAPPOINTED(Disappointed.class),
        /**
         * Filled with disgust; irritated and out of patience.
         */
        DISGUSTED(Disgusted.class),
        /**
         * Feeling a sudden or complete loss of courage in the face of trouble or danger.
         */
        DISMAYED(Dismayed.class),
        /**
         * Having one's attention diverted; preoccupied.
         */
        DISTRACTED(Distracted.class),
        /**
         * Having a feeling of shameful discomfort.
         */
        EMBARRASSED(Embarrassed.class),
        /**
         * Feeling pain by the excellence or good fortune of another.
         */
        ENVIOUS(Envious.class),
        /**
         * Having great enthusiasm.
         */
        EXCITED(Excited.class),
        /**
         * In the mood for flirting.
         */
        FLIRTATIOUS(Flirtatious.class),
        /**
         * Suffering from frustration; dissatisfied, agitated, or discontented because one is unable to perform an action or fulfill a desire.
         */
        FRUSTRATED(Frustrated.class),
        /**
         * Feeling appreciation or thanks.
         */
        GRATEFUL(Grateful.class),
        /**
         * Feeling very sad about something, especially something lost; mournful; sorrowful.
         */
        GRIEVING(Grieving.class),
        /**
         * Unhappy and irritable.
         */
        GRUMPY(Grumpy.class),
        /**
         * Feeling responsible for wrongdoing; feeling blameworthy.
         */
        GUILTY(Guilty.class),
        /**
         * Experiencing the effect of favourable fortune; having the feeling arising from the consciousness of well-being or of enjoyment; enjoying good of any kind, as peace, tranquillity, comfort; contented; joyous.
         */
        HAPPY(Happy.class),
        /**
         * Having a positive feeling, belief, or expectation that something wished for can or will happen.
         */
        HOPEFUL(Hopeful.class),
        /**
         * Feeling the sensation of heat, especially to the point of discomfort.
         */
        HOT(Hot.class),
        /**
         * Having or showing a modest or low estimate of one's own importance; feeling lowered in dignity or importance.
         */
        HUMBLED(Humbled.class),
        /**
         * Feeling deprived of dignity or self-respect.
         */
        HUMILIATED(Humiliated.class),
        /**
         * Having a physical need for food.
         */
        HUNGRY(Hungry.class),
        /**
         * Wounded, injured, or pained, whether physically or emotionally.
         */
        HURT(Hurt.class),
        /**
         * Favourably affected by something or someone.
         */
        IMPRESSED(Impressed.class),
        /**
         * Feeling amazement at something or someone; or feeling a combination of fear and reverence.
         */
        IN_AWE(InAwe.class),
        /**
         * Feeling strong affection, care, liking, or attraction..
         */
        IN_LOVE(InLove.class),
        /**
         * Showing anger or indignation, especially at something unjust or wrong.
         */
        INDIGNANT(Indignant.class),
        /**
         * Showing great attention to something or someone; having or showing interest.
         */
        INTERESTED(Interested.class),
        /**
         * Under the influence of alcohol; drunk.
         */
        INTOXICATED(Intoxicated.class),
        /**
         * Feeling as if one cannot be defeated, overcome or denied.
         */
        INVINCIBLE(Invincible.class),
        /**
         * Fearful of being replaced in position or affection.
         */
        JEALOUS(Jealous.class),
        /**
         * Feeling isolated, empty, or abandoned.
         */
        LONELY(Lonely.class),
        /**
         * Unable to find one's way, either physically or emotionally.
         */
        LOST(Lost.class),
        /**
         * Feeling as if one will be favored by luck.
         */
        LUCKY(Lucky.class),
        /**
         * Causing or intending to cause intentional harm; bearing ill will towards another; cruel; malicious.
         */
        MEAN(Mean.class),
        /**
         * Given to sudden or frequent changes of mind or feeling; temperamental.
         */
        MOODY(Moody.class),
        /**
         * Easily agitated or alarmed; apprehensive or anxious.
         */
        NERVOUS(Nervous.class),
        /**
         * Not having a strong mood or emotional state.
         */
        NEUTRAL(Neutral.class),
        /**
         * Feeling emotionally hurt, displeased, or insulted.
         */
        OFFENDED(Offended.class),
        /**
         * Feeling resentful anger caused by an extremely violent or vicious attack, or by an offensive, immoral, or indecent act.
         */
        OUTRAGED(Outraged.class),
        /**
         * Interested in play; fun, recreational, unserious, lighthearted; joking, silly.
         */
        PLAYFUL(Playful.class),
        /**
         * Feeling a sense of one's own worth or accomplishment.
         */
        PROUD(Proud.class),
        /**
         * Having an easy-going mood; not stressed; calm.
         */
        RELAXED(Relaxed.class),
        /**
         * Feeling uplifted because of the removal of stress or discomfort.
         */
        RELIEVED(Relieved.class),
        /**
         * Feeling regret or sadness for doing something wrong.
         */
        REMORSEFUL(Remorseful.class),
        /**
         * Without rest; unable to be still or quiet; uneasy; continually moving.
         */
        RESTLESS(Restless.class),
        /**
         * Feeling sorrow; sorrowful, mournful.
         */
        SAD(Sad.class),
        /**
         * Mocking and ironical.
         */
        SARCASTIC(Sarcastic.class),
        /**
         * Pleased at the fulfillment of a need or desire.
         */
        SATISFIED(Satisfied.class),
        /**
         * Without humor or expression of happiness; grave in manner or disposition; earnest; thoughtful; solemn.
         */
        SERIOUS(Serious.class),
        /**
         * Surprised, startled, confused, or taken aback.
         */
        SHOCKED(Shocked.class),
        /**
         * Feeling easily frightened or scared; timid; reserved or coy.
         */
        SHY(Shy.class),
        /**
         * Feeling in poor health; ill.
         */
        SICK(Sick.class),
        /**
         * Feeling the need for sleep.
         */
        SLEEPY(Sleepy.class),
        /**
         * Acting without planning; natural; impulsive.
         */
        SPONTANEOUS(Spontaneous.class),
        /**
         * Suffering emotional pressure.
         */
        STRESSED(Stressed.class),
        /**
         * Capable of producing great physical force; or, emotionally forceful, able, determined, unyielding.
         */
        STRONG(Strong.class),
        /**
         * Experiencing a feeling caused by something unexpected.
         */
        SURPRISED(Surprised.class),
        /**
         * Showing appreciation or gratitude.
         */
        THANKFUL(Thankful.class),
        /**
         * Feeling the need to drink.
         */
        THIRSTY(Thirsty.class),
        /**
         * In need of rest or sleep.
         */
        TIRED(Tired.class),
        /**
         * Feeling any emotion not defined here
         */
        UNDEFINED(Undefined.class),
        /**
         * Lacking in force or ability, either physical or emotional.
         */
        WEAK(Weak.class),
        /**
         * Thinking about unpleasant things that have happened or that might happen; feeling afraid and unhappy.
         */
        WORRIED(Worried.class);

        private Class<? extends AbstractValue> valueClass;

        private Value(Class<? extends AbstractValue> valueClass) {
            this.valueClass = valueClass;
        }

    }

    private static abstract class AbstractValue {

        @XmlTransient
        private Value value;

        private AbstractValue(Value value) {
            this.value = value;
        }
    }

    /**
     * Impressed with fear or apprehension; in fear; apprehensive.
     */
    static final class Afraid extends AbstractValue {
        Afraid() {
            super(Value.AFRAID);
        }
    }

    /**
     * Astonished; confounded with fear, surprise or wonder.
     */
    static final class Amazed extends AbstractValue {
        Amazed() {
            super(Value.AMAZED);
        }
    }

    /**
     * Inclined to love; having a propensity to love, or to sexual enjoyment; loving, fond, affectionate, passionate, lustful, sexual, etc.
     */
    static final class Amorous extends AbstractValue {
        Amorous() {
            super(Value.AMOROUS);
        }
    }

    /**
     * Displaying or feeling anger, i.e., a strong feeling of displeasure, hostility or antagonism towards someone or something, usually combined with an urge to harm.
     */
    static final class Angry extends AbstractValue {
        Angry() {
            super(Value.ANGRY);
        }
    }

    /**
     * To be disturbed or irritated, especially by continued or repeated acts.
     */
    static final class Annoyed extends AbstractValue {
        Annoyed() {
            super(Value.ANNOYED);
        }
    }

    /**
     * Full of anxiety or disquietude; greatly concerned or solicitous, esp. respecting something future or unknown; being in painful suspense.
     */
    static final class Anxious extends AbstractValue {
        Anxious() {
            super(Value.ANXIOUS);
        }
    }

    /**
     * To be stimulated in one's feelings, especially to be sexually stimulated.
     */
    static final class Aroused extends AbstractValue {
        Aroused() {
            super(Value.AROUSED);
        }
    }

    /**
     * To be stimulated in one's feelings, especially to be sexually stimulated.
     */
    static final class Ashamed extends AbstractValue {
        Ashamed() {
            super(Value.ASHAMED);
        }
    }

    /**
     * Suffering from boredom; uninterested, without attention.
     */
    static final class Bored extends AbstractValue {
        Bored() {
            super(Value.BORED);
        }
    }

    /**
     * Strong in the face of fear; courageous.
     */
    static final class Brave extends AbstractValue {
        Brave() {
            super(Value.BRAVE);
        }
    }

    /**
     * Peaceful, quiet.
     */
    static final class Calm extends AbstractValue {
        Calm() {
            super(Value.CALM);
        }
    }

    /**
     * Taking care or caution; tentative.
     */
    static final class Cautious extends AbstractValue {
        Cautious() {
            super(Value.CAUTIOUS);
        }
    }

    /**
     * Feeling the sensation of coldness, especially to the point of discomfort.
     */
    static final class Cold extends AbstractValue {
        Cold() {
            super(Value.COLD);
        }
    }

    /**
     * Feeling very sure of or positive about something, especially about one's own capabilities.
     */
    static final class Confident extends AbstractValue {
        Confident() {
            super(Value.CONFIDENT);
        }
    }

    /**
     * Chaotic, jumbled or muddled.
     */
    static final class Confused extends AbstractValue {
        Confused() {
            super(Value.CONFUSED);
        }
    }

    /**
     * Feeling introspective or thoughtful.
     */
    static final class Contemplative extends AbstractValue {
        Contemplative() {
            super(Value.CONTEMPLATIVE);
        }
    }

    /**
     * Pleased at the satisfaction of a want or desire; satisfied.
     */
    static final class Contented extends AbstractValue {
        Contented() {
            super(Value.CONTENTED);
        }
    }

    /**
     * Grouchy, irritable; easily upset.
     */
    static final class Cranky extends AbstractValue {
        Cranky() {
            super(Value.CRANKY);
        }
    }

    /**
     * Feeling out of control; feeling overly excited or enthusiastic.
     */
    static final class Crazy extends AbstractValue {
        Crazy() {
            super(Value.CRAZY);
        }
    }

    /**
     * Feeling original, expressive, or imaginative.
     */
    static final class Creative extends AbstractValue {
        Creative() {
            super(Value.CREATIVE);
        }
    }

    /**
     * Inquisitive; tending to ask questions, investigate, or explore.
     */
    static final class Curious extends AbstractValue {
        Curious() {
            super(Value.CURIOUS);
        }
    }

    /**
     * Feeling sad and dispirited.
     */
    static final class Dejected extends AbstractValue {
        Dejected() {
            super(Value.DEJECTED);
        }
    }

    /**
     * Severely despondent and unhappy.
     */
    static final class Depressed extends AbstractValue {
        Depressed() {
            super(Value.DEPRESSED);
        }
    }

    /**
     * Defeated of expectation or hope; let down.
     */
    static final class Disappointed extends AbstractValue {
        Disappointed() {
            super(Value.DISAPPOINTED);
        }
    }

    /**
     * Filled with disgust; irritated and out of patience.
     */
    static final class Disgusted extends AbstractValue {
        Disgusted() {
            super(Value.DISGUSTED);
        }
    }

    /**
     * Feeling a sudden or complete loss of courage in the face of trouble or danger.
     */
    static final class Dismayed extends AbstractValue {
        Dismayed() {
            super(Value.DISMAYED);
        }
    }

    /**
     * Having one's attention diverted; preoccupied.
     */
    static final class Distracted extends AbstractValue {
        Distracted() {
            super(Value.DISTRACTED);
        }
    }

    /**
     * Having a feeling of shameful discomfort.
     */
    static final class Embarrassed extends AbstractValue {
        Embarrassed() {
            super(Value.EMBARRASSED);
        }
    }

    /**
     * Feeling pain by the excellence or good fortune of another.
     */
    static final class Envious extends AbstractValue {
        Envious() {
            super(Value.ENVIOUS);
        }
    }

    /**
     * Having great enthusiasm.
     */
    static final class Excited extends AbstractValue {
        Excited() {
            super(Value.EXCITED);
        }
    }

    /**
     * In the mood for flirting.
     */
    static final class Flirtatious extends AbstractValue {
        Flirtatious() {
            super(Value.FLIRTATIOUS);
        }
    }

    /**
     * Suffering from frustration; dissatisfied, agitated, or discontented because one is unable to perform an action or fulfill a desire.
     */
    static final class Frustrated extends AbstractValue {
        Frustrated() {
            super(Value.FRUSTRATED);
        }
    }

    /**
     * Feeling appreciation or thanks.
     */
    static final class Grateful extends AbstractValue {
        Grateful() {
            super(Value.GRATEFUL);
        }
    }

    /**
     * Feeling very sad about something, especially something lost; mournful; sorrowful.
     */
    static final class Grieving extends AbstractValue {
        Grieving() {
            super(Value.GRIEVING);
        }
    }

    /**
     * Unhappy and irritable.
     */
    static final class Grumpy extends AbstractValue {
        Grumpy() {
            super(Value.GRUMPY);
        }
    }

    /**
     * Feeling responsible for wrongdoing; feeling blameworthy.
     */
    static final class Guilty extends AbstractValue {
        Guilty() {
            super(Value.GUILTY);
        }
    }

    /**
     * Experiencing the effect of favourable fortune; having the feeling arising from the consciousness of well-being or of enjoyment; enjoying good of any kind, as peace, tranquillity, comfort; contented; joyous.
     */
    static final class Happy extends AbstractValue {
        Happy() {
            super(Value.HAPPY);
        }
    }

    /**
     * Having a positive feeling, belief, or expectation that something wished for can or will happen.
     */
    static final class Hopeful extends AbstractValue {
        Hopeful() {
            super(Value.HOPEFUL);
        }
    }

    /**
     * Feeling the sensation of heat, especially to the point of discomfort.
     */
    static final class Hot extends AbstractValue {
        Hot() {
            super(Value.HOT);
        }
    }

    /**
     * Having or showing a modest or low estimate of one's own importance; feeling lowered in dignity or importance.
     */
    static final class Humbled extends AbstractValue {
        Humbled() {
            super(Value.HUMBLED);
        }
    }

    /**
     * Feeling deprived of dignity or self-respect.
     */
    static final class Humiliated extends AbstractValue {
        Humiliated() {
            super(Value.HUMILIATED);
        }
    }

    /**
     * Having a physical need for food.
     */
    static final class Hungry extends AbstractValue {
        Hungry() {
            super(Value.HUNGRY);
        }
    }

    /**
     * Wounded, injured, or pained, whether physically or emotionally.
     */
    static final class Hurt extends AbstractValue {
        Hurt() {
            super(Value.HURT);
        }
    }

    /**
     * Favourably affected by something or someone.
     */
    static final class Impressed extends AbstractValue {
        Impressed() {
            super(Value.IMPRESSED);
        }
    }

    /**
     * Feeling amazement at something or someone; or feeling a combination of fear and reverence.
     */
    static final class InAwe extends AbstractValue {
        InAwe() {
            super(Value.IN_AWE);
        }
    }

    /**
     * Feeling strong affection, care, liking, or attraction..
     */
    static final class InLove extends AbstractValue {
        InLove() {
            super(Value.IN_LOVE);
        }
    }

    /**
     * Showing anger or indignation, especially at something unjust or wrong.
     */
    static final class Indignant extends AbstractValue {
        Indignant() {
            super(Value.INDIGNANT);
        }
    }

    /**
     * Showing great attention to something or someone; having or showing interest.
     */
    static final class Interested extends AbstractValue {
        Interested() {
            super(Value.INTERESTED);
        }
    }

    /**
     * Under the influence of alcohol; drunk.
     */
    static final class Intoxicated extends AbstractValue {
        Intoxicated() {
            super(Value.INTOXICATED);
        }
    }

    /**
     * Feeling as if one cannot be defeated, overcome or denied.
     */
    static final class Invincible extends AbstractValue {
        Invincible() {
            super(Value.INVINCIBLE);
        }
    }

    /**
     * Fearful of being replaced in position or affection.
     */
    static final class Jealous extends AbstractValue {
        Jealous() {
            super(Value.JEALOUS);
        }
    }

    /**
     * Feeling isolated, empty, or abandoned.
     */
    static final class Lonely extends AbstractValue {
        Lonely() {
            super(Value.LONELY);
        }
    }

    /**
     * Unable to find one's way, either physically or emotionally.
     */
    static final class Lost extends AbstractValue {
        Lost() {
            super(Value.LOST);
        }
    }

    /**
     * Feeling as if one will be favored by luck.
     */
    static final class Lucky extends AbstractValue {
        Lucky() {
            super(Value.LUCKY);
        }
    }

    /**
     * Causing or intending to cause intentional harm; bearing ill will towards another; cruel; malicious.
     */
    static final class Mean extends AbstractValue {
        Mean() {
            super(Value.MEAN);
        }
    }

    /**
     * Given to sudden or frequent changes of mind or feeling; temperamental.
     */
    static final class Moody extends AbstractValue {
        Moody() {
            super(Value.MOODY);
        }
    }

    /**
     * Easily agitated or alarmed; apprehensive or anxious.
     */
    static final class Nervous extends AbstractValue {
        Nervous() {
            super(Value.NERVOUS);
        }
    }

    /**
     * Not having a strong mood or emotional state.
     */
    static final class Neutral extends AbstractValue {
        Neutral() {
            super(Value.NEUTRAL);
        }
    }

    /**
     * Feeling emotionally hurt, displeased, or insulted.
     */
    static final class Offended extends AbstractValue {
        Offended() {
            super(Value.OFFENDED);
        }
    }

    /**
     * Feeling resentful anger caused by an extremely violent or vicious attack, or by an offensive, immoral, or indecent act.
     */
    static final class Outraged extends AbstractValue {
        Outraged() {
            super(Value.OUTRAGED);
        }
    }

    /**
     * Interested in play; fun, recreational, unserious, lighthearted; joking, silly.
     */
    static final class Playful extends AbstractValue {
        Playful() {
            super(Value.PLAYFUL);
        }
    }

    /**
     * Feeling a sense of one's own worth or accomplishment.
     */
    static final class Proud extends AbstractValue {
        Proud() {
            super(Value.PROUD);
        }
    }

    /**
     * Having an easy-going mood; not stressed; calm.
     */
    static final class Relaxed extends AbstractValue {
        Relaxed() {
            super(Value.RELAXED);
        }
    }

    /**
     * Feeling uplifted because of the removal of stress or discomfort.
     */
    static final class Relieved extends AbstractValue {
        Relieved() {
            super(Value.RELIEVED);
        }
    }

    /**
     * Feeling regret or sadness for doing something wrong.
     */
    static final class Remorseful extends AbstractValue {
        Remorseful() {
            super(Value.REMORSEFUL);
        }
    }

    /**
     * Without rest; unable to be still or quiet; uneasy; continually moving.
     */
    static final class Restless extends AbstractValue {
        Restless() {
            super(Value.RESTLESS);
        }
    }

    /**
     * Feeling sorrow; sorrowful, mournful.
     */
    static final class Sad extends AbstractValue {
        Sad() {
            super(Value.SAD);
        }
    }

    /**
     * Mocking and ironical.
     */
    static final class Sarcastic extends AbstractValue {
        Sarcastic() {
            super(Value.SARCASTIC);
        }
    }

    /**
     * Pleased at the fulfillment of a need or desire.
     */
    static final class Satisfied extends AbstractValue {
        Satisfied() {
            super(Value.SATISFIED);
        }
    }

    /**
     * Without humor or expression of happiness; grave in manner or disposition; earnest; thoughtful; solemn.
     */
    static final class Serious extends AbstractValue {
        Serious() {
            super(Value.SERIOUS);
        }
    }

    /**
     * Surprised, startled, confused, or taken aback.
     */
    static final class Shocked extends AbstractValue {
        Shocked() {
            super(Value.SHOCKED);
        }
    }

    /**
     * Feeling easily frightened or scared; timid; reserved or coy.
     */
    static final class Shy extends AbstractValue {
        Shy() {
            super(Value.SHY);
        }
    }

    /**
     * Feeling in poor health; ill.
     */
    static final class Sick extends AbstractValue {
        Sick() {
            super(Value.SICK);
        }
    }

    /**
     * Feeling the need for sleep.
     */
    static final class Sleepy extends AbstractValue {
        Sleepy() {
            super(Value.SLEEPY);
        }
    }

    /**
     * Acting without planning; natural; impulsive.
     */
    static final class Spontaneous extends AbstractValue {
        Spontaneous() {
            super(Value.SPONTANEOUS);
        }
    }

    /**
     * Suffering emotional pressure.
     */
    static final class Stressed extends AbstractValue {
        Stressed() {
            super(Value.STRESSED);
        }
    }

    /**
     * Capable of producing great physical force; or, emotionally forceful, able, determined, unyielding.
     */
    static final class Strong extends AbstractValue {
        Strong() {
            super(Value.STRONG);
        }
    }

    /**
     * Experiencing a feeling caused by something unexpected.
     */
    static final class Surprised extends AbstractValue {
        Surprised() {
            super(Value.SURPRISED);
        }
    }

    /**
     * Showing appreciation or gratitude.
     */
    static final class Thankful extends AbstractValue {
        Thankful() {
            super(Value.THANKFUL);
        }
    }

    /**
     * Feeling the need to drink.
     */
    static final class Thirsty extends AbstractValue {
        Thirsty() {
            super(Value.THIRSTY);
        }
    }

    /**
     * In need of rest or sleep.
     */
    static final class Tired extends AbstractValue {
        Tired() {
            super(Value.TIRED);
        }
    }

    /**
     * Feeling any emotion.
     */
    static final class Undefined extends AbstractValue {
        Undefined() {
            super(Value.UNDEFINED);
        }
    }

    /**
     * Lacking in force or ability, either physical or emotional.
     */
    static final class Weak extends AbstractValue {
        Weak() {
            super(Value.WEAK);
        }
    }

    /**
     * Thinking about unpleasant things that have happened or that might happen; feeling afraid and unhappy.
     */
    static final class Worried extends AbstractValue {
        Worried() {
            super(Value.WORRIED);
        }
    }

    private static class MoodAdapter extends XmlAdapter<AbstractValue, Value> {

        @Override
        public Value unmarshal(AbstractValue v) throws Exception {
            if (v != null) {
                return v.value;
            }
            return null;
        }

        @Override
        public AbstractValue marshal(Value v) throws Exception {
            if (v != null) {
                return v.valueClass.newInstance();
            }
            return null;
        }
    }
}
