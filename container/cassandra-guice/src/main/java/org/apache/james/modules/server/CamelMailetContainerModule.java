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

package org.apache.james.modules.server;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.james.dnsservice.api.DNSService;
import org.apache.james.domainlist.api.DomainList;
import org.apache.james.mailetcontainer.api.MailProcessor;
import org.apache.james.mailetcontainer.api.MailetLoader;
import org.apache.james.mailetcontainer.api.MatcherLoader;
import org.apache.james.mailetcontainer.api.jmx.MailSpoolerMBean;
import org.apache.james.mailetcontainer.impl.JamesMailSpooler;
import org.apache.james.mailetcontainer.impl.JamesMailetContext;
import org.apache.james.mailetcontainer.impl.camel.CamelCompositeProcessor;
import org.apache.james.user.api.UsersRepository;
import org.apache.james.utils.ClassPathConfigurationProvider;
import org.apache.james.utils.ConfigurationPerformer;
import org.apache.james.utils.GuiceMailetLoader;
import org.apache.james.utils.GuiceMatcherLoader;
import org.apache.mailet.MailetContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

public class CamelMailetContainerModule extends AbstractModule {

    private final static Logger LOGGER = LoggerFactory.getLogger(CamelMailetContainerModule.class);

    @Override
    protected void configure() {
        bind(MailProcessor.class).to(CamelCompositeProcessor.class);
        bind(MailSpoolerMBean.class).to(JamesMailSpooler.class);
        bind(MailetLoader.class).annotatedWith(Names.named(MailetLoader.COMPONENT_NAME)).to(GuiceMailetLoader.class);
        bind(MatcherLoader.class).annotatedWith(Names.named(MatcherLoader.COMPONENT_NAME)).to(GuiceMatcherLoader.class);
        Multibinder.newSetBinder(binder(), ConfigurationPerformer.class).addBinding().to(IMAPModuleConfigurationPerformer.class);
    }

    @Provides
    @Singleton
    @Inject
    private MailetContext provideMailetContext(MailProcessor processorList,
                                                    @Named(DNSService.COMPONENT_NAME)DNSService dns,
                                                    @Named(UsersRepository.COMPONENT_NAME)UsersRepository localusers,
                                                    @Named(DomainList.COMPONENT_NAME)DomainList domains) {
        JamesMailetContext jamesMailetContext = new JamesMailetContext();
        jamesMailetContext.setLog(LOGGER);
        jamesMailetContext.setDNSService(dns);
        jamesMailetContext.setMailProcessor(processorList);
        jamesMailetContext.setUsersRepository(localusers);
        jamesMailetContext.setDomainList(domains);
        return jamesMailetContext;
    }

    @Singleton
    public static class IMAPModuleConfigurationPerformer implements ConfigurationPerformer {

        private final ClassPathConfigurationProvider classPathConfigurationProvider;
        private final CamelCompositeProcessor camelCompositeProcessor;
        private final JamesMailSpooler jamesMailSpooler;
        private final JamesMailetContext mailetContext;

        @Inject
        public IMAPModuleConfigurationPerformer(ClassPathConfigurationProvider classPathConfigurationProvider,
                                                CamelCompositeProcessor camelCompositeProcessor,
                                                JamesMailSpooler jamesMailSpooler,
                                                JamesMailetContext mailetContext) {
            this.classPathConfigurationProvider = classPathConfigurationProvider;
            this.camelCompositeProcessor = camelCompositeProcessor;
            this.jamesMailSpooler = jamesMailSpooler;
            this.mailetContext = mailetContext;
        }

        @Override
        public void initModule() throws Exception {
            camelCompositeProcessor.setLog(LOGGER);
            camelCompositeProcessor.setCamelContext(new DefaultCamelContext());
            camelCompositeProcessor.configure(classPathConfigurationProvider.getConfiguration("mailetcontainer").configurationAt("processors"));
            camelCompositeProcessor.init();
            jamesMailSpooler.setLog(LOGGER);
            jamesMailSpooler.configure(classPathConfigurationProvider.getConfiguration("mailetcontainer").configurationAt("spooler"));
            jamesMailSpooler.init();
            mailetContext.setLog(LOGGER);
            mailetContext.configure(classPathConfigurationProvider.getConfiguration("mailetcontainer").configurationAt("context"));
        }
    }

}
