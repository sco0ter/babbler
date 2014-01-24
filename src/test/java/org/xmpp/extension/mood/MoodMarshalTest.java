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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class MoodMarshalTest extends BaseTest {

    @Test
    public void marshalHappy() throws IOException, JAXBException, XMLStreamException {
        String xml = marshall(new Mood(Mood.Value.HAPPY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><happy></happy></mood>");
    }

    @Test
    public void marshalAfraid() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.AFRAID));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><afraid></afraid></mood>");
    }

    @Test
    public void marshalAmazed() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.HAPPY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><happy></happy></mood>");
    }

    @Test
    public void marshalAngry() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.ANGRY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><angry></angry></mood>");
    }

    @Test
    public void marshalAmorous() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.AMOROUS));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><amorous></amorous></mood>");
    }

    @Test
    public void marshalAnnoyed() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.ANNOYED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><annoyed></annoyed></mood>");
    }

    @Test
    public void marshalAnxious() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.ANXIOUS));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><anxious></anxious></mood>");
    }

    @Test
    public void marshalAroused() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.AROUSED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><aroused></aroused></mood>");
    }

    @Test
    public void marshalAshamed() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.HAPPY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><happy></happy></mood>");
    }

    @Test
    public void marshalBored() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.BORED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><bored></bored></mood>");
    }

    @Test
    public void marshalBrave() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.BRAVE));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><brave></brave></mood>");
    }

    @Test
    public void marshalCalm() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.CALM));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><calm></calm></mood>");
    }

    @Test
    public void marshalCautious() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.CAUTIOUS));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><cautious></cautious></mood>");
    }

    @Test
    public void marshalCold() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.COLD));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><cold></cold></mood>");
    }

    @Test
    public void marshalConfident() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.CONFIDENT));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><confident></confident></mood>");
    }

    @Test
    public void marshalConfused() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.CONFUSED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><confused></confused></mood>");
    }

    @Test
    public void marshalContemplative() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.CONTEMPLATIVE));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><contemplative></contemplative></mood>");
    }

    @Test
    public void marshalContented() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.CONTENTED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><contented></contented></mood>");
    }

    @Test
    public void marshalCranky() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.CRANKY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><cranky></cranky></mood>");
    }

    @Test
    public void marshalCrazy() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.CRAZY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><crazy></crazy></mood>");
    }

    @Test
    public void marshalCreative() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.CREATIVE));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><creative></creative></mood>");
    }

    @Test
    public void marshalCurious() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.CURIOUS));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><curious></curious></mood>");
    }

    @Test
    public void marshalDejected() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.DEJECTED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><dejected></dejected></mood>");
    }

    @Test
    public void marshalDepressed() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.HAPPY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><happy></happy></mood>");
    }

    @Test
    public void marshalDisappointed() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.DISAPPOINTED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><disappointed></disappointed></mood>");
    }

    @Test
    public void marshalDisgusted() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.DISGUSTED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><disgusted></disgusted></mood>");
    }

    @Test
    public void marshalDismayed() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.DISMAYED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><dismayed></dismayed></mood>");
    }

    @Test
    public void marshalDistracted() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.DISTRACTED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><distracted></distracted></mood>");
    }

    @Test
    public void marshalEmbarrassed() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.EMBARRASSED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><embarrassed></embarrassed></mood>");
    }

    @Test
    public void marshalEnvious() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.ENVIOUS));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><envious></envious></mood>");
    }

    @Test
    public void marshalExcited() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.EXCITED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><excited></excited></mood>");
    }

    @Test
    public void marshalFlirtatious() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.FLIRTATIOUS));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><flirtatious></flirtatious></mood>");
    }

    @Test
    public void marshalFrustrated() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.FRUSTRATED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><frustrated></frustrated></mood>");
    }

    @Test
    public void marshalGrumpy() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.GRUMPY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><grumpy></grumpy></mood>");
    }

    @Test
    public void marshalGuilty() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.GUILTY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><guilty></guilty></mood>");
    }

    @Test
    public void marshalHopeful() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.HOPEFUL));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><hopeful></hopeful></mood>");
    }

    @Test
    public void marshalHot() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.HOT));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><hot></hot></mood>");
    }

    @Test
    public void marshalHumbled() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.HUMBLED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><humbled></humbled></mood>");
    }

    @Test
    public void marshalHumiliated() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.HUMILIATED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><humiliated></humiliated></mood>");
    }

    @Test
    public void marshalHungry() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.HUNGRY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><hungry></hungry></mood>");
    }

    @Test
    public void marshalHurt() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.HURT));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><hurt></hurt></mood>");
    }

    @Test
    public void marshalImpressed() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.IMPRESSED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><impressed></impressed></mood>");
    }

    @Test
    public void marshalInAwe() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.IN_AWE));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><in_awe></in_awe></mood>");
    }

    @Test
    public void marshalInLove() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.IN_LOVE));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><in_love></in_love></mood>");
    }

    @Test
    public void marshalIndignant() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.INDIGNANT));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><indignant></indignant></mood>");
    }

    @Test
    public void marshalInterested() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.INTERESTED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><interested></interested></mood>");
    }

    @Test
    public void marshalIntoxicated() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.INTOXICATED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><intoxicated></intoxicated></mood>");
    }

    @Test
    public void marshalInvincible() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.INVINCIBLE));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><invincible></invincible></mood>");
    }

    @Test
    public void marshalJealous() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.JEALOUS));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><jealous></jealous></mood>");
    }

    @Test
    public void marshalLonely() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.LONELY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><lonely></lonely></mood>");
    }

    @Test
    public void marshalLucky() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.LUCKY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><lucky></lucky></mood>");
    }

    @Test
    public void marshalMean() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.MEAN));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><mean></mean></mood>");
    }

    @Test
    public void marshalMoody() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.MOODY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><moody></moody></mood>");
    }

    @Test
    public void marshalNervous() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.NERVOUS));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><nervous></nervous></mood>");
    }

    @Test
    public void marshalNeutral() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.HAPPY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><happy></happy></mood>");
    }

    @Test
    public void marshalOffended() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.OFFENDED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><offended></offended></mood>");
    }

    @Test
    public void marshalPlayful() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.PLAYFUL));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><playful></playful></mood>");
    }

    @Test
    public void marshalProud() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.PROUD));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><proud></proud></mood>");
    }

    @Test
    public void marshalRelaxed() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.RELAXED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><relaxed></relaxed></mood>");
    }

    @Test
    public void marshalRelieved() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.RELIEVED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><relieved></relieved></mood>");
    }

    @Test
    public void marshalRemorseful() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.REMORSEFUL));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><remorseful></remorseful></mood>");
    }

    @Test
    public void marshalRestless() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.RESTLESS));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><restless></restless></mood>");
    }

    @Test
    public void marshalSad() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.SAD));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><sad></sad></mood>");
    }

    @Test
    public void marshalSarcastic() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.SARCASTIC));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><sarcastic></sarcastic></mood>");
    }

    @Test
    public void marshalSerious() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.SERIOUS));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><serious></serious></mood>");
    }

    @Test
    public void marshalShocked() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.SHOCKED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><shocked></shocked></mood>");
    }

    @Test
    public void marshalShy() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.SHY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><shy></shy></mood>");
    }

    @Test
    public void marshalSick() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.SICK));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><sick></sick></mood>");
    }

    @Test
    public void marshalSleepy() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.SLEEPY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><sleepy></sleepy></mood>");
    }

    @Test
    public void marshalSpontaneous() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.SPONTANEOUS));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><spontaneous></spontaneous></mood>");
    }

    @Test
    public void marshalStressed() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.STRESSED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><stressed></stressed></mood>");
    }

    @Test
    public void marshalStrong() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.STRONG));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><strong></strong></mood>");
    }

    @Test
    public void marshalSurprised() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.SURPRISED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><surprised></surprised></mood>");
    }

    @Test
    public void marshalThankful() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.THANKFUL));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><thankful></thankful></mood>");
    }

    @Test
    public void marshalThirsty() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.THIRSTY));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><thirsty></thirsty></mood>");
    }

    @Test
    public void marshalTired() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.TIRED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><tired></tired></mood>");
    }

    @Test
    public void marshalUndefined() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.UNDEFINED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><undefined></undefined></mood>");
    }

    @Test
    public void marshalWeak() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.WEAK));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><weak></weak></mood>");
    }

    @Test
    public void marshalWorried() throws XMLStreamException, JAXBException, IOException {
        String xml = marshall(new Mood(Mood.Value.WORRIED));
        Assert.assertEquals(xml, "<mood xmlns=\"http://jabber.org/protocol/mood\"><worried></worried></mood>");
    }
}
