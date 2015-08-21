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

import javax.inject.Inject;

import org.apache.james.imap.api.process.ImapProcessor;
import org.apache.james.imap.decode.ImapDecoder;
import org.apache.james.imap.encode.ImapEncoder;
import org.apache.james.imap.encode.main.DefaultImapEncoderFactory;
import org.apache.james.imap.main.DefaultImapDecoderFactory;
import org.apache.james.imap.processor.main.DefaultImapProcessorFactory;
import org.apache.james.imapserver.netty.IMAPServerFactory;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.SubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.name.Names;

public class IMAPServerModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(IMAPServerModule.class);

    @Override
    protected void configure() {
        bind(IMAPServerFactory.class).toInstance(imapServerFactory());

        bind(ImapProcessor.class).annotatedWith(Names.named("imapProcessor")).toProvider(DefaultImapProcessorProvider.class);

        ImapDecoder imapDecoder = DefaultImapDecoderFactory.createDecoder();
        bind(ImapDecoder.class).annotatedWith(Names.named("imapDecoder")).toInstance(imapDecoder);

        DefaultImapEncoderFactory defaultImapEncoderFactory = new DefaultImapEncoderFactory();
        bind(ImapEncoder.class).annotatedWith(Names.named("imapEncoder")).toInstance(defaultImapEncoderFactory.buildImapEncoder());
    }

    private IMAPServerFactory imapServerFactory() {
        try {
            IMAPServerFactory imapServerFactory = new IMAPServerFactory();
            imapServerFactory.setLog(LOGGER);
            return imapServerFactory;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static class DefaultImapProcessorProvider implements Provider<ImapProcessor> {

        private final MailboxManager mailboxManager;
        private final SubscriptionManager subscriptionManager;

        @Inject
        private DefaultImapProcessorProvider(MailboxManager mailboxManager, SubscriptionManager subscriptionManager) {
            this.mailboxManager = mailboxManager;
            this.subscriptionManager = subscriptionManager;
        }

        @Override
        public ImapProcessor get() {
            return DefaultImapProcessorFactory.createXListSupportingProcessor(mailboxManager, subscriptionManager, null, 120, ImmutableSet.of("ACL", "MOVE"));
        }

    }
}