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
import com.google.inject.name.Names;
import org.apache.james.mailetcontainer.api.MailProcessor;
import org.apache.james.mailetcontainer.api.MailetLoader;
import org.apache.james.mailetcontainer.api.MatcherLoader;
import org.apache.james.mailetcontainer.api.jmx.MailSpoolerMBean;
import org.apache.james.mailetcontainer.impl.JamesMailSpooler;
import org.apache.james.mailetcontainer.impl.JamesMailetContext;
import org.apache.james.mailetcontainer.impl.camel.CamelCompositeProcessor;
import org.apache.mailet.MailetContext;

public class CamelMailetContainerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MailProcessor.class).to(CamelCompositeProcessor.class);
        bind(MailetContext.class).to(JamesMailetContext.class);
        bind(MailSpoolerMBean.class).to(JamesMailSpooler.class);
        bind(MailetLoader.class).annotatedWith(Names.named("mailetloader")).to(JavaMailetLoader.class);
        bind(MatcherLoader.class).annotatedWith(Names.named("matcherloader")).to(JavaMatcherLoader.class);
    }
}
