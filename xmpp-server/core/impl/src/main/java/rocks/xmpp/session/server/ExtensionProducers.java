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

package rocks.xmpp.session.server;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.extensions.caps.EntityCapabilitiesCache;
import rocks.xmpp.extensions.caps.server.ServerEntityCapabilities1Protocol;
import rocks.xmpp.extensions.caps2.server.ServerEntityCapabilities2Protocol;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.hashes.CryptographicHashFunctionsProtocol;
import rocks.xmpp.extensions.hashes.model.Hash;
import rocks.xmpp.extensions.ping.handler.PingHandler;
import rocks.xmpp.extensions.rsm.ResultSetManagementProtocol;
import rocks.xmpp.extensions.softwareinfo.SoftwareInformation;
import rocks.xmpp.extensions.softwareinfo.SoftwareInformationProtocol;
import rocks.xmpp.extensions.time.handler.EntityTimeHandler;
import rocks.xmpp.extensions.version.AbstractSoftwareVersionManager;
import rocks.xmpp.extensions.version.SoftwareVersionManager;
import rocks.xmpp.extensions.version.model.SoftwareVersion;
import rocks.xmpp.util.concurrent.AsyncResult;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class ExtensionProducers {

    @Inject
    private ServiceDiscoveryManager serviceDiscoveryManager;

    @Produces
    @ApplicationScoped
    public EntityTimeHandler produceEntityTimeHandler() {
        return new EntityTimeHandler();
    }

    @Produces
    @ApplicationScoped
    public PingHandler producePingHandler() {
        return new PingHandler();
    }

    @Produces
    @ApplicationScoped
    public CryptographicHashFunctionsProtocol cryptographicHashFunctionsProtocol() {
        return new CryptographicHashFunctionsProtocol();
    }

    @Produces
    @ApplicationScoped
    public ResultSetManagementProtocol resultSetManagementProtocol() {
        return new ResultSetManagementProtocol();
    }

    @Produces
    @ApplicationScoped
    public SoftwareVersionManager softwareVersionManager() {
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
    public SoftwareInformationProtocol softwareInformationProtocol() {
        SoftwareInformationProtocol softwareInformationProtocol = new SoftwareInformationProtocol();
        softwareInformationProtocol.setSoftwareInformation(new SoftwareInformation(null, "xmpp.rocks", "1.0"));
        return softwareInformationProtocol;
    }

    @Produces
    @ApplicationScoped
    public ServerEntityCapabilities1Protocol entityCapabilities1() {
        return new ServerEntityCapabilities1Protocol(serviceDiscoveryManager, new EntityCapabilitiesCache() {
            @Override
            public DiscoverableInfo readCapabilities(Hash hash) {
                return null;
            }

            @Override
            public void writeCapabilities(Hash hash, DiscoverableInfo discoverableInfo) {

            }

            @Override
            public DiscoverableInfo readEntityCapabilities(Jid entity) {
                return null;
            }

            @Override
            public void writeEntityCapabilities(Jid entity, DiscoverableInfo discoverableInfo) {

            }
        });
    }

    @Produces
    @ApplicationScoped
    public ServerEntityCapabilities2Protocol entityCapabilities2() {
        return new ServerEntityCapabilities2Protocol(serviceDiscoveryManager, new EntityCapabilitiesCache() {
            @Override
            public DiscoverableInfo readCapabilities(Hash hash) {
                return null;
            }

            @Override
            public void writeCapabilities(Hash hash, DiscoverableInfo discoverableInfo) {

            }

            @Override
            public DiscoverableInfo readEntityCapabilities(Jid entity) {
                return null;
            }

            @Override
            public void writeEntityCapabilities(Jid entity, DiscoverableInfo discoverableInfo) {

            }
        });
    }
}
