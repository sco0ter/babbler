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

package rocks.xmpp.extensions.disco.server;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.extensions.caps.EntityCapabilitiesCache;
import rocks.xmpp.extensions.caps.ServerEntityCapabilities1;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.info.InfoNodeProvider;
import rocks.xmpp.extensions.hashes.CryptographicHashFunctionsProtocol;
import rocks.xmpp.extensions.hashes.model.Hash;
import rocks.xmpp.extensions.rsm.ResultSetManagementProtocol;
import rocks.xmpp.extensions.softwareinfo.SoftwareInformation;
import rocks.xmpp.extensions.softwareinfo.SoftwareInformationProtocol;
import rocks.xmpp.extensions.version.AbstractSoftwareVersionManager;
import rocks.xmpp.extensions.version.SoftwareVersionManager;
import rocks.xmpp.extensions.version.model.SoftwareVersion;
import rocks.xmpp.util.concurrent.AsyncResult;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Christian Schudt
 */
@ApplicationScoped
public class ServerInfoNode implements InfoNode {

    @Inject
    private ServiceDiscoveryManager serviceDiscoveryManager;

    @Inject
    private Instance<ExtensionProtocol> extensionProtocols;

    @Inject
    private Instance<InfoNodeProvider> infoNodeProviders;

    @Override
    public String getNode() {
        return null;
    }

    @Override
    public Set<Identity> getIdentities() {
        return Collections.singleton(Identity.serverInstantMessaging());
    }

    @Override
    public Set<String> getFeatures() {
        return extensionProtocols.stream()
                .flatMap(extensionProtocol -> extensionProtocol.getFeatures().stream())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public List<DataForm> getExtensions() {
        return Collections.emptyList();
    }

    @Produces
    @ApplicationScoped
    private ServerServiceDiscoveryManager serviceDiscoveryManager() {
        ServerServiceDiscoveryManager serverServiceDiscoveryManager = new ServerServiceDiscoveryManager();
        serverServiceDiscoveryManager.addInfoNode(this);
        infoNodeProviders.stream().forEach(serverServiceDiscoveryManager::addInfoNodeProvider);
        return serverServiceDiscoveryManager;
    }

    @Produces
    @ApplicationScoped
    private CryptographicHashFunctionsProtocol cryptographicHashFunctionsProtocol() {
        return new CryptographicHashFunctionsProtocol();
    }

    @Produces
    @ApplicationScoped
    private ResultSetManagementProtocol resultSetManagementProtocol() {
        return new ResultSetManagementProtocol();
    }

    @Produces
    @ApplicationScoped
    private SoftwareVersionManager softwareVersionManager() {
        SoftwareVersionManager softwareVersionManager = new AbstractSoftwareVersionManager() {
            @Override
            public AsyncResult<SoftwareVersion> getSoftwareVersion(Jid jid) {
                return null;
            }
        };
        softwareVersionManager.setSoftwareVersion(new SoftwareVersion("xmpp.rocks", "1.0", System.getProperty("os.name")));
        return softwareVersionManager;
    }

    @Produces
    @ApplicationScoped
    private SoftwareInformationProtocol softwareInformationProtocol() {
        SoftwareInformationProtocol softwareInformationProtocol = new SoftwareInformationProtocol();
        softwareInformationProtocol.setSoftwareInformation(new SoftwareInformation(null, "xmpp.rocks", "1.0"));
        return softwareInformationProtocol;
    }

    @Produces
    @ApplicationScoped
    private ServerEntityCapabilities1 entityCapabilities1() {
        return new ServerEntityCapabilities1(serviceDiscoveryManager, new EntityCapabilitiesCache() {
            @Override
            public InfoNode readCapabilities(Hash hash) {
                return null;
            }

            @Override
            public void writeCapabilities(Hash hash, InfoNode infoNode) {

            }

            @Override
            public InfoNode readEntityCapabilities(Jid entity) {
                return null;
            }

            @Override
            public void writeEntityCapabilities(Jid entity, InfoNode infoNode) {

            }
        });
    }
}
