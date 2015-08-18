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

package org.apache.james.modules.protocols;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.james.protocols.lib.handler.ProtocolHandlerLoader;
import org.apache.james.smtpserver.netty.SMTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SMTPServerModule extends AbstractModule {

    public static final int DEFAULT_SMTP_PORT = 25;
    private static final Logger LOGGER = LoggerFactory.getLogger(SMTPServerModule.class);

    private final ProtocolHandlerLoader protocolHandlerLoader;

    public SMTPServerModule(ProtocolHandlerLoader protocolHandlerLoader) {
        this.protocolHandlerLoader = protocolHandlerLoader;
    }

    @Override
    protected void configure() {
        bind(SMTPServer.class).toInstance(smtpServer());
    }

    private SMTPServer smtpServer() {
        SMTPServer smtpServer = new SMTPServer();
        smtpServer.setBacklog(200);
        smtpServer.setLog(LOGGER);
        smtpServer.setProtocolHandlerLoader(protocolHandlerLoader);
        try {
            smtpServer.configure(generateHierarchicalConfiguration());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return smtpServer;
    }

    private HierarchicalConfiguration generateHierarchicalConfiguration() {
        HierarchicalConfiguration.Node handler1 = new HierarchicalConfiguration.Node("handler");
        handler1.addAttribute(new HierarchicalConfiguration.Node("class", "org.apache.james.smtpserver.fastfail.ValidRcptHandler"));

        HierarchicalConfiguration.Node handler2 = new HierarchicalConfiguration.Node("handler");
        handler2.addAttribute(new HierarchicalConfiguration.Node("class", "org.apache.james.smtpserver.CoreCmdHandlerLoader"));

        HierarchicalConfiguration.Node handlerChain = new HierarchicalConfiguration.Node("handlerchain");
        handlerChain.addAttribute(new HierarchicalConfiguration.Node("enableJmx", "false"));
        handlerChain.addChild(handler1);
        handlerChain.addChild(handler2);

        HierarchicalConfiguration configuration = new HierarchicalConfiguration();
        configuration.setProperty("bind", "0.0.0.0:" + smtpPort());
        configuration.getRoot().addChild(handlerChain);
        return configuration;
    }

    protected int smtpPort() {
        return DEFAULT_SMTP_PORT;
    }
}
