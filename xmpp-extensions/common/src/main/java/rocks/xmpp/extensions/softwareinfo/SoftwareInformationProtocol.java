/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import rocks.xmpp.extensions.data.model.DataForm;

/**
 * Represents the XEP-0232: Software Information.
 *
 * <p>In order to provide software information to other entities, simply {@linkplain
 * #setSoftwareInfo(SoftwareInformation) set} it.</p>
 *
 * <p>This class is thread-safe.</p>
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0232.html">XEP-0232: Software Information</a>
 * @since 0.9.0
 */
public final class SoftwareInformationProtocol implements SoftwareInfoProvider<SoftwareInformation> {

    private SoftwareInformation softwareInformation;

    /**
     * Gets the software information.
     *
     * @return The software information.
     */
    @Override
    public final synchronized SoftwareInformation getSoftwareInfo() {
        return softwareInformation;
    }

    /**
     * Gets the software information.
     *
     * @param softwareInformation The software information.
     */
    @Override
    public final synchronized void setSoftwareInfo(final SoftwareInformation softwareInformation) {
        this.softwareInformation = softwareInformation;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@value SoftwareInformation#FORM_TYPE}
     */
    @Override
    public final String getNamespace() {
        return SoftwareInformation.FORM_TYPE;
    }

    @Override
    public final synchronized boolean isEnabled() {
        return softwareInformation != null;
    }

    @Override
    public final Set<String> getFeatures() {
        return Collections.emptySet();
    }

    @Override
    public final synchronized List<DataForm> getExtensions() {
        if (softwareInformation != null) {
            return Collections.singletonList(softwareInformation.getDataForm());
        }
        return Collections.emptyList();
    }
}
