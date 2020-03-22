/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.extensions.softwareinfo;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.extensions.data.mediaelement.model.Media;
import rocks.xmpp.extensions.data.model.DataForm;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.Arrays;

/**
 * @author Christian Schudt
 */
public class SoftwareInfoTest extends XmlTest {

    @Test
    public void testSoftwareInfoCreation() throws JAXBException, XMLStreamException {
        SoftwareInformation softwareInfo = new SoftwareInformation(new Media(), "xmpp.rocks", "1.0", "Windows", "10");
        String xml = marshal(softwareInfo.getDataForm());

        Assert.assertEquals(xml, "<x xmlns=\"jabber:x:data\" type=\"result\">" +
                "<field type=\"hidden\" var=\"FORM_TYPE\"><value>urn:xmpp:dataforms:softwareinfo</value></field>" +
                "<field var=\"icon\"><media xmlns=\"urn:xmpp:media-element\" height=\"0\" width=\"0\"></media></field>" +
                "<field type=\"text-single\" var=\"os\"><value>Windows</value></field>" +
                "<field type=\"text-single\" var=\"os_version\"><value>10</value></field>" +
                "<field type=\"text-single\" var=\"software\"><value>xmpp.rocks</value></field>" +
                "<field type=\"text-single\" var=\"software_version\"><value>1.0</value></field>" +
                "</x>");

        Assert.assertNotNull(softwareInfo.getIcon());
        Assert.assertEquals(softwareInfo.getOs(), "Windows");
        Assert.assertEquals(softwareInfo.getOsVersion(), "10");
        Assert.assertEquals(softwareInfo.getSoftware(), "xmpp.rocks");
        Assert.assertEquals(softwareInfo.getSoftwareVersion(), "1.0");
    }

    @Test
    public void testSoftwareInfoSystemPropertiesCreation() throws JAXBException, XMLStreamException {
        SoftwareInformation softwareInfo = new SoftwareInformation(new Media(), "xmpp.rocks", "1.0");
        String xml = marshal(softwareInfo.getDataForm());

        Assert.assertNotNull(softwareInfo.getIcon());
        Assert.assertEquals(softwareInfo.getOs(), System.getProperty("os.name"));
        Assert.assertEquals(softwareInfo.getOsVersion(), System.getProperty("os.version"));
        Assert.assertEquals(softwareInfo.getSoftware(), "xmpp.rocks");
        Assert.assertEquals(softwareInfo.getSoftwareVersion(), "1.0");
    }

    @Test
    public void testSoftwareInfoFromDataForm() throws JAXBException, XMLStreamException {

        DataForm dataForm = new DataForm(DataForm.Type.RESULT, Arrays.asList(
                DataForm.Field.builder().var(DataForm.FORM_TYPE).value(SoftwareInformation.FORM_TYPE).type(DataForm.Field.Type.HIDDEN).build(),
                DataForm.Field.builder().var(SoftwareInformation.ICON).media(null).build(),
                DataForm.Field.builder().var(SoftwareInformation.OS).value("Linux").build(),
                DataForm.Field.builder().var(SoftwareInformation.OS_VERSION).value("123").build(),
                DataForm.Field.builder().var(SoftwareInformation.SOFTWARE).value("sw").build(),
                DataForm.Field.builder().var(SoftwareInformation.SOFTWARE_VERSION).value("123").build()
        ));

        SoftwareInformation softwareInfo = new SoftwareInformation(dataForm);

        Assert.assertNull(softwareInfo.getIcon());
        Assert.assertEquals(softwareInfo.getOs(), "Linux");
        Assert.assertEquals(softwareInfo.getOsVersion(), "123");
        Assert.assertEquals(softwareInfo.getSoftware(), "sw");
        Assert.assertEquals(softwareInfo.getSoftwareVersion(), "123");
    }
}
