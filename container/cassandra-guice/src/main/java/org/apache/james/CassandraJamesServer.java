/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james;

import com.google.common.base.Throwables;
import com.google.inject.Injector;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.james.imapserver.netty.IMAPServer;
import org.apache.james.imapserver.netty.IMAPServerFactory;
import org.apache.james.modules.mailbox.CassandraMailboxModule;
import org.apache.james.modules.mailbox.CassandraSessionModule;
import org.apache.james.modules.mailbox.ElasticSearchMailboxModule;
import org.apache.james.modules.protocols.*;
import org.apache.james.modules.server.*;

import com.google.inject.Guice;
import org.apache.james.protocols.lib.handler.ProtocolHandlerLoader;
import org.apache.james.smtpserver.netty.SMTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraJamesServer {

    private final ClassPathConfigurationProvider classPathConfigurationProvider;

    public CassandraJamesServer() {
        this.classPathConfigurationProvider = new ClassPathConfigurationProvider();
    }

    public void start() {
        Injector injector = resolveInjections();
        postInit(injector);
    }

    private Injector resolveInjections() {
        Injector parentInjector = Guice.createInjector(new CassandraMailboxModule(),
            cassandraSessionModule(),
            elasticSearchMailboxModule(),
            new JpaDataServerModule(),
            new DNSServiceModule(),
            new ProtocolHandlerModule(),
            new ActiveMQQueueModule(),
            new CamelMailetContainerModule()
        );
        ProtocolHandlerLoader loader = new GuiceProtocolHandlerLoader(parentInjector);
        return parentInjector.createChildInjector(new IMAPServerModule(),
            pop3ServerModule(loader),
            smtpServerModule(loader),
            lmtpServerModule()
        );
    }

    private void postInit(Injector injector) {
        try {
            injector.getInstance(SMTPServer.class).init();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        try {
            initIMAPServers(injector);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void initIMAPServers(Injector injector) throws Exception {
        IMAPServerFactory imapServerFactory = injector.getInstance(IMAPServerFactory.class);
        imapServerFactory.configure(new ClassPathConfigurationProvider().getConfiguration("imapserver"));
        imapServerFactory.init();
    }

    public void stop() {
    }

    protected CassandraSessionModule cassandraSessionModule() {
        return new CassandraSessionModule();
    }

    protected ElasticSearchMailboxModule elasticSearchMailboxModule() {
        return new ElasticSearchMailboxModule();
    }

    protected POP3ServerModule pop3ServerModule(ProtocolHandlerLoader protocolHandlerLoader) {
        return new POP3ServerModule(protocolHandlerLoader);
    }

    protected SMTPServerModule smtpServerModule(ProtocolHandlerLoader protocolHandlerLoader) {
        return new SMTPServerModule(protocolHandlerLoader);
    }

    protected LMTPServerModule lmtpServerModule() {
        return new LMTPServerModule();
    }

}
