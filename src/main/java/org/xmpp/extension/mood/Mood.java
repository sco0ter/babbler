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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "mood")
@XmlSeeAlso({Mood.Afraid.class, Mood.Happy.class})
public final class Mood {

    public static final Value HAPPY = new Happy();

    @XmlElement
    private String text;

    @XmlElements({@XmlElement(name = "afraid", type = Afraid.class),
            @XmlElement(name = "amazed", type = Amazed.class),
            @XmlElement(name = "angry", type = Amazed.class),
            @XmlElement(name = "amorous", type = Amazed.class),
            @XmlElement(name = "annoyed", type = Amazed.class),
            @XmlElement(name = "anxious", type = Amazed.class),
            @XmlElement(name = "aroused", type = Amazed.class),
            @XmlElement(name = "ashamed", type = Amazed.class),
            @XmlElement(name = "bored", type = Amazed.class),
            @XmlElement(name = "brave", type = Amazed.class),
            @XmlElement(name = "calm", type = Amazed.class),
            @XmlElement(name = "cautious", type = Amazed.class),
            @XmlElement(name = "cold", type = Amazed.class),
            @XmlElement(name = "confident", type = Amazed.class),
            @XmlElement(name = "confused", type = Amazed.class),
            @XmlElement(name = "contemplative", type = Amazed.class),
            @XmlElement(name = "contented", type = Amazed.class),
            @XmlElement(name = "cranky", type = Amazed.class),
            @XmlElement(name = "creative", type = Amazed.class),
            @XmlElement(name = "curious", type = Amazed.class),
            @XmlElement(name = "dejected", type = Amazed.class),
            @XmlElement(name = "depressed", type = Amazed.class),
            @XmlElement(name = "disappointed", type = Amazed.class),
            @XmlElement(name = "disgusted", type = Amazed.class),
            @XmlElement(name = "dismayed", type = Amazed.class),
            @XmlElement(name = "distracted", type = Amazed.class),
            @XmlElement(name = "embarrassed", type = Amazed.class),
            @XmlElement(name = "envious", type = Amazed.class),
            @XmlElement(name = "excited", type = Amazed.class),
            @XmlElement(name = "flirtatious", type = Amazed.class),
            @XmlElement(name = "frustrated", type = Amazed.class),
            @XmlElement(name = "grumpy", type = Amazed.class),
            @XmlElement(name = "guilty", type = Amazed.class),
            @XmlElement(name = "happy", type = Happy.class),
            @XmlElement(name = "hopeful", type = Amazed.class),
            @XmlElement(name = "hot", type = Amazed.class),
            @XmlElement(name = "humbled", type = Amazed.class),
            @XmlElement(name = "humiliated", type = Amazed.class),
            @XmlElement(name = "hungry", type = Amazed.class),
            @XmlElement(name = "hurt", type = Amazed.class),
            @XmlElement(name = "impressed", type = Amazed.class),
            @XmlElement(name = "in_awe", type = Amazed.class),
            @XmlElement(name = "in_love", type = Amazed.class),
            @XmlElement(name = "indignant", type = Amazed.class),
            @XmlElement(name = "interested", type = Amazed.class),
            @XmlElement(name = "intoxicated", type = Amazed.class),
            @XmlElement(name = "invincible", type = Amazed.class),
            @XmlElement(name = "jealous", type = Amazed.class),
            @XmlElement(name = "lonely", type = Amazed.class),
            @XmlElement(name = "lucky", type = Amazed.class),
            @XmlElement(name = "mean", type = Amazed.class),
            @XmlElement(name = "moody", type = Amazed.class),
            @XmlElement(name = "nervous", type = Amazed.class),
            @XmlElement(name = "neutral", type = Amazed.class),
            @XmlElement(name = "offended", type = Amazed.class),
            @XmlElement(name = "outraged", type = Amazed.class),
            @XmlElement(name = "playful", type = Amazed.class),
            @XmlElement(name = "proud", type = Amazed.class),
            @XmlElement(name = "relaxed", type = Amazed.class),
            @XmlElement(name = "relieved", type = Amazed.class),
            @XmlElement(name = "remorseful", type = Amazed.class),
            @XmlElement(name = "restless", type = Amazed.class),
            @XmlElement(name = "sad", type = Amazed.class),
            @XmlElement(name = "sarcastic", type = Amazed.class),
            @XmlElement(name = "serious", type = Amazed.class),
            @XmlElement(name = "shocked", type = Amazed.class),
            @XmlElement(name = "shy", type = Amazed.class),
            @XmlElement(name = "sick", type = Amazed.class),
            @XmlElement(name = "sleepy", type = Amazed.class),
            @XmlElement(name = "spontaneous", type = Amazed.class),
            @XmlElement(name = "stressed", type = Amazed.class),
            @XmlElement(name = "strong", type = Amazed.class),
            @XmlElement(name = "surprised", type = Amazed.class),
            @XmlElement(name = "thankful", type = Amazed.class),
            @XmlElement(name = "thirsty", type = Amazed.class),
            @XmlElement(name = "tired", type = Amazed.class),
            @XmlElement(name = "undefined", type = Amazed.class),
            @XmlElement(name = "weak", type = Amazed.class),
            @XmlElement(name = "worried", type = Happy.class)})
    private Value value;

    public String getText() {
        return text;
    }

    public Value getValue() {
        return value;
    }

    public static abstract class Value {
    }

    public static final class Afraid extends Value {

    }

    public static final class Amazed extends Value {

    }

    public static final class Happy extends Value {

    }
}
