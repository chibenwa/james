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
import org.apache.james.lmtpserver.netty.LMTPServer;
import org.apache.james.lmtpserver.netty.LMTPServerMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class LMTPServerModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(LMTPServerModule.class);
    public static final int LMTP_DEFAULT_PORT = 24;

    @Override
    protected void configure() {
        bind(LMTPServerMBean.class).toInstance(lmtpServer());
    }

    private LMTPServer lmtpServer() {
        try {
            LMTPServer lmtpServer = new LMTPServer();
            lmtpServer.setListenAddresses(new InetSocketAddress("0.0.0.0", lmtpPort()));
            lmtpServer.setBacklog(200);
            lmtpServer.setLog(LOGGER);
            lmtpServer.configure(createHierarchicalConfiguration());
            lmtpServer.bind();
            return lmtpServer;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private HierarchicalConfiguration createHierarchicalConfiguration() {
        HierarchicalConfiguration configuration = new HierarchicalConfiguration();
        configuration.setProperty("bind", "0.0.0.0:" + lmtpPort());

        HierarchicalConfiguration.Node handler1 = new HierarchicalConfiguration.Node("handler");
        handler1.addAttribute(new HierarchicalConfiguration.Node("class", "org.apache.james.lmtpserver.CoreCmdHandlerLoader"));

        HierarchicalConfiguration.Node handlerChain = new HierarchicalConfiguration.Node("handlerchain");
        handlerChain.addAttribute(new HierarchicalConfiguration.Node("enableJmx", "false"));
        handlerChain.addChild(handler1);

        configuration.getRoot().addChild(handlerChain);
        return configuration;
    }

    protected int lmtpPort() {
        return LMTP_DEFAULT_PORT;
    }
}
