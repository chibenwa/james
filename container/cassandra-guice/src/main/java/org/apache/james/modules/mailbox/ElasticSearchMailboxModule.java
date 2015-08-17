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

package org.apache.james.modules.mailbox;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.apache.james.mailbox.cassandra.CassandraId;
import org.apache.james.mailbox.elasticsearch.ClientProvider;
import org.apache.james.mailbox.elasticsearch.GuiceClientProvider;
import org.apache.james.mailbox.elasticsearch.events.ElasticSearchListeningMessageSearchIndex;
import org.apache.james.mailbox.store.extractor.TextExtractor;
import org.apache.james.mailbox.store.search.MessageSearchIndex;
import org.apache.james.mailbox.tika.extractor.TikaTextExtractor;

public class ElasticSearchMailboxModule extends AbstractModule {

    @Override
    protected void configure() {
        bindClientProvider();

        bind(new TypeLiteral<MessageSearchIndex<CassandraId>>(){}).to(new TypeLiteral<ElasticSearchListeningMessageSearchIndex<CassandraId>>() {});
        bind(TextExtractor.class).to(TikaTextExtractor.class);
    }

    protected void bindClientProvider() {
        bind(ClientProvider.class).toProvider(GuiceClientProvider.class);
    }

    protected void bindConstants() {
        bind(String.class).annotatedWith(Names.named(GuiceClientProvider.ELASTICSEARCH_MASTER_HOST)).toInstance("cassandra");
        bind(Integer.class).annotatedWith(Names.named(GuiceClientProvider.ELASTICSEARCH_PORT)).toInstance(9300);
        bind(Integer.class).annotatedWith(Names.named(GuiceClientProvider.ELASTICSEARCH_NB_SHARDS)).toInstance(1);
        bind(Integer.class).annotatedWith(Names.named(GuiceClientProvider.ELASTICSEARCH_NB_REPLICA)).toInstance(0);
    }
}
