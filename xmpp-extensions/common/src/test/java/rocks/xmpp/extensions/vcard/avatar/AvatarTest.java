/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.vcard.avatar;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.extensions.vcard.avatar.model.AvatarUpdate;
import rocks.xmpp.util.XmppUtils;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Christian Schudt
 */
public class AvatarTest extends XmlTest {

    @Test
    public void marshalVCardUpdateWithEmptyPhotoElement() throws JAXBException, XMLStreamException {
        AvatarUpdate avatarUpdate = new AvatarUpdate("");
        Assert.assertEquals(avatarUpdate.getHashAlgorithm(), "SHA-1");
        Assert.assertEquals(avatarUpdate.getHashValue(), new byte[0]);
        String xml = marshal(avatarUpdate);
        Assert.assertEquals("<x xmlns=\"vcard-temp:x:update\"><photo></photo></x>", xml);
    }

    @Test
    public void marshalVCardUpdateWithNoPhotoElement() throws JAXBException, XMLStreamException {
        AvatarUpdate avatarUpdate = new AvatarUpdate();
        Assert.assertNull(avatarUpdate.getHashValue());
        String xml = marshal(avatarUpdate);
        Assert.assertEquals("<x xmlns=\"vcard-temp:x:update\"></x>", xml);
    }

    @Test
    public void marshalVCardUpdateWithPhotoElement() throws JAXBException, XMLStreamException, NoSuchAlgorithmException {
        byte[] image = new byte[]{1, 2, 3, 4};
        String sha1 = XmppUtils.hash(image);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        AvatarUpdate avatarUpdate = new AvatarUpdate(sha1);
        Assert.assertEquals(avatarUpdate.getHashValue(), messageDigest.digest(image));
        String xml = marshal(avatarUpdate);
        Assert.assertEquals("<x xmlns=\"vcard-temp:x:update\"><photo>" + sha1 + "</photo></x>", xml);
    }
}
