package org.xmpp.stream;

import junit.framework.Assert;
import org.testng.annotations.Test;
import org.xmpp.bind.Bind;
import org.xmpp.extension.compression.Compression;
import org.xmpp.im.session.Session;
import org.xmpp.sasl.Mechanisms;
import org.xmpp.tls.StartTls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Schudt
 */
public class FeatureComparisonTest {

    @Test
    public void testCorrectFeatureNegotiationOrder() {

        Compression compression = new Compression();
        StartTls startTls = new StartTls();
        Mechanisms mechanisms = new Mechanisms();
        Bind bind = new Bind();
        Session session = new Session();
        List<Feature> features = new ArrayList<>();
        features.add(compression);
        features.add(startTls);
        features.add(mechanisms);
        features.add(bind);
        features.add(session);

        Collections.shuffle(features);
        Collections.sort(features);

        Assert.assertEquals(features.get(0), startTls);
        Assert.assertEquals(features.get(1), mechanisms);
        Assert.assertEquals(features.get(2), compression);
        Assert.assertEquals(features.get(3), bind);
        Assert.assertEquals(features.get(4), session);
    }
}
