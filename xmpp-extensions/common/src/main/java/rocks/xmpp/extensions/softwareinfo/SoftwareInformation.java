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

import java.util.Arrays;

import rocks.xmpp.extensions.data.StandardizedDataForm;
import rocks.xmpp.extensions.data.mediaelement.model.Media;
import rocks.xmpp.extensions.data.model.DataForm;

/**
 * Represents a view of the standardized fields of the 'urn:xmpp:dataforms:softwareinfo' data form
 * to be included in service discovery responses to provide detailed information about the software.
 *
 * <p>This class is immutable.</p>
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0232.html#registrar-formtype">5.1 Field Standardization</a>
 */
public final class SoftwareInformation implements StandardizedDataForm, SoftwareInfo {

    static final String FORM_TYPE = "urn:xmpp:dataforms:softwareinfo";

    static final String ICON = "icon";

    static final String OS = "os";

    static final String OS_VERSION = "os_version";

    static final String SOFTWARE = "software";

    static final String SOFTWARE_VERSION = "software_version";

    private final DataForm dataForm;

    /**
     * Creates a new Software Info data form, where the OS name and version are automatically taken from their respective system properties "os.name" and "os.version".
     *
     * @param icon            A default icon to show for a device running the software.
     * @param software        The XMPP software running at the entity.
     * @param softwareVersion The XMPP software version.
     */
    public SoftwareInformation(final Media icon, final String software, final String softwareVersion) {
        this(icon, software, softwareVersion, System.getProperty("os.name"), System.getProperty("os.version"));
    }

    /**
     * Creates a new Software Info data form.
     *
     * @param icon            A default icon to show for a device running the software.
     * @param software        The XMPP software running at the entity.
     * @param softwareVersion The XMPP software version.
     * @param os              The operating system on which the XMPP software is running.
     * @param osVersion       The operating system version.
     */
    public SoftwareInformation(final Media icon, final String software, final String softwareVersion, final String os, final String osVersion) {
        this.dataForm = new DataForm(DataForm.Type.RESULT,
                Arrays.asList(
                        DataForm.Field.builder().name(DataForm.FORM_TYPE).value(FORM_TYPE).type(DataForm.Field.Type.HIDDEN).build(),
                        DataForm.Field.builder().name(ICON).media(icon).build(),
                        DataForm.Field.builder().name(OS).value(os).build(),
                        DataForm.Field.builder().name(OS_VERSION).value(osVersion).build(),
                        DataForm.Field.builder().name(SOFTWARE).value(software).build(),
                        DataForm.Field.builder().name(SOFTWARE_VERSION).value(softwareVersion).build()
                )
        );
    }

    /**
     * Creates a software info from a data form.
     *
     * @param dataForm The data form.
     * @throws IllegalArgumentException If the data form's form type is not 'urn:xmpp:dataforms:softwareinfo'.
     */
    public SoftwareInformation(final DataForm dataForm) {
        if (!FORM_TYPE.equals(dataForm.getFormType())) {
            throw new IllegalArgumentException("Data form FORM_TYPE must be '" + FORM_TYPE + '\'');
        }
        this.dataForm = dataForm;
    }

    /**
     * A default icon to show for a device running the software.
     *
     * @return The default icon.
     */
    public final Media getIcon() {
        DataForm.Field iconField = dataForm.findField(ICON);
        if (iconField != null) {
            return iconField.getMedia();
        }
        return null;
    }

    @Override
    public final String getOs() {
        return dataForm.findValue(OS);
    }

    /**
     * Gets the operating system version.
     *
     * @return The operating system version.
     */
    public final String getOsVersion() {
        return dataForm.findValue(OS_VERSION);
    }

    @Override
    public final String getSoftware() {
        return dataForm.findValue(SOFTWARE);
    }

    /**
     * Gets the XMPP software version.
     *
     * @return The XMPP software version.
     */
    @Override
    public final String getSoftwareVersion() {
        return dataForm.findValue(SOFTWARE_VERSION);
    }

    @Override
    public final String getFormType() {
        return FORM_TYPE;
    }

    /**
     * Gets the underlying data form.
     *
     * @return The underlying data form.
     */
    @Override
    public final DataForm getDataForm() {
        return dataForm;
    }

    @Override
    public final String toString() {
        return dataForm.toString();
    }
}
