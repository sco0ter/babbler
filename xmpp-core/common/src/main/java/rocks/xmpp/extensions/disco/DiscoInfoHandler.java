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

package rocks.xmpp.extensions.disco;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.info.InfoProvider;

/**
 * Handles 'disco#info' IQ requests by responding with appropriate identities, features and extensions.
 *
 * @author Christian Schudt
 */
final class DiscoInfoHandler extends AbstractIQHandler {

    private final Set<InfoProvider> infoProviders = new CopyOnWriteArraySet<>();

    DiscoInfoHandler() {
        super(InfoDiscovery.class, IQ.Type.GET);
    }

    @Override
    protected final IQ processRequest(IQ iq) {

        InfoDiscovery infoDiscovery = iq.getExtension(InfoDiscovery.class);
        Set<DiscoverableInfo> discoverableInfos = new HashSet<>();
        for (InfoProvider infoNodeProvider : infoProviders) {
            try {
                DiscoverableInfo info =
                        infoNodeProvider.getInfo(iq.getTo(), iq.getFrom(), infoDiscovery.getNode(), iq.getLanguage());
                if (info != null) {
                    discoverableInfos.add(info);
                }
            } catch (StanzaErrorException e) {
                return iq.createError(e.getError());
            }
        }

        Set<String> features = discoverableInfos.stream()
                .filter(infoNode1 -> Objects.nonNull(infoNode1.getFeatures()))
                .flatMap(infoNode1 -> infoNode1.getFeatures().stream())
                .collect(Collectors.toSet());
        Set<Identity> identities = discoverableInfos.stream()
                .filter(infoNode1 -> Objects.nonNull(infoNode1.getIdentities()))
                .flatMap(infoNode1 -> infoNode1.getIdentities().stream())
                .collect(Collectors.toSet());
        List<DataForm> extensions = discoverableInfos.stream()
                .filter(infoNode1 -> Objects.nonNull(infoNode1.getExtensions()))
                .flatMap(infoNode1 -> infoNode1.getExtensions().stream())
                .collect(Collectors.toList());

        if (!discoverableInfos.isEmpty()) {
            return iq.createResult(new InfoDiscovery(infoDiscovery.getNode(), identities, features, extensions));
        } else {
            return iq.createError(Condition.ITEM_NOT_FOUND);
        }
    }

    /**
     * Adds an info provider.
     *
     * @param infoProvider The info provider.
     * @return true, if it has been successfully added.
     * @see #removeInfoProvider(InfoProvider)
     */
    final boolean addInfoProvider(InfoProvider infoProvider) {
        return infoProviders.add(infoProvider);
    }

    /**
     * Removes an info provider.
     *
     * @param infoProvider The info provider.
     * @return true, if it has been successfully remove.
     * @see #addInfoProvider(InfoProvider)
     */
    final boolean removeInfoProvider(InfoProvider infoProvider) {
        return infoProviders.remove(infoProvider);
    }
}
