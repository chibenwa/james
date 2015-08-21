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
import org.apache.james.imapserver.netty.IMAPServerFactory;
import org.apache.james.lmtpserver.netty.LMTPServerFactory;
import org.apache.james.modules.mailbox.CassandraMailboxModule;
import org.apache.james.modules.mailbox.CassandraSessionModule;
import org.apache.james.modules.mailbox.ElasticSearchMailboxModule;
import org.apache.james.modules.protocols.*;
import org.apache.james.modules.server.*;

import com.google.inject.Guice;
import org.apache.james.pop3server.netty.POP3ServerFactory;
import org.apache.james.protocols.lib.handler.ProtocolHandlerLoader;
import org.apache.james.smtpserver.netty.SMTPServerFactory;

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
            new POP3ServerModule(loader),
            new SMTPServerModule(),
            new LMTPServerModule()
        );
    }

    private void postInit(Injector injector) {
        try {
            initIMAPServers(injector);
            initPOP3Servers(injector);
            initSMTPServers(injector);
            initLMTPServers(injector);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void initPOP3Servers(Injector injector) throws Exception {
        POP3ServerFactory pop3ServerFactory = injector.getInstance(POP3ServerFactory.class);
        pop3ServerFactory.configure(classPathConfigurationProvider.getConfiguration("pop3server"));
        pop3ServerFactory.init();
    }

    private void initIMAPServers(Injector injector) throws Exception {
        IMAPServerFactory imapServerFactory = injector.getInstance(IMAPServerFactory.class);
        imapServerFactory.configure(classPathConfigurationProvider.getConfiguration("imapserver"));
        imapServerFactory.init();
    }

    private void initSMTPServers(Injector injector) throws Exception {
        SMTPServerFactory smtpServerFactory = injector.getInstance(SMTPServerFactory.class);
        smtpServerFactory.configure(classPathConfigurationProvider.getConfiguration("smtpserver"));
        smtpServerFactory.init();
    }

    private void initLMTPServers(Injector injector) throws Exception {
        LMTPServerFactory lmtpServerFactory = injector.getInstance(LMTPServerFactory.class);
        lmtpServerFactory.configure(classPathConfigurationProvider.getConfiguration("lmtpserver"));
        lmtpServerFactory.init();
    }

    public void stop() {
    }

    protected CassandraSessionModule cassandraSessionModule() {
        return new CassandraSessionModule();
    }

    protected ElasticSearchMailboxModule elasticSearchMailboxModule() {
        return new ElasticSearchMailboxModule();
    }

}
