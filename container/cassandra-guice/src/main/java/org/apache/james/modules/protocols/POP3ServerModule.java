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
import com.google.inject.name.Names;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.james.pop3server.netty.POP3Server;
import org.apache.james.pop3server.netty.POP3ServerMBean;
import org.apache.james.protocols.lib.handler.ProtocolHandlerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class POP3ServerModule extends AbstractModule {

    public static final int DEFAULT_POP3_PORT = 110;
    private static final Logger LOGGER = LoggerFactory.getLogger(POP3ServerModule.class);

    private final ProtocolHandlerLoader protocolHandlerLoader;

    public POP3ServerModule(ProtocolHandlerLoader protocolHandlerLoader) {
        this.protocolHandlerLoader = protocolHandlerLoader;
    }

    @Override
    protected void configure() {
        bind(ProtocolHandlerLoader.class).annotatedWith(Names.named("protocolhandlerloader")).toInstance(protocolHandlerLoader);
        bind(ProtocolHandlerLoader.class).toInstance(protocolHandlerLoader);
        POP3Server pop3Server = pop3Server();
        bind(POP3ServerMBean.class).toInstance(pop3Server);
    }

    private POP3Server pop3Server() {
        try {
            POP3Server pop3Server = new POP3Server();
            pop3Server.setBacklog(200);
            pop3Server.setLog(LOGGER);
            pop3Server.setProtocolHandlerLoader(protocolHandlerLoader);
            pop3Server.configure(createHierarchicalConfiguration());
            pop3Server.bind();
            return pop3Server;
        } catch(Exception exception) {
            throw Throwables.propagate(exception);
        }
    }

    private HierarchicalConfiguration createHierarchicalConfiguration() {
        HierarchicalConfiguration configuration = new HierarchicalConfiguration();
        configuration.setProperty("bind", "0.0.0.0:" + pop3Port());

        HierarchicalConfiguration.Node handler1 = new HierarchicalConfiguration.Node("handler");
        handler1.addAttribute(new HierarchicalConfiguration.Node("class", "org.apache.james.pop3server.core.CoreCmdHandlerLoader"));

        HierarchicalConfiguration.Node handlerChain = new HierarchicalConfiguration.Node("handlerchain");
        handlerChain.addAttribute(new HierarchicalConfiguration.Node("enableJmx", "false"));
        handlerChain.addChild(handler1);

        configuration.getRoot().addChild(handlerChain);
        return configuration;
    }

    protected int pop3Port() {
        return DEFAULT_POP3_PORT;
    }

}
